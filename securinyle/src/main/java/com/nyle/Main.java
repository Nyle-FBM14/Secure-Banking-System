package com.nyle;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.SecretKey;

import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;

public class Main {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        SecretKey key = AES.generateKey();
        KeyPair keypairA = RSA.generateRSAkeypair();
        PublicKey puA = keypairA.getPublic();
        PrivateKey prA = keypairA.getPrivate();
        KeyPair keypairB = RSA.generateRSAkeypair();
        PublicKey puB = keypairB.getPublic();
        PrivateKey prB = keypairB.getPrivate();

        HashMap<MessageHeaders, String> mssg = new HashMap<MessageHeaders, String>();
        HashMap<MessageHeaders, String> decryptedMssg;
        byte[] encryptedMssg;
        mssg.put(MessageHeaders.REQUESTTYPE, RequestTypes.SECURE_CONNECTION.toString());
        mssg.put(MessageHeaders.CARDNUM, "1111222233337777");

        System.out.println("Original Message");
        System.out.println(mssg);
        System.out.println(Utils.serialize(mssg).length);

        System.out.println("\nAES Encryption");
        encryptedMssg = SecurityUtils.encrypt(mssg, key, "AES");
        System.out.println(Arrays.toString(encryptedMssg));

        System.out.println("\nAES Decryption");
        decryptedMssg = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(encryptedMssg, key, "AES");
        System.out.println(decryptedMssg);

        System.out.println("\nSign Digital Sig");
        byte[] sig = RSA.signDigitalSignature(prA, mssg);
        encryptedMssg = SecurityUtils.encrypt(mssg, puB, "RSA/ECB/PKCS1Padding");
        SecuredMessage sm = new SecuredMessage(encryptedMssg, sig);

        System.out.println("\nVerify Digital Sig");
        decryptedMssg = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(sm.getMessage(), prB, "RSA/ECB/PKCS1Padding");
        System.out.println(decryptedMssg);
        if(RSA.verifyDigitalSignature(puA, decryptedMssg, sm.getMessageIntegrityAuthentication()))
            System.out.println("Valid signature");
        else
            System.out.println("Invalid signature");
    }
}