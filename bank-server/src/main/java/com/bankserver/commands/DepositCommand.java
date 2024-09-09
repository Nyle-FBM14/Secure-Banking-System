package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.bankserver.enumerations.MessageHeaders;
import com.bankserver.enumerations.ResponseStatusCodes;

public class DepositCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<MessageHeaders, String> request;

    public DepositCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }

    @Override
    public void execute() {
        try {
            String cardNum = request.get(MessageHeaders.CARDNUM);
            String pin = request.get(MessageHeaders.PIN);
            double depositAmount = Double.parseDouble(request.get(MessageHeaders.DEPOSITAMOUNT));

            HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();

            BankUser user = bank.getBankUser(cardNum);
            if(user == null || !(user.authenticate(pin))) { //invalid card or pin
                System.out.println("Request attempt with invalid credentials.\nCard number: " + cardNum + "\nPin: " + pin);
                response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
                response.put(MessageHeaders.RESPONSE_CODE, Integer.toString(ResponseStatusCodes.ERROR.code));
                out.writeObject(response);
                out.flush();
                return;
            }

            user.deposit(depositAmount);
            
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
            response.put(MessageHeaders.RESPONSE_CODE, Integer.toString(ResponseStatusCodes.SUCCESS.code));
            out.writeObject(response);
            out.flush();

            System.out.println(String.format("Deposited $&f into account with card number %s", depositAmount, cardNum));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
