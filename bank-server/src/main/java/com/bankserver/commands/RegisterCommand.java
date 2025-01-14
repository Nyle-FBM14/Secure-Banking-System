package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.security.Message;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.ResponseStatusCodes;

public class RegisterCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message request;

    public RegisterCommand (ObjectInputStream in, ObjectOutputStream out, Message request) {
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
            response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.SUCCESS.CODE));
            out.writeObject(response);
            out.flush();

            System.out.println("Registered user with card num: " + cardNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
