package com.bankserver;

import java.net.*;
import java.io.*;
import java.util.HashMap;

import com.bankserver.commands.CheckBalanceCommand;
import com.bankserver.commands.Command;
import com.bankserver.commands.ConnectCommand;
import com.bankserver.commands.DepositCommand;
import com.bankserver.commands.EndCommand;
import com.bankserver.commands.LoginCommand;
import com.bankserver.commands.RegisterCommand;
import com.bankserver.commands.WithdrawCommand;
import com.nyle.SecureBanking;
import com.nyle.SecuredMessage;
import com.nyle.enumerations.MessageHeaders;

public class AtmHandler extends Thread {
    private Socket socket = null;
    private Bank bank = Bank.getBankInstance();
    private SecureBanking secure = new SecureBanking();

    public AtmHandler(Socket socket) {
        super("AtmHandler");
        this.socket = socket;
    }

    private boolean connectionLoop(ObjectInputStream in, ObjectOutputStream out) throws Exception {
        while(true) {
            @SuppressWarnings("unchecked")
            HashMap<MessageHeaders, String> request = (HashMap<MessageHeaders, String>) in.readObject();
            Command command = null;
            switch (request.get(MessageHeaders.REQUESTTYPE)) {
                case "SECURE_CONNECTION":
                    command = new ConnectCommand(in, out, request, secure);
                    command.execute();
                    return true;
                case "REGISTER":
                    command = new RegisterCommand(in, out, request);
                    command.execute();
                    break;
                case "END":
                    command = new EndCommand(in, out, request);
                    command.execute();
                    return false;
            }
        }
    }
    private boolean loginLoop(ObjectInputStream in, ObjectOutputStream out) throws Exception {
        while(true) {
            SecuredMessage message = (SecuredMessage) in.readObject();
            HashMap<MessageHeaders, String> request = secure.decryptAndVerifyMessage(message);
            if(request != null) {
                Command command = null;
                switch (request.get(MessageHeaders.REQUESTTYPE)) {
                    case "LOGIN":
                        command = new LoginCommand(in, out, secure, message);
                        command.execute();
                        return true;
                    case "END":
                        command = new EndCommand(in, out, request);
                        command.execute();
                        return false;
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
                    SecuredMessage message = (SecuredMessage) in.readObject();
                    HashMap<MessageHeaders, String> request = secure.decryptAndVerifyMessage(message);
                    if(request == null)
                        continue;
                    
                    Command command = null;
                    System.out.println("\n****************Command received: " + request.get(MessageHeaders.REQUESTTYPE));
                    switch(request.get(MessageHeaders.REQUESTTYPE)){
                        case "DEPOSIT": //deposit
                            command = new DepositCommand(in, out, request);
                            break;
                        case "WITHDRAW": //withdraw
                            command = new WithdrawCommand(in, out, request);
                            break;
                        case "CHECK_BALANCE": //check balanace
                            command = new CheckBalanceCommand(in, out, request);
                            break;
                        case "LOGOUT": //client logout
                            //command = new LogoutCommand(in, out, request);
                            online = loginLoop(in, out);
                            break;
                        case "REGISTER": //register user
                            command = new RegisterCommand(in, out, request);
                            break;
                        case "END": //atm or program that registers users terminates their connection
                            command = new EndCommand(in, out, request);
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
