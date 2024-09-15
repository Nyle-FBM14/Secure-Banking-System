package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.enumerations.MessageHeaders;
import com.enumerations.ResponseStatusCodes;

public class DepositCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
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
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));

            BankUser user = bank.getBankUser(cardNum);
            if(user == null || !(user.authenticate(pin))) { //invalid card or pin
                System.out.println("Request attempt with invalid credentials.\nCard number: " + cardNum + "\nPin: " + pin);
                response.put(MessageHeaders.RESPONSE_CODE, Integer.toString(ResponseStatusCodes.ERROR.code));
            }
            else{
                user.deposit(depositAmount);
                response.put(MessageHeaders.RESPONSE_CODE, Integer.toString(ResponseStatusCodes.SUCCESS.code));
                System.out.println(String.format("Deposited $&f into account with card number %s", depositAmount, cardNum));
            }
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
