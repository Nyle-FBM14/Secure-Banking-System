package com.bankserver;

import java.util.ArrayList;

public class Bank {

    private static volatile Bank bankInstance;
    private final String ID = "BANK";
    private ArrayList<BankUser> bankUsers = new ArrayList<BankUser>();
    private ArrayList<Atm> atms = new ArrayList<Atm>();

    private Bank(){
        //maybe have bank read off a txt file for old data
    }
    public static Bank getBankInstance(){
        Bank bankTemp = bankInstance;
        if(bankTemp == null) {
            synchronized(Bank.class){
                bankTemp = bankInstance;
                if(bankTemp == null)
                    bankInstance = bankTemp = new Bank();
            }
        }
        return bankTemp;
    }
    public String getID(){
        return ID;
    }
    public void addClient(BankUser b){
        bankUsers.add(b);
    }
    public void addATM(Atm a){
        atms.add(a);
    }
    public boolean checkATM(String id){
        for(Atm a : atms){
            if(a.getID().equals(id))
                return true;
        }
        return false;
    }
    public BankUser getBankUser(String cardNum){
        for(BankUser b : bankUsers){
            if(b.getCardNum().equals(cardNum))
                return b;
        }
        return null;
    }
    public void printBankData() {
        System.out.println(atms);
        System.out.println(bankUsers);
    }
}
