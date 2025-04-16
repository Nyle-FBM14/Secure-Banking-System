package com.bankserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import com.security.AES;
import com.security.SecurityUtils;

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
                SecretKey initialKey = AES.stringToKey(data[1]);
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

    public void writeAtms() {
        try {
            FileWriter atmFile = new FileWriter("bank-server\\src\\main\\resources\\bank_atms.txt");
            BufferedWriter writer = new BufferedWriter(atmFile);
            
            for(Atm a: atms) {
                writer.write(a.getId() + "," + SecurityUtils.keyToString(a.getInitialkey()) + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("here");
        }
    }
}
