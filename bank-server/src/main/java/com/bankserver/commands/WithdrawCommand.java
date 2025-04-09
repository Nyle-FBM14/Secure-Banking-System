package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.RequestTypes;
import com.security.enumerations.ResponseStatusCodes;

public class WithdrawCommand implements Command {
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message message;
    private BankUser user;
    private SecureBanking secure;

    public WithdrawCommand (ObjectInputStream in, ObjectOutputStream out, Message message, BankUser user, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.message = message;
        this.user = user;
        this.secure = secure;
    }

    @Override
    public void execute() {
        try {
            user.withdraw(message.getAmount());
            
            message = new Message(RequestTypes.WITHDRAW, "Success", 0, null, null, null);
            SecuredMessage sMessage = secure.encryptAndSignMessage(message);
            out.writeObject(sMessage);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
