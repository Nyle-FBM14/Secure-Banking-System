/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atm1;

/**
 *
 * @author nmelegri
 */

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

public class ATM1 {

    private static final String id = "ATM2";
    private static final RSA rsa = new RSA();
    private static final AES aes = new AES();
    private static SecretKey masterkey; //masterkey shared with bank server, NOT master secret key in project manual
    private static HashMap<String, PublicKey> public_keys = new HashMap<String, PublicKey>();
    
    private static Scanner userInput;
    //current session's master secret key, encrypt key, and MAC key
    private static byte[] masterSecret;
    private static SecretKey encryptKey;
    private static SecretKey macKey;
    
    private static boolean loggedIn = false;
    
    private static byte[] deriveKey(byte[] masterSecretKey, byte[] salt, int keyLength) throws Exception {
        char[] masterSecret = new String(masterSecretKey, "UTF-8").toCharArray();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(masterSecret, salt, 65536, keyLength * 8);
        return factory.generateSecret(spec).getEncoded();
    }
    private static void deriveEncryptionAndMacKeys() throws Exception{
        System.out.println("Deriving keys");
        //this assumes that the bank server and ATM machines keep using the same salt string for deriving keys
        //better security can be added by generating a random string every time and passing the salt to the ATMs
        byte[] encryptKeyBytes = deriveKey(masterSecret, "NyleIsTheBest".getBytes(), 16);
        encryptKey = new SecretKeySpec(encryptKeyBytes, "AES");
        
        byte[] macKeyBytes = deriveKey(masterSecret, "SaltKing".getBytes(), 32);
        macKey = new SecretKeySpec(macKeyBytes, "HmacSHA256");
    }
    
