package com.bankserver;

import java.io.IOException;
import java.net.ServerSocket;

public class BankServer {

    public static void main(String[] args) {
        int portNumber = 15777;
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        }
        System.out.println("Port #: " + portNumber);
        
        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server running...");
            while (listening) {
	            new AtmHandler(serverSocket.accept()).start();
	        }
        }
        catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}
