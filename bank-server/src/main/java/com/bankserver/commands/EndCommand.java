package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.security.Message;

public class EndCommand implements Command {
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    @SuppressWarnings("unused")
    private ObjectOutputStream out;
    @SuppressWarnings("unused")
    private Message message;

    public EndCommand (ObjectInputStream in, ObjectOutputStream out, Message message) {
        this.in = in;
        this.out = out;
        this.message = message;
    }
    @Override
    public void execute() {
        try {
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
