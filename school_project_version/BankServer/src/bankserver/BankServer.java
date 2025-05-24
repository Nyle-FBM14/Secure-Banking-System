/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.*;

/**
 *
 * @author nmelegri
 */
public class BankServer {

    public static class Bank {
        private static ArrayList<Client> clients = new ArrayList<Client>();
        private static ArrayList<ATM> atms = new ArrayList<ATM>();
        private static final AES aes = new AES();
        private static final RSA rsa = new RSA();
        public static final String id = "BANK";
        
        public void addClient(Client c){
            clients.add(c);
        }
        public void addATM(ATM a){
            atms.add(a);
        }
        public boolean checkATM(String id){
            for(ATM a : atms){
                if(a.getID().equals(id))
                    return true;
            }
            return false;
        }
        public Client getClient(String username){
            for(Client c : clients){
                if(c.getUsername().equals(username))
                    return c;
            }
            return null;
        }
        public PublicKey getATMPublicKey(String id){
            for(ATM a: atms){
                if(a.getID().equals(id))
                    return a.getPK();
            }
            return null;
        }
        public SecretKey getATMMasterKey(String id){
            for(ATM a: atms){
                if(a.getID().equals(id))
                    return a.getMK();
            }
            return null;
        }
        public AES getAES(){
            return aes;
        }
        public RSA getRSA(){
            return rsa;
        }
        public String getID(){
            return id;
        }
    }
    public static void main(String[] args) {
        Bank bank = new Bank();
        int portNumber = 15777;
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        }
        System.out.println("Port #: " + portNumber);
        
        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (listening) {
                    System.out.println("Server running...");
	            new Bank_Protocols(serverSocket.accept(), bank).start();
	        }
        }
        catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
    
}
