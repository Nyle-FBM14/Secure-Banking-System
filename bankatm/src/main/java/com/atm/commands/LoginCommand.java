package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;

import com.atm.ATMModel;
import com.nyle.SecureBanking;
import com.nyle.SecuredMessage;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;

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

    @Override
    public void execute() {
        try {
            HashMap<MessageHeaders, String> credentials = new HashMap<MessageHeaders, String>();
            credentials.put(MessageHeaders.REQUESTTYPE, RequestTypes.LOGIN.toString());
            credentials.put(MessageHeaders.CARDNUM, cardNum);
            credentials.put(MessageHeaders.PIN, pin);
            SecuredMessage credsMessage = secure.generateCredentialsMessage(credentials);
            out.writeObject(credsMessage);
            out.flush();

            //assumes correct credentials for now
            SecuredMessage keys = (SecuredMessage) in.readObject();
            secure.getDerivedKeys(keys);
            model.setCredentials(cardNum, pin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
