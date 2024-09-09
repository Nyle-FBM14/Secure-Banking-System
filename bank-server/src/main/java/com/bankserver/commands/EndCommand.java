package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.enumerations.MessageHeaders;
import com.bankserver.enumerations.ResponseStatusCodes;

public class EndCommand implements Command {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<MessageHeaders, String> request;

    public EndCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }
    @Override
    public void execute() {
        try {
            HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
            response.put(MessageHeaders.RESPONSE_CODE, Integer.toString(ResponseStatusCodes.SUCCESS.code));
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
