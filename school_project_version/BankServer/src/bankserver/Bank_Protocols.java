/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankserver;

/**
 *
 * @author nmelegri
 */

import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigInteger;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.spec.DHParameterSpec;

public class Bank_Protocols extends Thread{
    private Socket socket = null;
    private static BankServer.Bank bank;
    //current session keys
    private byte[] masterSecret;
    private SecretKey encryptKey;
    private SecretKey macKey;
    
    private String atmID;
    private Client currentUser;
    
    private Audit audit = new Audit(); //added for audit
    
    public Bank_Protocols(Socket socket, BankServer.Bank bank) {
        super("Bank_Protocols");
        this.socket = socket;
        this.bank = bank;
        
        audit.initializeTextFile();    //added for audit
    }
    
    private void registerUser(BufferedReader in, PrintWriter out) throws Exception{
        String[] userData = in.readLine().split(" ");
        System.out.println(userData[0]);
        System.out.println(userData[1]);
        System.out.println(userData[2]);
        bank.addClient(new Client(userData[0], userData[1], Double.parseDouble(userData[2])));
        out.println("Client registered to sever.");
    }
    private static byte[] deriveKey(byte[] masterSecretKey, byte[] salt, int keyLength) throws Exception {
        char[] masterSecret = new String(masterSecretKey, "UTF-8").toCharArray();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(masterSecret, salt, 65536, keyLength * 8);
        return factory.generateSecret(spec).getEncoded();
    }
    private void deriveEncryptionAndMacKeys() throws Exception{
        //this assumes that the bank server and ATM machines keep using the same salt string for deriving keys
        //better security can be added by generating a random string every time and passing the salt to the ATMs
        byte[] encryptKeyBytes = deriveKey(masterSecret, "NyleIsTheBest".getBytes(), 16);//bank.getATMMasterKey(atmID).getEncoded()
        encryptKey = new SecretKeySpec(encryptKeyBytes, "AES");
        
        byte[] macKeyBytes = deriveKey(masterSecret, "SaltKing".getBytes(), 32);
        macKey = new SecretKeySpec(macKeyBytes, "HmacSHA256");
    }
    private void genMasterSecret(BufferedReader in, PrintWriter out) throws Exception{
        //initializing generators for DH algorithm
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
        //get parameters from ATM
        //String[] dhString = in.readLine().split(" ");
        String[] dhString = bank.getAES().decrypt(in.readLine(), bank.getATMMasterKey(atmID)).split(" ");
        //convert back into DHParameterSpec object
        BigInteger p = new BigInteger(dhString[0]);
        BigInteger g = new BigInteger(dhString[1]);
        int l = Integer.parseInt(dhString[2]);
        DHParameterSpec specDH = new DHParameterSpec(p, g, l);
        
        //get ATM's key, then decrypt
        //String atmKeyString = in.readLine();
        String atmKeyString = bank.getAES().decrypt(in.readLine(), bank.getATMMasterKey(atmID));
        System.out.println("*************************\n"+atmKeyString);
        //convert key string back into public key object
        byte[] atmKeyBytes = Base64.getDecoder().decode(atmKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(atmKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DH");
        PublicKey atmKey = keyFactory.generatePublic(keySpec);
        
        //generate key pair
        keyGen.initialize(specDH);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        //turn key into string, then encrypt
        String keyString = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        //String keyEncrypted = bank.getAES().encrypt(keyString, bank.getATMMasterKey(atmID));
        System.out.println("*************************\n"+keyString);
        //send to ATM
        //out.println(keyString);
        out.println(bank.getAES().encrypt(keyString, bank.getATMMasterKey(atmID)));
        
        //generate MASTER SECRET
        keyAgree.init(keyPair.getPrivate());
        keyAgree.doPhase(atmKey, true);
        masterSecret = keyAgree.generateSecret();
        
        //check if keys match on both ends for debugging purposes
        System.out.println("MASTER SECRET GENERATED");
        System.out.println(masterSecret);//bank.getAES().getKeyString(masterSecret)
    }
    private void newATMConnection(BufferedReader in, PrintWriter out, String[] input) throws Exception{
        //input format: <request type> <atm id> <nonce>
        //check if ATM has connected to server before
        String id = input[1];
        atmID = id;
        int nonce = Integer.parseInt(input[2]);
        if(false){//bank.checkATM(input[1])
            //ATM has connected to ATM before
            //output format: <1> <nonce+1>
            do{
                out.println("1 " + Integer.toString(nonce + 1));
                input = in.readLine().split(" ");
            }while(input[0].equals("invalid"));
            System.out.println(input[0]);
            
            //masterkey shared with ATM
            String mk = bank.getAES().getKeyString(bank.getATMMasterKey(atmID));
            //halving key into 2 parts 
            int middle = mk.length()/2;
            String[] halves = {mk.substring(0, middle), mk.substring(middle)};

            //encrypt with public key
            halves[0] = bank.getRSA().encryptPublic(mk, bank.getATMPublicKey(atmID));
            halves[1] = bank.getRSA().encryptPublic(mk, bank.getATMPublicKey(atmID));
            out.println(halves[0] + " " + halves[1]);
        }
        else{
            //new ATM connection
            //exchange public keys
            //output format: <0> <nonce+1> <server's public key> <server id>
            nonce++;
            do{
                out.println("0 " + Integer.toString(nonce) + " " + bank.getRSA().getPublicKey() + " " + bank.getID());
                input = in.readLine().split(" ");
            }while(input[0].equals("invalid"));
            
            nonce++;
            System.out.println(nonce);
            System.out.println(input[0]);
            //input format: <atm's public key> <nonce+2>
            while(nonce != Integer.parseInt(input[1])){//checks nonce, tells client to try again
                //output format: <invalid>
                out.println("invalid");
                input = in.readLine().split(" ");
            }
            
            //add atm data to server
            PublicKey pk = bank.getRSA().stringToPublicKey(input[0]);
            SecretKey mk = bank.getAES().generateKey();
            bank.addATM(new ATM(id, pk, mk));
            //creating and sharing the master key between the bank server and atm
            //follows the phase 1 protocol from lab 3 and 4 without the initial sending of ID
            String nb = "14"; //FIX make method that generates a random nonce
            String output = nb + " " + bank.getID(); //<Nb> <bank id>
            output = bank.getRSA().encryptPublic(output, bank.getATMPublicKey(id));
            out.println(output);
            
            String encrypted = in.readLine();
            input = bank.getRSA().decryptPrivate(encrypted).split(" "); //<Na> <Nb>
            while(!input[1].equals(nb)){//checks nonce, tells client to try again
                //output format: <invalid>
                out.println("invalid");
                input = in.readLine().split(" ");
            }
            out.println(bank.getRSA().encryptPublic(nb, pk));//<Nb>
            
            output = bank.getAES().getKeyString(bank.getATMMasterKey(id));
            output = bank.getRSA().doubleEncrypt(output, pk);
            out.println(output); //<masterkey between atm and bank server>
        }
        System.out.println("Case 0 done.");
    }
    
    private void loginUser(BufferedReader in, PrintWriter out) throws Exception{
        String input = in.readLine();
        input = bank.getAES().decrypt(input, bank.getATMMasterKey(atmID));
        
        String[] userCredentials = input.split(" ");
        System.out.println(userCredentials[0]);
        System.out.println(userCredentials[1]);
        String output;
        currentUser = bank.getClient(userCredentials[0]); //sets the current user of the session
        if(currentUser == null){
            output = "User does not exist or wrong username.";
        }
        else if(currentUser.authenticate(userCredentials[1])){
            output = "Login successful.";
        }
        else{
            output = "Incorrect password";
            currentUser = null; //resets the current user to nobody
        }
        out.println(bank.getAES().encrypt(output, bank.getATMMasterKey(atmID)));
    }
    
    private void checkBalance(BufferedReader in, PrintWriter out) throws Exception {
        // Since currentUser has already been established before checkBalance ever gets called, this works
        Double balance = currentUser.checkBalance();
        String balanceStr = balance.toString();
        out.println(bank.getAES().encrypt_verify(balanceStr, encryptKey, macKey));
        
        //Auditing
        LocalDateTime currentDateTime = LocalDateTime.now();
        String auditEntry = "Customer ID: " + currentUser.getUsername() + " Balance: " + balanceStr + " Time: " + currentDateTime;
        String encryptedAuditEntry = bank.getRSA().encryptPublic(auditEntry, bank.getRSA().getPublicKeyNotEncoded());
        audit.appendLineToFile(encryptedAuditEntry);
    }
    
    private void deposit(BufferedReader in, PrintWriter out) throws Exception {
        Double oldBalance = currentUser.checkBalance();
        String oldBalanceStr = oldBalance.toString();
        
        
        String depositStr = bank.getAES().decrypt_verify(in.readLine(), encryptKey, macKey);
        double deposit = Double.parseDouble(depositStr);
        
        currentUser.deposit(deposit);
        
        Double newBalance = currentUser.checkBalance();
        String newBalanceStr = newBalance.toString();
        
        String summary = "Deposited. Old Balance: " + oldBalanceStr + ", New Balance: " + newBalanceStr;
        out.println(bank.getAES().encrypt_verify(summary, encryptKey, macKey));
        
        //Auditing
        LocalDateTime currentDateTime = LocalDateTime.now();
        String auditEntry = "Customer ID: " + currentUser.getUsername() + " " + summary + " Time: " + currentDateTime;
        String encryptedAuditEntry = bank.getRSA().encryptPublic(auditEntry, bank.getRSA().getPublicKeyNotEncoded());
        audit.appendLineToFile(encryptedAuditEntry);
    }
    
    private void withdraw(BufferedReader in, PrintWriter out) throws Exception {
        Double oldBalance = currentUser.checkBalance();
        String oldBalanceStr = oldBalance.toString();


        String withdrawStr = bank.getAES().decrypt_verify(in.readLine(), encryptKey, macKey);
        double withdraw = Double.parseDouble(withdrawStr);

        currentUser.withdraw(withdraw);

        Double newBalance = currentUser.checkBalance();
        String newBalanceStr = newBalance.toString();

        String summary = "Withdrawn. Old Balance: " + oldBalanceStr + ", New Balance: " + newBalanceStr;
        out.println(bank.getAES().encrypt_verify(summary, encryptKey, macKey));

        //Auditing
        LocalDateTime currentDateTime = LocalDateTime.now();
        String auditEntry = "Customer ID: " + currentUser.getUsername() + " " + summary + " Time: " + currentDateTime;
        String encryptedAuditEntry = bank.getRSA().encryptPublic(auditEntry, bank.getRSA().getPublicKeyNotEncoded());
        audit.appendLineToFile(encryptedAuditEntry);
    }
    
    // For decrypting Audit file
    private void decryptAudit(){
        try (BufferedReader reader = new BufferedReader(new FileReader(audit.getfileName()))) {
            String line;

            System.out.println("Contents of the file:");

            // Loop through each line in the file
            while ((line = reader.readLine()) != null) {
                System.out.println(bank.getRSA().decryptPrivate(line));
            }
        } catch (Exception ignore) {
            //System.err.println("Error reading the file: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    private void logoutUser(BufferedReader in, PrintWriter out) throws Exception {
        currentUser = null;
        String output = "Logout successful.";
        out.println(bank.getAES().encrypt_verify(output, encryptKey, macKey));
        
    }
    
    public void run() {
        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            boolean atm_online = true;
            String[] input;
            String output;
            
            /*
                -atm should be able to continously make requests to the server
                -a client logging out does not mean the atm socket should be closed,
                a new client should be able to log in
                -running the atm main method should go to case 0, all other cases are based on user input
                -try to contain each case into its own method
            */
            while(atm_online){
                String request = in.readLine();
                
                if(request == null || request.equals(""))
                    continue;
                //if the request is not setting up a new ATM connection or registering a user, it must be encrypted
                if(request.charAt(0) != '0' && request.charAt(0) != '6' && request.charAt(0) != '1'){
                    request = bank.getAES().decrypt_verify(request, encryptKey, macKey);
                    System.out.println(request);
                }
                input = request.split(" ");
                switch(Integer.parseInt(input[0])){
                    case 0: //new atm connection
                        try{
                            newATMConnection(in, out, input);
                            
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case 1: //client login
                        try{
                            loginUser(in, out);
                            if(currentUser != null){
                                //create new session keys after user logs in successfully
                                genMasterSecret(in, out);
                                deriveEncryptionAndMacKeys();
                            }
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case 2: //deposit
                        try{
                            deposit(in, out);
                        } catch(Exception e){
                            e.printStackTrace();
                        } 
                        break;
                    case 3: //withdraw
                        try{
                            withdraw(in, out);
                        } catch(Exception e){
                            e.printStackTrace();
                        } 
                        break;
                    case 4: //check balanace
                        try{
                            checkBalance(in, out);
                        } catch(Exception e){
                            e.printStackTrace();
                        } 
                        break;
                    case 5: //client logout
                        try{
                            logoutUser(in, out);
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                        decryptAudit();
                        break;
                    case 6: //register user
                        try{
                            registerUser(in, out);
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println("Eh");
                }
                
                
            } //end of while loop
            
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
