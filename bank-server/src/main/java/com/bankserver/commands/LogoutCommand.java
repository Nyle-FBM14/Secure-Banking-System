package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.bankserver.AtmHandler;
import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.security.Message;
import com.security.SecureBanking;

public class LogoutCommand implements Command {
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    @SuppressWarnings("unused")
    private ObjectOutputStream out;
    @SuppressWarnings("unused")
    private Message message;
    @SuppressWarnings("unused")
    private BankUser user;
    private SecureBanking secure;

    public LogoutCommand (ObjectInputStream in, ObjectOutputStream out, Message message, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.message = message;
        this.secure = secure;
    }
    @Override
    public void execute() {
        secure.resetSession();
        AtmHandler.user = null;
        Bank bank = Bank.getBankInstance();
        bank.writeAtms();
    }
    
}
