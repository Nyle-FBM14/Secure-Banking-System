package com.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import com.security.enumerations.Algorithms;
import com.security.enumerations.KeySizes;

public class SecurityUtils {

    /*
     * Note to self: ByteArrayOutputStream writes data into a byte array
     * ObjectOutputStream serializes an object into a stream of bytes
     * It writes into ByteArrayOutputStream
     * ByteArrayOutputStream calls toByteArray() to return byte array
    */
    public static byte[] serialize(Object object) {
        try {
            ByteArrayOutputStream byteArrMaker = new ByteArrayOutputStream();
            ObjectOutputStream serializer = new ObjectOutputStream(byteArrMaker);
            serializer.writeObject(object);
            serializer.flush();
            return byteArrMaker.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
     * Note to self: ByteArrayInputStream takes a byte array and turns it into stream
     * ObjectInputStream reads from that stream and deserializes it
    */
    public static Object deserialize(byte[] buffer) {
        try {
            ByteArrayInputStream arrayToStream = new ByteArrayInputStream(buffer);
            ObjectInputStream deserializer = new ObjectInputStream(arrayToStream);
            return deserializer.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String keyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        return random.generateSeed(16);
    }
    public static BigInteger generateLargePrime() {
        SecureRandom random = new SecureRandom();
        return BigInteger.probablePrime(KeySizes.DH_PRIME.SIZE, random);
    }
    public static String generateNonce() {
        SecureRandom random = new SecureRandom();
        int nonce = random.nextInt();
        return Integer.toString(nonce);
    }
    public static String nonceFunction(String nonceString) {
        long nonce = Long.parseLong(nonceString);
        nonce = ((nonce + 37) * 7) / (nonce + 3);
        return Long.toString(nonce);
    }
    public static String hashFunction(String s) {
        StringBuilder hashString = new StringBuilder();
        try {
            MessageDigest hashFunction = MessageDigest.getInstance(Algorithms.HASH256.INSTANCE);
            byte[] hash = hashFunction.digest(s.getBytes());

            for(byte b : hash) {
                hashString.append(String.format("%02X", b));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashString.toString();
    }

    public static byte[] encrypt(Object message, Key key, String instance) {
        try{
            Cipher cipher = Cipher.getInstance(instance);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(serialize(message));
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static Object decrypt(byte[] encryptedMessage, Key key, String instance) {
        try {
            Cipher cipher = Cipher.getInstance(instance);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return deserialize(cipher.doFinal(encryptedMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encryptString(String message, SecretKey key, String instance) {
        try{
            Cipher cipher = Cipher.getInstance(instance);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static String decryptString(String encryptedMessage, SecretKey key, String instance) {
        try{
            Cipher cipher = Cipher.getInstance(instance);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] makeMac(Object message, SecretKey macKey) {
        try {
            Mac mac = Mac.getInstance(Algorithms.MAC_HASH.INSTANCE);
            mac.init(macKey);
            byte[] macBytes = mac.doFinal(serialize(message));
            return macBytes;
            //return Base64.getEncoder().encodeToString(macBytes); //for String - import java.util.Base64;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verifyMac(Object message, byte[] receivedMac, SecretKey macKey) {
        byte[] generatedMac = makeMac(message, macKey);
        return MessageDigest.isEqual(receivedMac, generatedMac);
    }
}
