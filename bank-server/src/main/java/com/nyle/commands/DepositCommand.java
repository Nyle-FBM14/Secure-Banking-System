package com.nyle.commands;

import java.io.BufferedReader;
import java.io.PrintWriter;

import com.nyle.Bank;
import com.nyle.BankUser;

public class DepositCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private BufferedReader in;
    private PrintWriter out;

    public DepositCommand (BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void execute() {
        try {
            String input = in.readLine(); //get card number
            BankUser currentUser = bank.getBankUser(input);
            if(currentUser == null){
                out.println("error");
                return;
            }
            input = in.readLine(); //get deposit amount
            currentUser.deposit(Double.parseDouble(input));
            out.println("deposited");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
