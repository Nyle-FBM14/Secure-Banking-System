package com.bankserver;

import java.net.*;
import java.io.*;
import java.util.HashMap;

import javax.crypto.SecretKey;

import com.bankserver.commands.CheckBalanceCommand;
import com.bankserver.commands.Command;
import com.bankserver.commands.ConnectCommand;
import com.bankserver.commands.DepositCommand;
import com.bankserver.commands.EndCommand;
import com.bankserver.commands.LoginCommand;
import com.bankserver.commands.LogoutCommand;
import com.bankserver.commands.RegisterCommand;
import com.bankserver.commands.WithdrawCommand;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.SecurityUtils;
import com.security.enumerations.Algorithms;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.RequestTypes;

public class AtmHandler extends Thread {
    private Socket socket = null;
    private Bank bank = Bank.getBankInstance();
    private SecureBanking secure = new SecureBanking();
    BankUser user;

    public AtmHandler(Socket socket) {
        super("AtmHandler");
        this.socket = socket;
    }

    private boolean connectionLoop(ObjectInputStream in, ObjectOutputStream out) throws Exception {
        while(true) {
            Message message = (Message) in.readObject();
            Command command = null;
            switch (message.getRequestType()) {
                case RequestTypes.SECURE_CONNECTION:
                    command = new ConnectCommand(in, out, message, secure);
                    command.execute();
                    return true;
                case RequestTypes.REGISTER:
                    command = new RegisterCommand(in, out, message);
                    command.execute();
                    break;
                case RequestTypes.END:
                    command = new EndCommand(in, out, message);
                    command.execute();
                    return false;
                default:
                    System.out.println("default");
            }
        }
    }
    private boolean loginLoop(ObjectInputStream in, ObjectOutputStream out) throws Exception {
        while(true) {
            SecuredMessage sMessage = (SecuredMessage) in.readObject();
            Message message = (Message) secure.decryptAndVerifyMessage(sMessage);
            if(message != null) {
                Command command = null;
                switch (message.getRequestType()) {
                    case RequestTypes.LOGIN:
                        command = new LoginCommand(in, out, secure, message, user);
                        command.execute();
                        return true;
                    case RequestTypes.END:
                        command = new EndCommand(in, out, message);
                        command.execute();
                        return false;
                    default:
                        System.out.println("default");
                }
            }
            
        }
    }
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            )
        {
            if(connectionLoop(in, out)) {
                boolean online = loginLoop(in, out);
                while(online){
                    SecuredMessage sMessage = (SecuredMessage) in.readObject();
                    Message message = secure.decryptAndVerifyMessage(sMessage);
                    if(message == null)
                        continue;
                    
                    Command command = null;
                    System.out.println("\n****************Command received: " + message.getRequestType().toString());
                    switch(message.getRequestType()){
                        case RequestTypes.DEPOSIT: //deposit
                            command = new DepositCommand(in, out, message, user, secure);
                            break;
                        case RequestTypes.WITHDRAW: //withdraw
                            command = new WithdrawCommand(in, out, message, user, secure);
                            break;
                        case RequestTypes.CHECK_BALANCE: //check balanace
                            command = new CheckBalanceCommand(in, out, message, user, secure);
                            break;
                        case RequestTypes.LOGOUT: //client logout
                            command = new LogoutCommand(in, out, message, user, secure);
                            online = loginLoop(in, out);
                            break;
                        case RequestTypes.END: //atm or program that registers users terminates their connection
                            //command = new EndCommand(in, out, message);
                            online = false;
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
            } //end of if
            
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
