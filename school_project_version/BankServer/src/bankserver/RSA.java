/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankserver;

/**
 *
 * @author nmelegri
 */

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.*;

public class RSA {
    private PrivateKey private_key;
    private PublicKey public_key;
    private Signature sig;
    
    public RSA() {
        try{
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(2048);
            KeyPair key_pair = keygen.generateKeyPair();
            private_key = key_pair.getPrivate();
            public_key = key_pair.getPublic();
            sig = Signature.getInstance("SHA256withRSA");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public String encode(byte[] messageBytes) {
        return Base64.getEncoder().encodeToString(messageBytes);
    }
    public byte[] decode(String message) {
        return Base64.getDecoder().decode(message);
    }
    public String encryptPrivate(String message){
        try{
            byte[] messageBytes = message.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, private_key);

            return encode(cipher.doFinal(messageBytes));
        } catch (Exception e){
            e.printStackTrace();
        }
        
        return "Encryption failed.";
    }
    public String encryptPublic(String message, PublicKey key){
        try{
            byte[] messageBytes = message.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return encode(cipher.doFinal(messageBytes));
        } catch (Exception e){
            e.printStackTrace();
        }
        
        return "Encryption failed.";
    }
    //first encrypts with sender's private key, then encrypts with receiver's public key
    public String doubleEncrypt(String message, PublicKey clientKey){
        String encrypt1 = "", encrypt2 = "";
        try{
            //encrypt with private key
            encrypt1 = encryptPrivate(message);

            //halving first encryption into 2 parts 
            int middle = encrypt1.length()/2;
            String[] halves = {encrypt1.substring(0, middle), encrypt1.substring(middle)};

            //encrypt with public key
            halves[0] = encryptPublic(halves[0], clientKey);
            halves[1] = encryptPublic(halves[1], clientKey);

            encrypt2 = halves[0] + " " + halves[1];
        } catch (Exception e){
            e.printStackTrace();
        }

        return encrypt2;
    }
    public String decryptPrivate(String encryptedMessage){
        if(encryptedMessage == null)
            return "";
        try{
            byte[] encryptedBytes = decode(encryptedMessage);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, private_key);
            return new String(cipher.doFinal(encryptedBytes));
        } catch(Exception e){
            //e.printStackTrace();
        }
        
        return "";
    }
    public String decryptPublic(String encryptedMessage, PublicKey key){
        try{
            byte[] encryptedBytes = decode(encryptedMessage);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encryptedBytes));
        } catch (Exception e){
            e.printStackTrace();
        }
        
        return "Decryption failed.";
    }
    public String doubleDecrypt(String message, PublicKey key){
        String[] halves = message.split(" ");
        //decrypt halves with private key
        halves[0] = decryptPrivate(halves[0]);
        halves[1] = decryptPrivate(halves[1]);

        String decrypt1 = halves[0] + halves[1];
        
        //decrypt combined halves with public key
        String decrypt2 = decryptPublic(decrypt1, key);
        
        return decrypt2;
    }
    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(public_key.getEncoded());
    }
    public PublicKey getPublicKeyNotEncoded() {
        return public_key;
    }
    public String PublicKeyString(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public PublicKey stringToPublicKey(String key){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key)));
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    //SIGNATURE SECTION
    public void initializeSign() {
        try {
            sig.initSign(private_key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeVerify() {
        try {
            sig.initVerify(public_key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSig(String message) {
        byte[] data = message.getBytes();

        try {
            sig.update(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] signSig() {
        byte[] signed = null;
        try{
            signed = sig.sign();
        } catch(Exception e){
            e.printStackTrace();
        }
        return signed;
    }

    public boolean verifySig(byte[] signature) {
        boolean verified = false;
        try{
            sig.verify(signature);
        } catch(Exception e){
            e.printStackTrace();
        }
        return verified;
    }
}
