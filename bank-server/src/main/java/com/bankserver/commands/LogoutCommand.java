package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    public LogoutCommand (ObjectInputStream in, ObjectOutputStream out, Message message, BankUser user, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.message = message;
        this.user = user;
        this.secure = secure;
    }
    @Override
    public void execute() {
        secure.resetSession();
        user = null;
    }
    
}
