package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.bankserver.Bank;
import com.bankserver.BankUser;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.ResponseStatusCodes;

public class LoginCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<MessageHeaders, String> request;

    public LoginCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request) {
        this.in = in;
        this.out = out;
        this.request = request;
    }
    @Override
    public void execute() {
        try {
            String cardNum = request.get(MessageHeaders.CARDNUM);
            String pin = request.get(MessageHeaders.PIN);

            BankUser user = bank.getBankUser(cardNum);

            //create response
            HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
            
            if(user == null) { //check if user exists
                response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.INVALID_CARD.CODE));
            }
            else if (user.authenticate(pin)){ //validate pin
                response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.SUCCESS.CODE));
                System.out.println("Validated user with card num: " + cardNum);
            }
            else{
                response.put(MessageHeaders.RESPONSECODE, Integer.toString(ResponseStatusCodes.INVALID_PIN.CODE));
            }
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
