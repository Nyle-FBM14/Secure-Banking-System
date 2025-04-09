package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.RequestTypes;

public class LoginCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    private Message message;
    private BankUser user;

    public LoginCommand(ObjectInputStream in, ObjectOutputStream out, SecureBanking secure, Message message, BankUser user) {
        this.in = in;
        this.out = out;
        this.secure = secure;
        this.message = message;
        this.user = user;
    }

    private String[] receiveCredentials() throws Exception {
        SecuredMessage sMessage;
        //receive cardnum
        String cardNum = message.getMessage();

        //check if user exists
        user = bank.getBankUser(cardNum);
        if(user == null) {
            message = new Message(RequestTypes.LOGIN, "No user found.", 0, null, null, null);
        }
        else{
            message = new Message(RequestTypes.LOGIN, "Acknowledged.", 0, null, null, null);
        }
        sMessage = secure.encryptAndSignMessage(message);
        out.writeObject(sMessage);
        out.flush();

        //receive pin
        sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        String pin = message.getMessage();

        //validate user
        if(user.authenticate(pin)) {
            message = new Message(RequestTypes.LOGIN, "Acknowledged.", 0, null, null, null);
        }
        else {
            message = new Message(RequestTypes.LOGIN, "Invalid password.", 0, null, null, null);
        }
        sMessage = secure.encryptAndSignMessage(message);
        out.writeObject(sMessage);
        out.flush();

        String[] credentials = {cardNum, pin};
        return credentials;
    }
    private void dhExchange(String cardNum, String pin) throws Exception {
        //receive atm public key
        SecuredMessage sMessage = (SecuredMessage) in.readObject();
        message = secure.decryptAndVerifyMessage(sMessage);
        PublicKey atmPuK = (PublicKey) message.getKey();
        
        //generate DH keypair
        secure.generateDHKeyPair(cardNum, pin);
        //send bank public key
        sMessage = secure.generateDHPublicKeyMessage();
        out.writeObject(sMessage);
        out.flush();

        //generate masterkey
        secure.generateMasterKey(atmPuK);
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
            e.printStackTrace();
        }
    }
}
