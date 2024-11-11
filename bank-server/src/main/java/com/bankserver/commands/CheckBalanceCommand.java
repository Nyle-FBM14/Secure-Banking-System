package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.ResponseStatusCodes;

public class CheckBalanceCommand implements Command {
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<MessageHeaders, String> request;
    private SecureBanking secure;

    public CheckBalanceCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.request = request;
        this.secure = secure;
    }
    @Override
    public void execute() {
        try {
            String cardNum = request.get(MessageHeaders.CARDNUM);
            String pin = request.get(MessageHeaders.PIN);

            HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));

            BankUser user = bank.getBankUser(cardNum);
            if(user == null || !(user.authenticate(pin))) { //invalid card or pin
                System.out.println("Request attempt with invalid credentials.\nCard number: " + cardNum + "\nPin: " + pin);
                response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.ERROR.CODE));
            }
            else{
                double balance = user.checkBalance();
                response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.SUCCESS.CODE));
                response.put(MessageHeaders.RESPONSE, Double.toString(balance));
                System.out.println(String.format("Balance of account with card number %s is $%f.", cardNum, balance));
            }
            SecuredMessage message = secure.encryptAndSignMessage(response);
            out.writeObject(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
