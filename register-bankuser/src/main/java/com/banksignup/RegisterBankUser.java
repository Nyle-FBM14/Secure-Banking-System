package com.banksignup;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.*;
import java.util.HashMap;

import java.util.Scanner;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.enumerations.MessageHeaders;
import com.enumerations.RequestTypes;

public class RegisterBankUser {

    @SuppressWarnings("unchecked")
    public static void registerUser(ObjectInputStream in, ObjectOutputStream out, BufferedReader stdIn){
        try {
            HashMap<MessageHeaders, String> user = new HashMap<MessageHeaders, String>();
            System.out.println("\n****************");
            System.out.println("Enter account details for new bank user (card number, pin , starting balance):");
            user.put(MessageHeaders.REQUESTTYPE, "REGISTER");
            user.put(MessageHeaders.CARDNUM, stdIn.readLine());
            user.put(MessageHeaders.PIN, stdIn.readLine());
            user.put(MessageHeaders.STARTBALANCE, stdIn.readLine());
            //send register request with user data
            out.writeObject(user);
            out.flush();

            //response
            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) in.readObject();
            System.out.println(response.get(MessageHeaders.REQUESTTYPE));
            System.out.println(response.get(MessageHeaders.RESPONSE_CODE));
            System.out.println("****************\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    private static void terminateConnection(ObjectInputStream in, ObjectOutputStream out) {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.END.toString());
            out.writeObject(request);
            out.flush();

            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) in.readObject();
            System.out.println(response.get(MessageHeaders.RESPONSE_CODE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 15777;

        if (args.length == 2) {
            hostName = args[0];
            portNumber = Integer.parseInt(args[1]);
        }
        System.out.println("Host Name: " + hostName);
        System.out.println("Port #: " + portNumber);

        try (
                Socket socket = new Socket(hostName, portNumber);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            )
        {
            boolean registeringUsers = true;
            while(registeringUsers){
                System.out.println("0 to register, 1 to exit");
                int choice = Integer.parseInt(stdIn.readLine());
                switch (choice) {
                    case 0:
                        registerUser(in, out, stdIn);
                        break;
                    case 1:
                        registeringUsers = false;
                        terminateConnection(in, out);
                        System.out.println("Closing program.");
                }
            }
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to "
                    + hostName);
            System.exit(1);
        }
    }
}