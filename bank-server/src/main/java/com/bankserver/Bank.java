package com.bankserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.nyle.AES;
import javax.crypto.SecretKey;

public class Bank {

    private static volatile Bank bankInstance;
    private final String ID = "BANK";
    private ArrayList<BankUser> bankUsers = new ArrayList<BankUser>();
    private ArrayList<Atm> atms = new ArrayList<Atm>();

    private Bank(){
        loadAtms();
        loadClients();
    }
    public static Bank getBankInstance() {
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
    public String getID() {
        return ID;
    }
    public void addClient(BankUser b) {
        bankUsers.add(b);
    }
    public void addATM(Atm a) {
        atms.add(a);
    }
    public BankUser getBankUser(String cardNum) {
        for(BankUser b : bankUsers){
            if(b.getCardNum().equals(cardNum))
                return b;
        }
        return null;
    }
    public Atm getAtm(String id) {
        for(Atm a : atms) {
            if(a.getId().equals(id))
            return a;
        }
        return null;
    }
    public void printBankData() {
        System.out.println(atms);
        System.out.println(bankUsers);
    }

    private void loadAtms() {
        File atmFile = new File("bank-server\\src\\main\\resources\\bank_atms.txt");
        String atmData;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(atmFile));
            while((atmData = reader.readLine()) != null) {
                String[] data = atmData.split(",");
                SecretKey initialKey = AES.stringToKey(data[2]);
                this.atms.add(new Atm(data[0], initialKey));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadClients() {
        File userFile = new File("bank-server\\src\\main\\resources\\bank_users.txt");
        String userData;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(userFile));
            while((userData = reader.readLine()) != null) {
                String[] data = userData.split(",");
                this.bankUsers.add(new BankUser(data[0], data[1], Double.parseDouble(data[2])));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
