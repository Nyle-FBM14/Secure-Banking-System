package com.nyle.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.nyle.Atm;
import com.nyle.Bank;

public class ConnectCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<String, String> request;

    public ConnectCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<String, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }

    @Override
    public void execute(){
        try {
            String id = request.get("ID");
            Atm atm = new Atm(id);
            bank.addATM(atm);

            //create resonse
            HashMap<String, String> response = new HashMap<String, String>();
            response.put("REQUESTTYPE", request.get("REQUESTTYPE"));
            response.put("RESPONSE", "Connection secured.");
            out.writeObject(response);
            out.flush();

            System.out.println("Connection secured with ATM ID " + id);
        } catch (Exception e) {
            System.out.println("Connection failed");
        }
        
    }
}
