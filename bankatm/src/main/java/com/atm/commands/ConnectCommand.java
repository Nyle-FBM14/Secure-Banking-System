package com.atm.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.SecurityUtils;
import com.security.Utils;
import com.security.enumerations.Algorithms;
import com.security.enumerations.RequestTypes;

public class ConnectCommand implements Command {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    //private SecureBanking secure;
    private SecretKey initialKey;
    private String id;

    public ConnectCommand(ObjectInputStream in, ObjectOutputStream out, String id) {
        this.in = in;
        this.out = out;
        //this.secure = secure;
        this.id = id;
        try {
            getInitialKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getInitialKey() throws Exception{ //reads id and initial key from txt file
        String atmData;
        BufferedReader reader = new BufferedReader(new FileReader("bankatm\\src\\main\\resources\\com\\atm\\atm_" + id + "_data.txt"));
        while((atmData = reader.readLine()) != null) {
            String[] data = atmData.split(",");
            this.initialKey = new SecretKeySpec(Base64.getDecoder().decode(data[1]), "AES");
        }
        reader.close();
    }
    private void setInitialKey(Key key) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("bankatm\\src\\main\\resources\\com\\atm\\atm_" + id + "_data.txt"));
        writer.write(id + "," + Utils.keyToString(key));
        writer.close();
    }
    /*
    public void generateMasterKey() throws Exception {
        //exchange primes
        SecuredMessage dhPrimeMssg = secure.generateDHPrimeMessage();
        out.writeObject(dhPrimeMssg);
        out.flush();
        SecuredMessage bankdhPrimeMssg =  (SecuredMessage) in.readObject();

        //exchange public keys
        KeyPair kp = secure.generateDHKeyPair(bankdhPrimeMssg, dhPrimeMssg.getMessage(), false);
        SecuredMessage dhPuKeyMssg = secure.generateDHPublicKeyMessage(kp.getPublic());
        out.writeObject(dhPuKeyMssg);
        out.flush();
        SecuredMessage bankdhPuKeyMssg =  (SecuredMessage) in.readObject();

        //generate masterkey
        secure.generateMasterKey(bankdhPuKeyMssg, kp.getPrivate());
    }*/
    @Override
    public void execute() {
        try {
            //atm: ID || n
            Message outMessage = new Message(RequestTypes.SECURE_CONNECTION, id, 0, null, Utils.generateNonce(), null);
            out.writeObject(outMessage);
            out.flush();

            //bank: E(initialKey, f(n) || initialKey')
            byte[] response = (byte[]) in.readObject();
            Message inMessage = (Message) SecurityUtils.decrypt(response, initialKey, Algorithms.AES.INSTANCE);
            if(!Utils.nonceFunction(outMessage.getNonce()).equals(inMessage.getNonce())) { //retry mechanism
                System.out.println("Nonce does not match.");
                return;
            }
            setInitialKey(inMessage.getKey());

            //acknowledgements?
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
