package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.bankserver.BankUser;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.RequestTypes;

public class DepositCommand implements Command{
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message message;
    BankUser user;
    private SecureBanking secure;

    public DepositCommand (ObjectInputStream in, ObjectOutputStream out, Message message, BankUser user, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.message = message;
        this.user = user;
        this.secure = secure;
    }

    @Override
    public void execute() {
        try {
            user.deposit(message.getAmount());

            message = new Message(RequestTypes.DEPOSIT, "Success", 0, null, null, null);
            SecuredMessage sMessage = secure.encryptAndSignMessage(message);
            out.writeObject(sMessage);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
