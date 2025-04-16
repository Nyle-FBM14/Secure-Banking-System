package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.RequestTypes;

public class EndCommand implements Command {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;

    public EndCommand(ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.secure = secure;
    }

    @Override
    public void execute() {
        try {
            Message message = new Message(RequestTypes.END, null, 0, null, null);
            SecuredMessage sMessage = secure.encryptAndSignMessage(message);
            out.writeObject(sMessage);
            out.flush();

            sMessage = (SecuredMessage) in.readObject();
            message = secure.decryptAndVerifyMessage(sMessage);
            System.out.println(message.getMessage());

            System.out.println("Terminating ATM.");
        } catch (Exception e) {
            System.out.println("End failed.");
            e.printStackTrace();
        }
    }
}
