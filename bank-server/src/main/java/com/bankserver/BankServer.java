package com.bankserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.*;

public class BankServer {

    public static void main(String[] args) {
        int portNumber = 15777;
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        }
        System.out.println("Port #: " + portNumber);
        
        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (listening) {
                    System.out.println("Server running...");
	            new AtmHandler(serverSocket.accept()).start();
	        }
        }
        catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}
