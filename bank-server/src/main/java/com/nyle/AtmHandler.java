package com.nyle;

import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.nyle.commands.Command;
import com.nyle.commands.ConnectCommand;

import java.math.BigInteger;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.spec.DHParameterSpec;
import java.util.HashMap;

public class AtmHandler extends Thread {
    private Socket socket = null;
    private Bank bank = Bank.getBankInstance();

    public AtmHandler(Socket socket) {
        super("AtmHandler");
        this.socket = socket;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            boolean atm_online = true;
            HashMap<String, String> request;
            HashMap<String, String> response;
            
            while(atm_online){
                Command command = null;
                request = (HashMap<String, String>) in.readObject();
                System.out.println(request);
                switch(request.get("REQUESTTYPE")){
                    case "DEPOSIT": //deposit
                        
                        break;
                    case "WITHDRAW": //withdraw
                        
                        break;
                    case "CHECK": //check balanace
                        
                        break;
                    case "CONNECT": //new atm connection
                        command = new ConnectCommand(in, out, request);
                        break;
                    case "LOGIN": //client login
                        
                        break;
                    case "LOGOUT": //client logout
                        
                        break;
                    case "REGISTER": //register user
                        
                        break;
                    default:
                        System.out.println("Eh");
                }

                if (command != null) {
                    System.out.println("Executing command...");
                    command.execute();
                    System.out.println("Waiting for next request...");
                }
                bank.printAtms();
            } //end of while loop
            
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
