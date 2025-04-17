package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import com.bankserver.AtmHandler;
import com.bankserver.Bank;
import com.security.Message;
import com.security.SecureBanking;
import com.security.enumerations.RequestTypes;

public class LogoutCommand implements Command {
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message message;
    private SecureBanking secure;
    private Logger logger;

    public LogoutCommand (ObjectInputStream in, ObjectOutputStream out, Message message, SecureBanking secure, Logger logger) {
        this.in = in;
        this.out = out;
        this.message = message;
        this.secure = secure;
        this.logger = logger;
    }
    @Override
    public void execute() {
        try {
            secure.resetSession();
            logger.info(AtmHandler.user.getCardNum() + " logged out.");
            AtmHandler.user = null;

            message = new Message(RequestTypes.LOGOUT, "Logout successful.", 0, null, null);
            out.writeObject(secure.encryptAndSignMessage(message));
            out.flush();
            Bank bank = Bank.getBankInstance();
            bank.writeClients();
        } catch (Exception e) {
            System.out.println("Logout failed.");
            e.printStackTrace();
        }
    }
}
