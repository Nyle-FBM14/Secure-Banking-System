package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.bankserver.AtmHandler;
import com.bankserver.Bank;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.SecurityUtils;
import com.security.enumerations.RequestTypes;

public class LoginCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    private Message message;

    public LoginCommand(ObjectInputStream in, ObjectOutputStream out, SecureBanking secure, Message message) {
        this.in = in;
        this.out = out;
        this.secure = secure;
        this.message = message;
    }

    private String[] receiveCredentials() throws Exception {
        SecuredMessage sMessage;
        //receive cardnum
        String cardNum = message.getMessage();

        //check if user exists
        AtmHandler.user = bank.getBankUser(cardNum);
        if(AtmHandler.user == null) {
            message = new Message(RequestTypes.LOGIN, "No user found.", 0, null, null);
        }
        else{
            message = new Message(RequestTypes.LOGIN, "Acknowledged.", 0, null, null);
        }
        sMessage = secure.encryptAndSignMessage(message);
        out.writeObject(sMessage);
        out.flush();

        //receive pin
        sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        String pin = message.getMessage();

        //validate user
        if(AtmHandler.user.authenticate(pin)) {
            message = new Message(RequestTypes.LOGIN, "Acknowledged.", 0, null, null);
        }
        else {
            message = new Message(RequestTypes.LOGIN, "Invalid password.", 0, null, null);
        }
        sMessage = secure.encryptAndSignMessage(message);
        out.writeObject(sMessage);
        out.flush();

        String[] credentials = {cardNum, pin};
        return credentials;
    }
    private void dhExchange(String cardNum, String pin) throws Exception {
        //receive prime
        SecuredMessage sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        String p1 = message.getMessage();

        //send prime
        String p2 = SecurityUtils.generateLargePrime().toString();
        message = new Message(RequestTypes.LOGIN, p2, 0, null, null);
        out.writeObject(secure.encryptAndSignMessage(message));
        out.flush();

        //generate DH keypair
        secure.generateDHKeyPair(p1, p2);

        //receive atm public key
        sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        
        //send bank public key
        sMessage = secure.generateDHPublicKeyMessage();
        out.writeObject(sMessage);
        out.flush();

        //generate masterkey
        secure.generateMasterKey(message.getMessage(), cardNum, pin);
    }
    private void generateAndSendSessionKeys() throws Exception {
        SecuredMessage sMessage = secure.deriveSessionAndMacKeysAndGenerateMessage();
        out.writeObject(sMessage);
        out.flush();
    }
    @Override
    public void execute() {
        try {
            String[] credentials = receiveCredentials();
            dhExchange(credentials[0], credentials[1]);
            generateAndSendSessionKeys();
        } catch (Exception e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
    }
}
