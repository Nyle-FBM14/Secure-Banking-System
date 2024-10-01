package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Atm;
import com.bankserver.Bank;
import com.enumerations.MessageHeaders;
import com.enumerations.ResponseStatusCodes;

public class ConnectCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<MessageHeaders, String> request;

    public ConnectCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }

    @Override
    public void execute(){
        try {
            String id = request.get(MessageHeaders.ID);
            int nonce = Integer.parseInt(request.get(MessageHeaders.NONCE));
            Atm atm = bank.getAtm(id);

            //create response
            HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
            response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.SUCCESS.code));
            out.writeObject(response);
            out.flush();

            System.out.println("Connection secured with ATM ID " + id);
        } catch (Exception e) {
            System.out.println("Connection failed");
        }
        
    }
}
