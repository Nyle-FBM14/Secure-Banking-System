package com.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import com.security.enumerations.Algorithms;
import com.security.enumerations.KeySizes;

public class RSA {
    public static KeyPair generateRSAkeypair() {
        try{
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(KeySizes.RSA_KEYGEN.SIZE);
            KeyPair keypair = keygen.generateKeyPair();
            return keypair;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] doubleEncrypt(PrivateKey pr, PublicKey pu, Object message) {
        try {
            Cipher cipher = Cipher.getInstance(Algorithms.RSA.INSTANCE);

            //encrypt with private key
            cipher.init(Cipher.ENCRYPT_MODE, pr);
            byte[] prEncrypted = cipher.doFinal(SecurityUtils.serialize(message));

            //encrypt with public key
            cipher.init(Cipher.ENCRYPT_MODE, pu);
            return cipher.doFinal(prEncrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Object doubleDecrypt(PrivateKey pr, PublicKey pu, Object encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance(Algorithms.RSA.INSTANCE);

            //decrypt with private key
            cipher.init(Cipher.DECRYPT_MODE, pu);
            byte[] puDecrypted = cipher.doFinal(SecurityUtils.serialize(encryptedMessage));

            //decrypt with public key
            cipher.init(Cipher.DECRYPT_MODE, pr);
            return SecurityUtils.deserialize(cipher.doFinal(puDecrypted));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] signDigitalSignature(PrivateKey key, Object message) {
        try {
            Signature sig = Signature.getInstance(Algorithms.RSA_SIGNATURE.INSTANCE);
            sig.initSign(key);
            sig.update(SecurityUtils.serialize(message));
            return sig.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean verifyDigitalSignature(PublicKey key, Object message, byte[] receivedSig) {
        try {
            Signature sig = Signature.getInstance(Algorithms.RSA_SIGNATURE.INSTANCE);
            sig.initVerify(key);
            sig.update(SecurityUtils.serialize(message));
            return sig.verify(receivedSig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static PublicKey stringToPublicKey(String key){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key)));
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
