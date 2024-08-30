package com.nyle;

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
        Bank existingBank = bankInstance;
        if(existingBank == null) {
            synchronized(Bank.class){
                if(bankInstance == null)
                bankInstance = new Bank();
            }
        }
        
        return existingBank;
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
    public void printAtms() {
        for(Atm a: atms)
            System.out.println(a.getID());
    }
}
