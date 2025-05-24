/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atm1;

/**
 *
 * @author nmelegri
 */

import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    byte[] x = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    private IvParameterSpec iv = new IvParameterSpec(x);

    public SecretKey generateKey() {
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // 128-bit key size
            return keyGen.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getKeyString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public SecretKey stringToKey(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKey, "AES");
    }
    // Method to encrypt using master key KA
    public String encrypt(String message, SecretKey key) {
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return "Encrypt failed.";
    }
    // Method to decrypt using master key KA
    public String decrypt(String encryptedMessage, SecretKey key) {
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return "Decryption failed.";
    }
    
    //encrypts with encryption key derived from master secret
    //adds MAC verification
    public String encrypt_verify(String message, SecretKey key, SecretKey macKey) {
        try{
            //generating MAC
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(macKey);
            byte[] macBytes = mac.doFinal(message.getBytes());
            String macString = Base64.getEncoder().encodeToString(macBytes);
            
            String output = macString + " " + message;
            //encrypting message
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedBytes = cipher.doFinal(output.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return "Encrypt failed.";
    }
    
    //decrypts with encryption key derived from master secret
    //verifies MAC
    public String decrypt_verify(String encryptedMessage, SecretKey key, SecretKey macKey) {
        try{
            //decrypting message
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            String decryptedResponse = new String(decryptedBytes);
            
            //split message from MAC
            String[] response = decryptedResponse.split(" ", 2);
            
            //verify mac
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(macKey);
            byte[] macBytes = Base64.getDecoder().decode(response[0]);
            byte[] generatedMacBytes = mac.doFinal(response[1].getBytes());
            
            if(MessageDigest.isEqual(macBytes, generatedMacBytes)){
                System.out.println("Valid MAC");
            }
            else{
                return "Invalid MAC";
            }
            
            return response[1];
            
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return "Decryption failed.";
    }
}
