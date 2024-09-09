package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.bankserver.enumerations.MessageHeaders;
import com.bankserver.enumerations.ResponseStatusCodes;

public class RegisterCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<MessageHeaders, String> request;

    public RegisterCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }
    @Override
    public void execute() {
        try {
            String cardNum = request.get(MessageHeaders.CARDNUM);
            String pin = request.get(MessageHeaders.PIN);
            double startingBalance = Double.parseDouble(request.get(MessageHeaders.STARTBALANCE));
            
            BankUser newUser = new BankUser(cardNum, pin, startingBalance);

            bank.addClient(newUser);
            
            //create response
            HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
            response.put(MessageHeaders.RESPONSE_CODE, Integer.toString(ResponseStatusCodes.SUCCESS.code));
            out.writeObject(response);
            out.flush();

            System.out.println("Registered user with card num: " + cardNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
