package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.atm.ATMModel;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.RequestTypes;

public class LogoutCommand implements Command{
    private ATMModel model = ATMModel.getATMModelInstance();
    private String amount;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    
    public LogoutCommand(String amount, ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) {
        this.amount = amount;
        this.in = in;
        this.out = out;
        this.secure = secure;
    }

    @Override
    public void execute() {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.LOGOUT.toString());
            SecuredMessage message = secure.encryptAndSignMessage(request, true);
            out.writeObject(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //secure banking needs a way to reset session keys
    }
    
}
