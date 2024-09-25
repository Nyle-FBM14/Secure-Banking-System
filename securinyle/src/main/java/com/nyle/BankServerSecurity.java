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
    private PublicKey publicKeyReceiver; //make setter

    public BankServerSecurity() {
        KeyPair keypair = RSA.generateRSAkeypair();
        privateKey = keypair.getPrivate();
        publicKey = keypair.getPublic();
    }

    public SecuredMessage GenerateDHPrimeMessage() { //maybe generalize A
        BigInteger prime = Utils.getLargePrime();
        byte[] signedPrime = RSA.signDigitalSignature(privateKey, prime);
        return new SecuredMessage(Utils.serialize(prime), signedPrime);
    }
    public KeyPair generateDHKeyPair(SecuredMessage dhPrime, byte[] myPrime) {
        BigInteger p1 = (BigInteger) Utils.deserialize(dhPrime.getMessage());
        if(RSA.verifyDigitalSignature(publicKeyReceiver, p1, dhPrime.getMessageIntegrityAuthentication())) {
            BigInteger p2 = (BigInteger) Utils.deserialize(myPrime);
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
    public SecuredMessage GenerateDHPublicKeyMessage(PublicKey key) { //maybe generalize A
        byte[] signedKey = RSA.signDigitalSignature(privateKey, key);
        return new SecuredMessage(Utils.serialize(key), signedKey);
    }
    public void generateMasterKey(SecuredMessage senderDHPubKey, PrivateKey prKey) {
        PublicKey senderDHPublicKey = (PublicKey) Utils.deserialize(senderDHPubKey.getMessage());
        if(RSA.verifyDigitalSignature(publicKeyReceiver, senderDHPublicKey, senderDHPubKey.getMessageIntegrityAuthentication())){
            try {
                KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                keyAgree.init(prKey);
                keyAgree.doPhase(senderDHPublicKey, true);
                masterKey = keyAgree.generateSecret(Algorithms.AES.INSTANCE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SecuredMessage generateCredentialsMessage(HashMap<MessageHeaders, String> credentials) { //unique to atm
        byte[] encryptedMessage = SecurityUtils.encrypt(credentials, masterKey, Algorithms.AES.INSTANCE);
        byte[] mac = SecurityUtils.makeMac(credentials, masterKey);
        return new SecuredMessage(encryptedMessage, mac);
    }
    @SuppressWarnings("unchecked")
    public SecretKey generateMasterSessionKey(SecuredMessage message) { //unique to bank server
        HashMap<MessageHeaders, String> credentials = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(message.getMessage(), masterKey, Algorithms.AES.INSTANCE);
        if(SecurityUtils.verifyMac(credentials, message.getMessageIntegrityAuthentication(), masterKey)) {
            String base = credentials.get(MessageHeaders.CARDNUM) + credentials.get(MessageHeaders.PIN) + credentials.get(MessageHeaders.TIMESTAMP);
            try {
                PBEKeySpec spec = new PBEKeySpec(base.toCharArray(), Utils.getSalt(), KeySizes.MASTERKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                byte[] msk = factory.generateSecret(spec).getEncoded();
                masterSessionKey = new SecretKeySpec(msk, Algorithms.AES.INSTANCE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return masterSessionKey;
    }

    public SecuredMessage deriveSessionAndMacKeysAndGenerateMessage() { //unique to bank server
        SecuredMessage message = null;
        try {
            //deriving keys
            PBEKeySpec spec = new PBEKeySpec(Utils.keyToString(masterSessionKey).toCharArray(), Utils.getSalt(), KeySizes.MASTERKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] sk = factory.generateSecret(spec).getEncoded();
            spec = new PBEKeySpec(Utils.keyToString(masterSessionKey).toCharArray(), Utils.getSalt(), KeySizes.MASTERKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
            byte[] mak = factory.generateSecret(spec).getEncoded();
            //setting instance variables
            sessionKey = new SecretKeySpec(sk, Algorithms.AES.INSTANCE);
            macKey = new SecretKeySpec(mak, Algorithms.AES.INSTANCE);

            //creating message
            HashMap<MessageHeaders, String> keys =  new HashMap<MessageHeaders, String>();
            keys.put(MessageHeaders.SESSIONKEY, Utils.keyToString(sessionKey));
            keys.put(MessageHeaders.MACKEY, Utils.keyToString(macKey));
            byte[] encryptedMessage = SecurityUtils.encrypt(keys, masterKey, Algorithms.AES.INSTANCE);
            byte[] messageMac = SecurityUtils.makeMac(keys, masterKey);
            message = new SecuredMessage(encryptedMessage, messageMac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
    @SuppressWarnings("unchecked")
    public boolean getDerivedKeys(SecuredMessage message) { //unique to atm
        HashMap<MessageHeaders, String> keys = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(message.getMessage(), masterKey, Algorithms.AES.INSTANCE);
        if(SecurityUtils.verifyMac(keys, message.getMessageIntegrityAuthentication(), masterKey)) {
            sessionKey = AES.stringToKey(keys.get(MessageHeaders.SESSIONKEY));
            macKey = AES.stringToKey(keys.get(MessageHeaders.MACKEY));
            return true;
        }
        return false;
    }

    public SecuredMessage encryptAndSignMessage(Object message) {
        SecuredMessage sMessage = null;
        try {
            byte[] encryptedMessage = SecurityUtils.encrypt(message, sessionKey, Algorithms.AES.INSTANCE);
            byte[] messageMac = SecurityUtils.makeMac(message, macKey);
            sMessage = new SecuredMessage(encryptedMessage, messageMac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sMessage;
    }
    public Object decryptAndVerifyMessage(SecuredMessage message) {
        Object decryptedMessage = SecurityUtils.decrypt(message.getMessage(), sessionKey, Algorithms.AES.INSTANCE);
        if(SecurityUtils.verifyMac(decryptedMessage, message.getMessageIntegrityAuthentication(), macKey)) {
            return decryptedMessage;
        }
        return null;
    }
}
