package com.atm;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.*;
import java.util.HashMap;

import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class ATM {
    private static final String id = "ATM1";

    public static void secureConnection(ObjectInputStream in, ObjectOutputStream out){

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
            secureConnection(in, out);
            while(true){
                int command = Integer.parseInt(stdIn.readLine());
                HashMap<String, String> request = new HashMap<String, String>();
                switch(command){
                    case 0:
                        request.put("REQUESTTYPE", "LOGIN");
                        break;
                    case 1:
                        request.put("REQUESTTYPE", "DEPOSIT");
                        break;
                    case 2:
                        request.put("REQUESTTYPE", "WITHDRAW");
                        break;
                    case 3:
                        request.put("REQUESTTYPE", "CHECK");
                        break;
                    case 4:
                        request.put("REQUESTTYPE", "LOGOUT");
                        break;
                    default:
                        continue;
                }
            } //end of while loop
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
