package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.ResponseStatusCodes;

public class WithdrawCommand implements Command {
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<MessageHeaders, String> request;

    public WithdrawCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }

    @Override
    public void execute() {
        try {
            String cardNum = request.get(MessageHeaders.CARDNUM);
            String pin = request.get(MessageHeaders.PIN);
            double withdrawAmount = Double.parseDouble(request.get(MessageHeaders.WITHDRAWAMOUNT));

            HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));

            BankUser user = bank.getBankUser(cardNum);
            if(user == null || !(user.authenticate(pin))) { //invalid card or pin
                System.out.println("Request attempt with invalid credentials.\nCard number: " + cardNum + "\nPin: " + pin);
                response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.ERROR.CODE));
            }
            else{
                user.withdraw(withdrawAmount);
                response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.SUCCESS.CODE));
                System.out.println(String.format("Withdrew $&f from account with card number %s", withdrawAmount, cardNum));
            }
            
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
