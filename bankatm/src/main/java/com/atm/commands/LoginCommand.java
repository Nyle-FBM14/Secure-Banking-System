package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.atm.ATMModel;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.SecurityUtils;
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

    private void sendCredentials() throws Exception { //does not handle wrong credentials
        //send card number
        //cardNum = SecurityUtils.hashFunction(cardNum);
        Message message = new Message(RequestTypes.LOGIN, cardNum, 0, null, null);
        SecuredMessage sMessage = secure.encryptAndSignMessage(message);
        out.writeObject(sMessage);
        out.flush();
        
        //receive acknowledgment
        sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        System.out.println(message.getMessage());

        //send pin
        //pin = SecurityUtils.hashFunction(pin);
        message = new Message(RequestTypes.LOGIN, pin, 0, null, null);
        sMessage = secure.encryptAndSignMessage(message);
        out.writeObject(sMessage);
        out.flush();
        
        //receive acknowledgment
        sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        System.out.println(message.getMessage());
    }
    private void dhExchange() throws Exception {
        //send prime
        String p1 = SecurityUtils.generateLargePrime().toString();
        Message message = new Message(RequestTypes.LOGIN, p1, 0, null, null);
        out.writeObject(secure.encryptAndSignMessage(message));
        out.flush();

        //receive prime
        SecuredMessage sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);

        //generate DH keypair
        secure.generateDHKeyPair(p1, message.getMessage());

        //send atm public key
        sMessage = secure.generateDHPublicKeyMessage();
        out.writeObject(sMessage);
        out.flush();

        //receive bank public key
        sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);

        //generate masterkey
        secure.generateMasterKey(message.getMessage(), cardNum, pin);
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
        } catch (Exception e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
    }
}
