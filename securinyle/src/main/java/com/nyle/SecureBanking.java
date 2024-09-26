package com.nyle;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
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

public class SecureBanking {
    private SecretKey masterKey; //initially created with each new ATM connection
    private SecretKey masterSessionKey; //created with each client login - atm will not have this
    private SecretKey sessionKey; //derived from master session key
    private SecretKey macKey; //derived from master session key
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey publicKeyPartner;
    private ArrayList<String> usedNonces =  new ArrayList<String>();

    public SecureBanking() {
        KeyPair keypair = RSA.generateRSAkeypair();
        privateKey = keypair.getPrivate();
        publicKey = keypair.getPublic();
    }
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setpublicKeyPartner(PublicKey publicKeyPartner) {
        this.publicKeyPartner = publicKeyPartner;
    }

    public SecuredMessage generateDHPrimeMessage() { //maybe generalize A
        BigInteger prime = Utils.generateLargePrime();
        byte[] signedPrime = RSA.signDigitalSignature(privateKey, prime);
        return new SecuredMessage(Utils.serialize(prime), signedPrime);
    }
    public KeyPair generateDHKeyPair(SecuredMessage dhPrime, byte[] myPrime, boolean isBank) {
        BigInteger p1 = (BigInteger) Utils.deserialize(dhPrime.getMessage());
        if(RSA.verifyDigitalSignature(publicKeyPartner, p1, dhPrime.getMessageIntegrityAuthentication())) {
            BigInteger p2 = (BigInteger) Utils.deserialize(myPrime);
            DHParameterSpec dhSpecs;
            if(isBank) {
                dhSpecs = new DHParameterSpec(p1, p2, KeySizes.DH_PRIME.SIZE);
            }
            else {
                dhSpecs = new DHParameterSpec(p2, p1, KeySizes.DH_PRIME.SIZE);
            }
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
    public SecuredMessage generateDHPublicKeyMessage(PublicKey key) { //maybe generalize A
        byte[] signedKey = RSA.signDigitalSignature(privateKey, key);
        return new SecuredMessage(Utils.serialize(key), signedKey);
    }
    public void generateMasterKey(SecuredMessage senderDHPubKey, PrivateKey prKey) {
        PublicKey senderDHPublicKey = (PublicKey) Utils.deserialize(senderDHPubKey.getMessage());
        if(RSA.verifyDigitalSignature(publicKeyPartner, senderDHPublicKey, senderDHPubKey.getMessageIntegrityAuthentication())){
            try {
                KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                keyAgree.init(prKey);
                keyAgree.doPhase(senderDHPublicKey, true);
                byte[] sharedKey = keyAgree.generateSecret();
                MessageDigest hashFunction = MessageDigest.getInstance(Algorithms.HASH256.INSTANCE);
                masterKey = new SecretKeySpec(hashFunction.digest(sharedKey), Algorithms.AES.INSTANCE);
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
    public void generateMasterSessionKey(SecuredMessage message) { //unique to bank server
        HashMap<MessageHeaders, String> credentials = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(message.getMessage(), masterKey, Algorithms.AES.INSTANCE);
        if(SecurityUtils.verifyMac(credentials, message.getMessageIntegrityAuthentication(), masterKey)) {
            String base = credentials.get(MessageHeaders.CARDNUM) + credentials.get(MessageHeaders.PIN) + credentials.get(MessageHeaders.TIMESTAMP);
            try {
                PBEKeySpec spec = new PBEKeySpec(base.toCharArray(), Utils.generateSalt(), KeySizes.MASTERSESSIONKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                byte[] msk = factory.generateSecret(spec).getEncoded();
                masterSessionKey = new SecretKeySpec(msk, Algorithms.AES.INSTANCE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //return masterSessionKey;
    }

    public SecuredMessage deriveSessionAndMacKeysAndGenerateMessage() { //unique to bank server
        SecuredMessage message = null;
        try {
            //deriving keys
            PBEKeySpec spec = new PBEKeySpec(Utils.keyToString(masterSessionKey).toCharArray(), Utils.generateSalt(), KeySizes.MASTERSESSIONKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] sk = factory.generateSecret(spec).getEncoded();
            spec = new PBEKeySpec(Utils.keyToString(masterSessionKey).toCharArray(), Utils.generateSalt(), KeySizes.MASTERSESSIONKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
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

    public SecuredMessage encryptAndSignMessage(HashMap<MessageHeaders, String> message) {
        SecuredMessage sMessage = null;
        message.put(MessageHeaders.NONCE, Utils.generateNonce());
        try {
            byte[] encryptedMessage = SecurityUtils.encrypt(message, sessionKey, Algorithms.AES.INSTANCE);
            byte[] messageMac = SecurityUtils.makeMac(message, macKey);
            sMessage = new SecuredMessage(encryptedMessage, messageMac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sMessage;
    }
    @SuppressWarnings("unchecked")
    public HashMap<MessageHeaders, String> decryptAndVerifyMessage(SecuredMessage message) {
        HashMap<MessageHeaders, String> decryptedMessage = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(message.getMessage(), sessionKey, Algorithms.AES.INSTANCE);
        if(SecurityUtils.verifyMac(decryptedMessage, message.getMessageIntegrityAuthentication(), macKey) && !usedNonces.contains(decryptedMessage.get(MessageHeaders.NONCE))) {
            usedNonces.add(decryptedMessage.get(MessageHeaders.NONCE));
            decryptedMessage.remove(MessageHeaders.NONCE);
            return decryptedMessage;
        }
        return null;
    }
}
