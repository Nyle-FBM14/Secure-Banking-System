package com.security;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.security.enumerations.Algorithms;
import com.security.enumerations.KeySizes;
import com.security.enumerations.RequestTypes;

public class SecureBanking {
    private SecretKey initialKey; //initially created with each new ATM connection
    private SecretKey masterKey;
    private SecretKey sessionKey; //derived from master key
    private SecretKey macKey; //derived from master key
    private KeyPair keypair; //for dh

    private boolean isLoggedIn = false;
    @SuppressWarnings("unused")
    private ArrayList<String> usedNonces =  new ArrayList<String>();

    public void setInitialKey(SecretKey initialKey) {
        this.initialKey = initialKey;
    }
    public SecretKey getInitialKey() {
        return initialKey;
    }
    /*
    public SecuredMessage generateDHPrimeMessage() {
        BigInteger prime = Utils.generateLargePrime();
        byte[] signedPrime = RSA.signDigitalSignature(privateKey, prime);
        return new SecuredMessage(Utils.serialize(prime), signedPrime);
    } */
    public boolean generateDHKeyPair(String p1, String p2) {
        BigInteger p = new BigInteger(p1);
        BigInteger g = new BigInteger(p2);

        DHParameterSpec dhSpecs = new DHParameterSpec(p, g, KeySizes.DH_KEY.SIZE);
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
            keyGen.initialize(dhSpecs);
            keypair = keyGen.generateKeyPair();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public SecuredMessage generateDHPublicKeyMessage() {
        Message message = new Message(RequestTypes.LOGIN, SecurityUtils.keyToString(keypair.getPublic()), 0, null, null);
        return encryptAndSignMessage(message);
    }
    public boolean generateMasterKey(String puKeyString, String cardNum, String pin) {
        try {
            MessageDigest hashFunction = MessageDigest.getInstance(Algorithms.HASH256.INSTANCE);

            //convert key string to a public key
            KeyFactory keyFactory = KeyFactory.getInstance("DH");
            PublicKey puKey =  keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(puKeyString)));

            //completing DH exchange and generating shared key
            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(keypair.getPrivate());
            keyAgree.doPhase(puKey, true);
            byte[] sharedKey = keyAgree.generateSecret();

            //mixing it up fr
            sharedKey = hashFunction.digest(sharedKey);
            String base = Arrays.toString(sharedKey) + cardNum + pin;
            sharedKey = hashFunction.digest(base.getBytes());

            //generating masterkey
            masterKey = new SecretKeySpec(sharedKey, Algorithms.AES.INSTANCE);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /*
    public SecuredMessage generateCredentialsMessage(HashMap<MessageHeaders, String> credentials) { //unique to atm
        System.out.println(credentials);
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
    } */

    public SecuredMessage deriveSessionAndMacKeysAndGenerateMessage() { //bank server
        SecuredMessage sMessage = null;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            //session key
            PBEKeySpec spec = new PBEKeySpec(SecurityUtils.keyToString(masterKey).toCharArray(), SecurityUtils.generateSalt(), KeySizes.MASTERKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
            sessionKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), Algorithms.AES.INSTANCE);
            //mac key
            spec = new PBEKeySpec(SecurityUtils.keyToString(masterKey).toCharArray(), SecurityUtils.generateSalt(), KeySizes.MASTERKEY_ITERATIONS.SIZE, KeySizes.AES.SIZE);
            macKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), Algorithms.AES.INSTANCE);

            //creating message
            String keys = SecurityUtils.keyToString(sessionKey) + " " + SecurityUtils.keyToString(macKey);
            Message message = new Message(RequestTypes.LOGIN, keys, 0, null, null);
            byte[] encryptedMessage = SecurityUtils.encrypt(message, masterKey, Algorithms.AES.INSTANCE);
            byte[] messageMac = SecurityUtils.makeMac(message, masterKey);
            sMessage = new SecuredMessage(encryptedMessage, messageMac);

            //logged in
            isLoggedIn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sMessage;
    }

    public boolean getDerivedKeys(SecuredMessage sMessage) { //atm
        Message message = (Message) SecurityUtils.decrypt(sMessage.getMessage(), masterKey, Algorithms.AES.INSTANCE);
        if(SecurityUtils.verifyMac(message, sMessage.getMessageIntegrityAuthentication(), masterKey)) {
            String[] keys = message.getMessage().split(" ");
            sessionKey = AES.stringToKey(keys[0]);
            macKey = AES.stringToKey(keys[1]);

            isLoggedIn = true;
            return true;
        }
        return false;
    }

    public SecuredMessage encryptAndSignMessage(Message message) {
        SecuredMessage sMessage = null;
        SecretKey key, mac;
        if(isLoggedIn) {
            key = sessionKey;
            mac = macKey;
        }
        else {
            key = initialKey;
            mac = initialKey;
        }
        byte[] encryptedMessage = SecurityUtils.encrypt(message, key, Algorithms.AES.INSTANCE);
        byte[] messageMac = SecurityUtils.makeMac(message, mac);
        sMessage = new SecuredMessage(encryptedMessage, messageMac);
        
        return sMessage;
    }
    
    public Message decryptAndVerifyMessage(SecuredMessage message) {
        SecretKey key, mac;
        if(isLoggedIn) {
            key = sessionKey;
            mac = macKey;
        }
        else {
            key = initialKey;
            mac = initialKey;
        }
        Message decryptedMessage = (Message) SecurityUtils.decrypt(message.getMessage(), key, Algorithms.AES.INSTANCE);
        if(SecurityUtils.verifyMac(decryptedMessage, message.getMessageIntegrityAuthentication(), mac)) { // && !usedNonces.contains(decryptedMessage.get(MessageHeaders.NONCE))
            //usedNonces.add(decryptedMessage.get(MessageHeaders.NONCE));
            //decryptedMessage.remove(MessageHeaders.NONCE);
            return decryptedMessage;
        }
        return null;
    }

    public void resetSession() {
        masterKey = null;
        sessionKey = null;
        macKey = null;
        isLoggedIn = false;
    }
}
