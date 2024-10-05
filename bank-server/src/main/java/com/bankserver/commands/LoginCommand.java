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
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    private SecuredMessage message;

    public LoginCommand (ObjectInputStream in, ObjectOutputStream out, SecureBanking secure, SecuredMessage message) {
        this.in = in;
        this.out = out;
        this.secure = secure;
        this.message = message;
    }

    public boolean checkCredentials() {
        HashMap<MessageHeaders, String> credentials = secure.decryptAndVerifyMessage(message);
        if(credentials == null)
            return false;
        BankUser user = bank.getBankUser(credentials.get(MessageHeaders.CARDNUM));
        return user.authenticate(credentials.get(MessageHeaders.PIN));
    }
    @Override
    public void execute() {
        try {
            if(!checkCredentials()){ //send response code
                return;
            }
            secure.generateMasterSessionKey(message);
            SecuredMessage keys = secure.deriveSessionAndMacKeysAndGenerateMessage();
            out.writeObject(keys);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
