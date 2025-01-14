package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.atm.ATMModel;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.RequestTypes;

public class WithdrawCommand implements Command{
    private ATMModel model = ATMModel.getATMModelInstance();
    private String amount;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;

    public WithdrawCommand(String amount, ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) {
        this.amount = amount;
        this.in = in;
        this.out = out;
        this.secure = secure;
    }

    @Override
    public void execute() {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.WITHDRAW.toString());
            request.put(MessageHeaders.CARDNUM, model.getCardNum());
            request.put(MessageHeaders.PIN, model.getPin());
            request.put(MessageHeaders.WITHDRAWAMOUNT, amount);
            
            SecuredMessage message = secure.encryptAndSignMessage(request, true);
            out.writeObject(request);
            out.flush();

            message = (SecuredMessage) in.readObject();
            HashMap<MessageHeaders, String> response = secure.decryptAndVerifyMessage(message, true);
            System.out.println(response.get(MessageHeaders.RESPONSECODE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
