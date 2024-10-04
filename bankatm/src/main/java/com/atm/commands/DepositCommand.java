package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.atm.ATMModel;
import com.nyle.SecureBanking;
import com.nyle.SecuredMessage;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;
import com.nyle.enumerations.ResponseStatusCodes;

public class DepositCommand implements Command {
    private ATMModel model = ATMModel.getATMModelInstance();
    private String amount;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    
    public DepositCommand(ATMModel model, String amount, ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) {
        this.model = model;
        this.amount = amount;
        this.in = in;
        this.out = out;
        this.secure = secure;
    }

    @Override
    public void execute() {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.DEPOSIT.toString());
            request.put(MessageHeaders.CARDNUM, model.getCardNum());
            request.put(MessageHeaders.PIN, model.getPin());
            request.put(MessageHeaders.DEPOSITAMOUNT, amount);

            SecuredMessage message = secure.encryptAndSignMessage(request);
            out.writeObject(message);
            out.flush();

            message = (SecuredMessage) in.readObject();
            HashMap<MessageHeaders, String> response = secure.decryptAndVerifyMessage(message);
            System.out.println(response.get(MessageHeaders.RESPONSECODE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
