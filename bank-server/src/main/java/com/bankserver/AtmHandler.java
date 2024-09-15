package com.bankserver;

import java.net.*;
import java.io.*;
/*
import java.security.*;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.spec.DHParameterSpec; */
import java.util.HashMap;


import com.bankserver.commands.CheckBalanceCommand;
import com.bankserver.commands.Command;
import com.bankserver.commands.ConnectCommand;
import com.bankserver.commands.DepositCommand;
import com.bankserver.commands.EndCommand;
import com.bankserver.commands.LoginCommand;
import com.bankserver.commands.RegisterCommand;
import com.bankserver.commands.WithdrawCommand;
import com.enumerations.MessageHeaders;

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
            )
        {
            boolean atm_online = true;
            HashMap<MessageHeaders, String> request;
            //HashMap<MessageHeaders, String> response;
            
            while(atm_online){
                Command command = null;
                request = (HashMap<MessageHeaders, String>) in.readObject();
                System.out.println("\n****************Command received: " + request.get(MessageHeaders.REQUESTTYPE));
                switch(request.get(MessageHeaders.REQUESTTYPE)){
                    case "DEPOSIT": //deposit
                        command = new DepositCommand(in, out, request);
                        break;
                    case "WITHDRAW": //withdraw
                        command = new WithdrawCommand(in, out, request);
                        break;
                    case "CHECKBALANCE": //check balanace
                        command = new CheckBalanceCommand(in, out, request);
                        break;
                    case "CONNECT": //new atm connection
                        command = new ConnectCommand(in, out, request);
                        break;
                    case "LOGIN": //client login
                        command = new LoginCommand(in, out, request);
                        break;
                    case "LOGOUT": //client logout
                        //command = new LogoutCommand(in, out, request);
                        break;
                    case "REGISTER": //register user
                        command = new RegisterCommand(in, out, request);
                        break;
                    case "END": //atm or program that registers users terminates their connection
                        command = new EndCommand(in, out, request);
                        atm_online = false;
                        bank.printBankData();
                        break;
                    default:
                        System.out.println("ATM Handler default");
                }

                if (command != null) {
                    System.out.println("Executing command...");
                    command.execute();
                    System.out.println("Waiting for next request...\n****************\n");
                }
            } //end of while loop
            
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
