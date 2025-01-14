package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.atm.ATMModel;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.RequestTypes;

public class CheckBalanceCommand implements Command {
    private ATMModel model = ATMModel.getATMModelInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;

    public CheckBalanceCommand(ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.secure = secure;
    }

    @Override
    public void execute() {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.CHECK_BALANCE.toString());
            request.put(MessageHeaders.CARDNUM, model.getCardNum());
            request.put(MessageHeaders.PIN, model.getPin());
            SecuredMessage message = secure.encryptAndSignMessage(request, true);
            out.writeObject(message);
            out.flush();

            message = (SecuredMessage) in.readObject();
            HashMap<MessageHeaders, String> response = secure.decryptAndVerifyMessage(message, true);
            System.out.println(response.get(MessageHeaders.RESPONSECODE));

            System.out.println(response.get(MessageHeaders.RESPONSE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
