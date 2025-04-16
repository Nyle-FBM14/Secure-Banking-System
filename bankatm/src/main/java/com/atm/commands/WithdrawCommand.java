package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.RequestTypes;

public class WithdrawCommand implements Command{
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
            Message message = new Message(RequestTypes.WITHDRAW, null, Double.parseDouble(amount), null, null);
            SecuredMessage sMessage = secure.encryptAndSignMessage(message);
            out.writeObject(sMessage);
            out.flush();

            sMessage = (SecuredMessage) in.readObject();
            message = secure.decryptAndVerifyMessage(sMessage);
            System.out.println(message.getMessage());
        } catch (Exception e) {
            System.out.println("Withdraw failed");
        }
    }
    
}
