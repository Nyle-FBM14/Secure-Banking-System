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

public class Main {

    public static void registerUser(BufferedReader in, PrintWriter out, BufferedReader stdIn){
        out.println("REGISTER");
        
        try {
            System.out.println("Enter account details for new bank user (card number, pin , starting balance):\n");
            out.println(stdIn.readLine());
            out.println(stdIn.readLine());
            out.println(stdIn.readLine());
            System.out.println(in.readLine());
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
                PrintWriter out
                = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in
                = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedReader stdIn
                = new BufferedReader(
                        new InputStreamReader(System.in)))
        {
            boolean registeringUsers = true;
            while(registeringUsers){
                int choice = Integer.parseInt(stdIn.readLine());

                switch (choice) {
                    case 0:
                        registerUser(in, out, stdIn);
                        break;
                    case 1:
                        registeringUsers = false;
                }
            }
            
            System.out.println(in.readLine());
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