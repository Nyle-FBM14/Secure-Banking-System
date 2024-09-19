package com.nyle;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import javax.crypto.Cipher;

public class RSA {
    public KeyPair generateRSAkeypair() {
        try{
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(2048);
            KeyPair keypair = keygen.generateKeyPair();
            return keypair;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public byte[] doubleEncrypt(PrivateKey pr, PublicKey pu, Object message) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            //encrypt with private key
            cipher.init(Cipher.ENCRYPT_MODE, pr);
            byte[] prEncrypted = cipher.doFinal(Utils.serialize(message));

            //encrypt with public key
            cipher.init(Cipher.ENCRYPT_MODE, pu);
            return cipher.doFinal(prEncrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public Object doubleDecrypt(PrivateKey pr, PublicKey pu, Object encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            //decrypt with private key
            cipher.init(Cipher.DECRYPT_MODE, pu);
            byte[] puDecrypted = cipher.doFinal(Utils.serialize(encryptedMessage));

            //decrypt with public key
            cipher.init(Cipher.DECRYPT_MODE, pr);
            return Utils.deserialize(cipher.doFinal(puDecrypted));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] signDigitalSignature(PrivateKey key, Object message) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(key);
            sig.update(Utils.serialize(message));
            return sig.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean verifyDigitalSignature(PublicKey key, Object message, byte[] receivedSig) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(key);
            sig.update(Utils.serialize(message));
            return sig.verify(receivedSig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
