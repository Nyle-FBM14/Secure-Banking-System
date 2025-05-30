package com.bankserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class BankServer {

    public static void main(String[] args) {
        int portNumber = 15777;
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        }
        System.out.println("Port #: " + portNumber);
        Logger logger = Logger.getGlobal();
        logger.setUseParentHandlers(false);
        
        boolean listening = true;
        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                EncryptingHandler handler = new EncryptingHandler();
            )
        {
            logger.addHandler(handler);
            System.out.println("Server running...");
            while (listening) {
	            new AtmHandler(serverSocket.accept(), logger).start();
	        }
        }
        catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}
