package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.RequestTypes;

public class LogoutCommand implements Command{
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
            SecuredMessage sMessage = secure.encryptAndSignMessage(message);
            out.writeObject(sMessage);
            out.flush();

            secure.resetSession();
            sMessage = (SecuredMessage) in.readObject();
            message = secure.decryptAndVerifyMessage(sMessage);
            System.out.println(message.getMessage());
        } catch (Exception e) {
            System.out.println("Logout failed.");
            e.printStackTrace();
        }
    }
}
