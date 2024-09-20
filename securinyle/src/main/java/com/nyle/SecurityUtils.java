package com.nyle;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import com.nyle.enumerations.Algorithms;

public class SecurityUtils {
    /*
     * Note to self: ByteArrayOutputStream writes data into a byte array
     * ObjectOutputStream serializes an object into a stream of bytes
     * It writes into ByteArrayOutputStream
     * ByteArrayOutputStream calls toByteArray() to return byte array
     */
    public static byte[] encrypt(Object message, Key key, String instance) {
        try{
            Cipher cipher = Cipher.getInstance(instance);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(Utils.serialize(message));
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /*
     * Note to self: ByteArrayInputStream takes a byte array and turns it into stream
     * ObjectInputStream reads from that stream and deserializes it
     */
    public static Object decrypt(byte[] encryptedMessage, Key key, String instance) {
        try {
            Cipher cipher = Cipher.getInstance(instance);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return Utils.deserialize(cipher.doFinal(encryptedMessage));
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
            byte[] macBytes = mac.doFinal(Utils.serialize(message));
            return macBytes;
            //return Base64.getEncoder().encodeToString(macBytes); //for String - import java.util.Base64;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verifyMac(Object message, byte[] receivedMac, SecretKey macKey) {
        byte[] generatedMac = makeMac(message, macKey);
        return generatedMac.equals(receivedMac);
    }
}
