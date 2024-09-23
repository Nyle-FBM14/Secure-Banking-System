package com.nyle;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHGenParameterSpec;
import javax.crypto.spec.DHParameterSpec;

import com.nyle.enumerations.Algorithms;
import com.nyle.enumerations.KeySizes;

public class BankServerSecurity {
    private SecretKey masterKey; //initially created with each new ATM connection
    private SecretKey masterSessionKey; //created with each client login - a session key for encryption key and a mac key will be derived from this
    private SecretKey sessionKey;
    private SecretKey macKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey publicKeyATM;

    public BankServerSecurity() {
        masterKey = AES.generateKey();

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
    public SecretKey generateMasterSessionKey(SecuredMessage atmPubKey, PrivateKey prKey) {
        if(RSA.verifyDigitalSignature(publicKeyATM, atmPubKey.getMessage(), atmPubKey.getMessageIntegrityAuthentication())){
            try {
                PublicKey atmKey = (PublicKey) Utils.deserialize(atmPubKey.getMessage());
                KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                keyAgree.init(prKey);
                keyAgree.doPhase(atmKey, true);
                masterSessionKey = keyAgree.generateSecret(Algorithms.AES.INSTANCE);
                return masterSessionKey;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
