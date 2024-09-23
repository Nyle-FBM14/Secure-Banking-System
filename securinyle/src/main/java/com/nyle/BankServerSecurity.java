package com.nyle;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.nyle.enumerations.Algorithms;
import com.nyle.enumerations.KeySizes;
import com.nyle.enumerations.MessageHeaders;

public class BankServerSecurity {
    private SecretKey masterKey; //initially created with each new ATM connection
    private SecretKey masterSessionKey; //created with each client login - a session key for encryption key and a mac key will be derived from this
    private SecretKey sessionKey;
    private SecretKey macKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey publicKeyATM;

    public BankServerSecurity() {
        KeyPair keypair = RSA.generateRSAkeypair();
        privateKey = keypair.getPrivate();
        publicKey = keypair.getPublic();
    }

    public SecuredMessage GenerateDHPrimeMessage() {
        byte[] prime = Utils.serialize(Utils.getLargePrime());
        byte[] signedPrime = RSA.signDigitalSignature(privateKey, prime);
        return new SecuredMessage(prime, signedPrime);
    }
    public KeyPair generateDHKeyPair(SecuredMessage dhPrime, byte[] myPrime) {
        if(RSA.verifyDigitalSignature(publicKeyATM, dhPrime.getMessage(), dhPrime.getMessageIntegrityAuthentication())) {
            BigInteger p1 = (BigInteger) Utils.deserialize(myPrime);
            BigInteger p2 = (BigInteger) Utils.deserialize(dhPrime.getMessage());
            DHParameterSpec dhSpecs = new DHParameterSpec(p1, p2, KeySizes.DH_PRIME.SIZE);
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
                keyGen.initialize(dhSpecs);
                return keyGen.generateKeyPair();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public SecuredMessage GenerateDHPublicKeyMessage(PublicKey key) {
        byte[] signedKey = RSA.signDigitalSignature(privateKey, key);
        return new SecuredMessage(Utils.serialize(key), signedKey);
    }
    public SecretKey generateMasterKey(SecuredMessage atmPubKey, PrivateKey prKey) {
        if(RSA.verifyDigitalSignature(publicKeyATM, atmPubKey.getMessage(), atmPubKey.getMessageIntegrityAuthentication())){
            try {
                PublicKey atmKey = (PublicKey) Utils.deserialize(atmPubKey.getMessage());
                KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                keyAgree.init(prKey);
                keyAgree.doPhase(atmKey, true);
                masterKey = keyAgree.generateSecret(Algorithms.AES.INSTANCE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return masterKey;
    }
    @SuppressWarnings("unchecked")
    public SecretKey generateMasterSessionKey(SecuredMessage credentials) {
        HashMap<MessageHeaders, String> message = (HashMap<MessageHeaders, String>) Utils.deserialize(credentials.getMessage());
        String base = message.get(MessageHeaders.CARDNUM) + message.get(MessageHeaders.PIN); //add timestamp
        try {
            PBEKeySpec spec = new PBEKeySpec(base.toCharArray(), Utils.getSalt(), KeySizes.MASTERKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] msk = factory.generateSecret(spec).getEncoded();
            masterSessionKey = new SecretKeySpec(msk, Algorithms.AES.INSTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return masterSessionKey;
    }
    public SecuredMessage deriveSessionAndMacKeysAndGenerateMessage() {
        SecuredMessage message;
        try {
            PBEKeySpec spec = new PBEKeySpec(Utils.keyToString(masterSessionKey).toCharArray(), Utils.getSalt(), KeySizes.MASTERKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] sk = factory.generateSecret(spec).getEncoded();
            spec = new PBEKeySpec(Utils.keyToString(masterSessionKey).toCharArray(), Utils.getSalt(), KeySizes.MASTERKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
            byte[] mak = factory.generateSecret(spec).getEncoded();
            sessionKey = new SecretKeySpec(sk, Algorithms.AES.INSTANCE);
            macKey = new SecretKeySpec(mak, Algorithms.AES.INSTANCE);

            HashMap<MessageHeaders, String> keys =  new HashMap<MessageHeaders, String>();
            //make new headers
            message = new SecuredMessage(sk, mak);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}
