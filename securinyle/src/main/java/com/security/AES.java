package com.security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

import com.security.enumerations.KeySizes;

public class AES {
    
    public static SecretKey generateKey() {
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KeySizes.AES.SIZE);
            return keyGen.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SecretKey stringToKey(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKey, "AES");
    }
}
