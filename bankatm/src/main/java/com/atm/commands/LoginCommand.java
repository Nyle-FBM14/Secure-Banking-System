package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;

import com.nyle.SecureBanking;
import com.nyle.SecuredMessage;
import com.nyle.enumerations.MessageHeaders;

public class LoginCommand implements Command{
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    private HashMap<MessageHeaders, String> credentials;

    
    @Override
    public void execute() {
        try {
            SecuredMessage credsMessage = secure.generateCredentialsMessage(credentials);
            out.writeObject(credsMessage);
            out.flush();

            SecuredMessage keys = (SecuredMessage) in.readObject();
            secure.getDerivedKeys(keys);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
