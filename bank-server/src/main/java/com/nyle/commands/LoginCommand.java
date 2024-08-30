package com.nyle.commands;

import java.io.BufferedReader;
import java.io.PrintWriter;

import com.nyle.Bank;
import com.nyle.BankUser;

public class LoginCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private BufferedReader in;
    private PrintWriter out;

    public LoginCommand (BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }
    @Override
    public void execute() {
        try {
            String cardNum = in.readLine();
            //check if user exists
            BankUser currentUser = bank.getBankUser(cardNum);
            if(currentUser == null) {
                out.println("wrongCard");
                return;
            }

            String pin = in.readLine();
            //validate pin
            if(currentUser.authenticate(pin)){
                out.println("loginSuccessful");
            }
            else{
                out.println("invalidPin");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
