package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;

public class RegisterCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<String, String> request;

    public RegisterCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<String, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }
    @Override
    public void execute() {
        try {
            String cardNum = request.get("CARDNUM");
            String pin = request.get("PIN");
            double startingBalance = Double.parseDouble(request.get("STARTBALANCE"));
            
            BankUser newUser = new BankUser(cardNum, pin, startingBalance);

            bank.addClient(newUser);
            
            //create response
            HashMap<String, String> response = new HashMap<String, String>();
            response.put("REQUESTTYPE", request.get("REQUESTTYPE"));
            response.put("RESPONSE", "User registered successfully.");
            out.writeObject(response);
            out.flush();

            System.out.println("Registered user with card num: " + cardNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
