package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;

public class LoginCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<String, String> request;

    public LoginCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<String, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }
    @Override
    public void execute() {
        try {
            String cardNum = request.get("CARDNUM");
            String pin = request.get("PIN");

            BankUser user = bank.getBankUser(cardNum);

            //create response
            HashMap<String, String> response = new HashMap<String, String>();
            response.put("REQUESTTYPE", request.get("REQUESTTYPE"));
            
            if(user == null) { //check if user exists
                response.put("RESPONSE", "Invalid card.");
            }
            else if (user.authenticate(pin)){ //validate pin
                response.put("RESPONSE", "User login successful.");
            }
            else{
                response.put("RESPONSE", "Invalid pin.");
            }
            out.writeObject(response);
            out.flush();
            System.out.println("Validated user with card num: " + cardNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