    private static void genMasterSecret(BufferedReader in, PrintWriter out) throws Exception{
        //initializing generators for DH algorithm
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
        //setting up parameters
        paramGen.init(2048);
        AlgorithmParameters paramDH = paramGen.generateParameters();
        DHParameterSpec specDH = paramDH.getParameterSpec(DHParameterSpec.class);
        
        //convert DH parameters into a string to send over to the bank server
        String dhString = specDH.getP() + " " + specDH.getG() + " " + specDH.getL();
        //encrypt and send DH parameters to bank server
        //out.println(dhString);
        out.println(aes.encrypt(dhString, masterkey));
        
        //generate key pair
        keyGen.initialize(specDH);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        //turn key into string, then encrypt
        String keyString = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        System.out.println("*************************\n"+keyString);
        
        //send to bank server
        //out.println(keyString);
        out.println(aes.encrypt(keyString, masterkey));
        //receive bank server's key
        //String bankKeyString = in.readLine();
        String bankKeyString = aes.decrypt(in.readLine(), masterkey);
        System.out.println("*************************\n"+bankKeyString);
        //convert key string back to public key object
        byte[] bankKeyBytes = Base64.getDecoder().decode(bankKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bankKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DH");
        PublicKey bankKey = keyFactory.generatePublic(keySpec);
        
        //generate MASTER SECRET
        keyAgree.init(keyPair.getPrivate());
        keyAgree.doPhase(bankKey, true);
        masterSecret = keyAgree.generateSecret();
        
        //check if keys match on both ends for debugging purposes
        System.out.println("MASTER SECRET GENERATED");
        System.out.println(masterSecret);
    }
    private static void setup(BufferedReader in, PrintWriter out) throws Exception{
        int nonce = 1; //FIX make method that generates a random nonce
        //output format: <request type> <atm id> <nonce>
        out.println("0 " + id + " " + Integer.toString(nonce));
        nonce++;
        
        String[] input = in.readLine().split(" ");
        System.out.println(input[0]);
        while(nonce != Integer.parseInt(input[1])){ //checks nonce
            out.println("invalid");
            input = in.readLine().split(" ");
        }
        if(false){ //input format: <1> <nonce+1>input[0].equals("1")
            System.out.println("ATM data saved in bank server");
            out.println("valid");
            
            //masterkey shared with bank server
            String[] halves = in.readLine().split(" ");
            //decrypt halves with private key
            halves[0] = rsa.decryptPrivate(halves[0]);
            halves[1] = rsa.decryptPrivate(halves[1]);
            String mk = halves[0] + halves[1];
            masterkey = aes.stringToKey(mk);
        }
        else{//input format: <0> <nonce+1> <server's public key> <server's id>
            public_keys.put(input[3], rsa.stringToPublicKey(input[2]));
            
            nonce++;
            System.out.println(nonce);
            //output format: <atm's public key> <nonce+2>
            do{
                out.println(rsa.getPublicKey() + " " + Integer.toString(nonce));
                input = in.readLine().split(" ");
            }while (input[0].equals("invalid"));
            //follows the phase 1 protocol from lab 3 and 4 without the initial sending of ID
            input = rsa.decryptPrivate(input[0]).split(" "); //<Nb> <bank id>
            String bankID = input[1];
            String nb = input[0];
            do{
                out.println(rsa.encryptPublic("16 " + nb, public_keys.get(input[1]))); //<Na> <Nb>
                input = rsa.decryptPrivate(in.readLine()).split(" ");
            }while(input[0].equals("invalid"));
            System.out.println(input[0]); //<Nb>
            
            input = rsa.doubleDecrypt(in.readLine(), public_keys.get(bankID)).split(" "); //<masterkey between atm and bank server>
            masterkey = aes.stringToKey(input[0]);
        }
        System.out.println("Case 0 done.");
    }
    
    private static boolean login(BufferedReader in, PrintWriter out) throws Exception{
        System.out.println("Logging in");
        //ask user for username
        System.out.print("Enter username: ");
        String username = userInput.nextLine();
        //ask user for password
        System.out.print("Enter your password: ");
        String password = userInput.nextLine();
        
        String userCredentials = username + " " + password;
        String request = aes.encrypt(userCredentials, masterkey);
        out.println(request);
        
        String response = aes.decrypt(in.readLine(), masterkey);
        System.out.println(response);
        
        return response.equals("Login successful.");
    }
    
    private static void deposit(BufferedReader in, PrintWriter out, double deposit) throws Exception{
        String request = aes.encrypt_verify("2", encryptKey, macKey);
        out.println(request);
                            
        String depositStr = deposit+""; //converting double primitive to string
        String depositRequest = aes.encrypt_verify(depositStr, encryptKey, macKey);
        out.println(depositRequest);
        
        String response = aes.decrypt_verify(in.readLine(), encryptKey, macKey);
        System.out.println(response);
    }
    private static void withdraw(BufferedReader in, PrintWriter out, double withdraw) throws Exception{
        String request = aes.encrypt_verify("3", encryptKey, macKey);
        out.println(request);
                            
        String withdrawStr = withdraw+""; //converting double primitive to string
        String withdrawRequest = aes.encrypt_verify(withdrawStr, encryptKey, macKey);
        out.println(withdrawRequest);
        
        String response = aes.decrypt_verify(in.readLine(), encryptKey, macKey);
        System.out.println(response);
    }
    private static void checkBalance(BufferedReader in, PrintWriter out) throws Exception{
        String request = aes.encrypt_verify("4", encryptKey, macKey);
        out.println(request);
                            
        String balance = aes.decrypt_verify(in.readLine(), encryptKey, macKey);
        System.out.println(balance);
        
    }
    private static void logout(BufferedReader in, PrintWriter out) throws Exception{
        System.out.println("Logging out..");
        String request = aes.encrypt_verify("5", encryptKey, macKey);
        out.println(request);
        
        String response = aes.decrypt_verify(in.readLine(), encryptKey, macKey);
        
        if (response.equals("Logout successful.")){
            loggedIn = false;
            System.out.println("Logged out.");
        }
        else{
            System.out.println("Error loggin out.");
        }
        
    }
    public static void main(String[] args) {
        
        String hostName = "localhost";
        int portNumber = 15777;
        //instantiate Scanner for getting user input
        userInput = new Scanner(System.in);

        if (args.length == 2) {
            hostName = args[0];
            portNumber = Integer.parseInt(args[1]);
        }
        System.out.println("Host Name: " + hostName);
        System.out.println("Port #: " + portNumber);

        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out
                = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in
                = new BufferedReader(
                        new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn
                = new BufferedReader(
                        new InputStreamReader(System.in)))
        {
            
            //case 0, public key exchange and getting masterkey for bank server
            try{
                setup(in, out);
                
            } catch(Exception e){
                e.printStackTrace();
            }
            
            while(true) {
            while(!loggedIn){ //ask for login until correct credentials are entered
                try{
                    out.println("1");
                    loggedIn = login(in, out);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            //create new session keys for successful login
            try{
                genMasterSecret(in, out);
                deriveEncryptionAndMacKeys();
            }catch(Exception e){
                e.printStackTrace();
            }
                
            System.out.println("\nOptions: ");
            System.out.println("2: Deposit, 3: Withdraw, 4: Check Balance, 5: Logout");
            optionLoop: do{
                System.out.println("Select an option: ");
                int choice = userInput.nextInt();
                /*
                if (input == null || input.equals(""))
                    continue;
                int choice = Integer.parseInt(input);*/
//                input = aes.encrypt_verify(input, encryptKey, macKey);
                switch(choice){
                    case 2: // deposit
                        try{  
                            System.out.print("Amount: ");
                            double deposit = userInput.nextDouble(); 
                            deposit(in, out, deposit);
                            
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        
                        break;
                    case 3: // withdraw
                        try{
                            System.out.print("Amount: ");
                            double withdraw = userInput.nextDouble(); 
                            withdraw(in, out, withdraw);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        
                        break;
                    case 4:
                        try{
                            checkBalance(in, out); 
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        
                        break;
                    case 5:
                        try{
                            logout(in, out);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        
                        break optionLoop;
                    default:
                        System.out.println("Invalid command");
                }
            }while(true);
            }

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
