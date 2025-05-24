package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import com.bankserver.AtmHandler;
import com.bankserver.BankUser;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.RequestTypes;

public class WithdrawCommand implements Command {
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message message;
    private SecureBanking secure;
    private Logger logger;
    private BankUser user;

    public WithdrawCommand (ObjectInputStream in, ObjectOutputStream out, Message message, SecureBanking secure, Logger logger, BankUser user) {
        this.in = in;
        this.out = out;
        this.message = message;
        this.secure = secure;
        this.logger = logger;
        this.user = user;
    }

    @Override
    public void execute() {
        try {
            user.withdraw(message.getAmount());
            logger.info(user.getCardNum() + " withdrew $" + message.getAmount());
            
            message = new Message(RequestTypes.WITHDRAW, "Success", 0, null, null);
            SecuredMessage sMessage = secure.encryptAndSignMessage(message);
            out.writeObject(sMessage);
            out.flush();
        } catch (Exception e) {
            System.out.println("Withdraw failed.");
            e.printStackTrace();
        }

    }
}
