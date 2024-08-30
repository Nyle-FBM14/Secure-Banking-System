package com.nyle.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.nyle.Bank;
import com.nyle.BankUser;

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
            String cardNum = request.get("CARDNUM"); //get card number
            String pin = request.get("PIN"); //get pin
            double startingBalance = Double.parseDouble(request.get("STARTBALANCE")); //get starting balance
            
            BankUser newUser = new BankUser(cardNum, pin, startingBalance);

            bank.addClient(newUser);
            
            //create resonse
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
