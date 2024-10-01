package com.nyle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.crypto.SecretKey;

import com.nyle.enumerations.MessageHeaders;

public class Main {
    public static void main(String[] args) {
        SecretKey atm = AES.generateKey();
        
        try {
            BufferedWriter write = new BufferedWriter(new FileWriter("key.txt"));
            write.write(Utils.keyToString(atm));
            write.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}