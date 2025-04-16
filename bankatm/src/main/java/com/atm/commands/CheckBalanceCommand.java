package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.atm.ATMModel;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
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
            Message message = new Message(RequestTypes.CHECK_BALANCE, null, 0, null, null);
            SecuredMessage sMessage = secure.encryptAndSignMessage(message);
            out.writeObject(sMessage);
            out.flush();

            sMessage = (SecuredMessage) in.readObject();
            message = secure.decryptAndVerifyMessage(sMessage);
            model.setBalance(String.valueOf(message.getAmount()));
        } catch (Exception e) {
            System.out.println("Check balance failed");
        }
    }
    
}
