package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.nyle.SecureBanking;
import com.nyle.SecuredMessage;
import com.nyle.enumerations.MessageHeaders;

public class LoginCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;

    public LoginCommand (ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) {
        this.in = in;
        this.out = out;
    }

    public boolean checkCredentials(SecuredMessage creds) {
        HashMap<MessageHeaders, String> credentials = secure.decryptAndVerifyMessage(creds);
        if(credentials == null)
            return false;
        BankUser user = bank.getBankUser(credentials.get(MessageHeaders.CARDNUM));
        return user.authenticate(credentials.get(MessageHeaders.PIN));
    }
    @Override
    public void execute() {
        try {
            SecuredMessage credentials = (SecuredMessage) in.readObject();
            if(!checkCredentials(credentials)){
                return;
            }
            secure.generateMasterSessionKey(credentials);
            SecuredMessage keys = secure.deriveSessionAndMacKeysAndGenerateMessage();
            out.writeObject(keys);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
