package com.atm;

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

public class ATM {
    private static final String id = "ATM1";

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
            int command = Integer.parseInt(stdIn.readLine());

            switch(command){
                case 0:

                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
                default:

            }
            out.println("CONNECT");
            out.println("ID1");
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
