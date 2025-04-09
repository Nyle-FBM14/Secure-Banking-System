package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.HashMap;

import com.atm.ATMModel;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.SecurityUtils;
import com.security.Utils;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.RequestTypes;

public class LoginCommand implements Command{
    private ATMModel model = ATMModel.getATMModelInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    private String cardNum, pin;

    public LoginCommand(ObjectInputStream in, ObjectOutputStream out, SecureBanking secure, String cardNum, String pin) {
        this.in = in;
        this.out = out;
        this.secure = secure;
        this.cardNum = cardNum;
        this.pin = pin;
    }

    private void sendCredentials() throws Exception {
        //send card number
        cardNum = SecurityUtils.hashFunction(cardNum);
        Message message = new Message(RequestTypes.LOGIN, cardNum, 0, null, "", null);
        SecuredMessage sMessage = secure.encryptAndSignMessage(message);
        out.writeObject(sMessage);
        out.flush();
        
        //receive acknowledgment
        sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        System.out.println(message.getMessage());

        //send pin
        pin = SecurityUtils.hashFunction(pin);
        message = new Message(RequestTypes.LOGIN, pin, 0, null, "", null);
        sMessage = secure.encryptAndSignMessage(message);
        out.writeObject(sMessage);
        out.flush();
        
        //receive acknowledgment
        sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        System.out.println(message.getMessage());
    }
    private void dhExchange() throws Exception {
        //generate DH keypair
        secure.generateDHKeyPair(cardNum, pin);

        //send atm public key
        SecuredMessage sMessage = secure.generateDHPublicKeyMessage();
        out.writeObject(sMessage);
        out.flush();

        //receive bank public key
        sMessage = (SecuredMessage) in.readObject();
        Message message = secure.decryptAndVerifyMessage(sMessage);
        PublicKey bankPuk = (PublicKey) message.getKey();

        //generate masterkey
        secure.generateMasterKey(bankPuk);
    }
    private void getSessionKeys() throws Exception {
        SecuredMessage sMessage = (SecuredMessage) in.readObject();
        secure.getDerivedKeys(sMessage);
    }
    @Override
    public void execute() {
        try {
            sendCredentials();
            dhExchange();
            getSessionKeys();
            model.setCredentials(cardNum, pin);
            System.out.println("got keys");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
