package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;

public class WithdrawCommand implements Command {
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<String, String> request;

    public WithdrawCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<String, String> request) {
        this.in = in;
        this.out = out;
        this.request =  request;
    }

    @Override
    public void execute() {
        try {
            String cardNum = request.get("CARDNUM");
            String pin = request.get("PIN");
            double withdrawAmount = Double.parseDouble(request.get("WITHDRAWAMOUNT"));

            BankUser user = bank.getBankUser(cardNum);
            if(user == null) { //invalid card
                System.out.println("Request attempt with invalid credentials.\nCard number: " + cardNum + "\nPin: " + pin);
                return;
            }
            if(!(user.authenticate(pin))) { //invalid pin
                System.out.println("Request attempt with invalid credentials.\nCard number: " + cardNum + "\nPin: " + pin);
                return;
            }

            user.withdraw(withdrawAmount);
            //create response
            HashMap<String, String> response = new HashMap<String, String>();
            response.put("REQUESTTYPE", request.get("REQUESTTYPE"));
            response.put("RESPONSE", "Withdraw successful.");
            out.writeObject(response);
            out.flush();

            System.out.println(String.format("Withdrew $&f from account with card number %s", withdrawAmount, cardNum));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
