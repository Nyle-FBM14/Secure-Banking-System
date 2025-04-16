package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.security.Message;
import com.security.SecureBanking;
import com.security.enumerations.RequestTypes;

public class LogoutCommand implements Command{
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    
    public LogoutCommand(ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.secure = secure;
    }

    @Override
    public void execute() {
        try {
            Message message = new Message(RequestTypes.LOGOUT, null, 0, null, null);
            out.writeObject(secure.encryptAndSignMessage(message));
            out.flush();

            secure.resetSession();
        } catch (Exception e) {
            System.out.println("Logout failed");
        }
    }
}
