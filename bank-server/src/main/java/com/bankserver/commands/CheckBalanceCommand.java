package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import com.bankserver.AtmHandler;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.RequestTypes;

public class CheckBalanceCommand implements Command {
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message message;
    private SecureBanking secure;
    private Logger logger;

    public CheckBalanceCommand (ObjectInputStream in, ObjectOutputStream out, Message message, SecureBanking secure, Logger logger) {
        this.in = in;
        this.out = out;
        this.message = message;
        this.secure = secure;
        this.logger = logger;
    }
    @Override
    public void execute() {
        try {
            message = new Message(RequestTypes.CHECK_BALANCE, null, AtmHandler.user.checkBalance(), null, null);
            SecuredMessage sMessage = secure.encryptAndSignMessage(message);
            out.writeObject(sMessage);
            out.flush();
            logger.info(AtmHandler.user.getCardNum() + " checked their balance.");
        } catch (Exception e) {
            System.out.println("Check balance failed.");
            e.printStackTrace();
        }
    }
}
