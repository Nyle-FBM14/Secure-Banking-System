package com.atm.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nyle.RSA;
import com.nyle.SecureBanking;
import com.nyle.SecuredMessage;
import com.nyle.SecurityUtils;
import com.nyle.Utils;
import com.nyle.enumerations.Algorithms;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;

public class ConnectCommand implements Command {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    private SecretKey initialKey;
    private String id;

    public ConnectCommand(ObjectInputStream in, ObjectOutputStream out, SecureBanking secure, String id) {
        this.in = in;
        this.out = out;
        this.secure = secure;
        this.id = id;
        try {
            getInitialKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getInitialKey() throws Exception{ //reads id and initial key from txt file
        String atmData;
        BufferedReader reader = new BufferedReader(new FileReader("bankatm\\src\\main\\resources\\com\\nyle\\atm_" + id + "_data.txt"));
        while((atmData = reader.readLine()) != null) {
            String[] data = atmData.split(",");
            this.initialKey = new SecretKeySpec(Base64.getDecoder().decode(data[1]), "AES");
        }
        reader.close();
    }
    private void setInitialKey(String key) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("bankatm\\src\\main\\resources\\com\\nyle\\atm_" + id + "_data.txt"));
        writer.write(id + "," + key);
        writer.close();
    }
    @SuppressWarnings("unchecked")
    public void initialExchange() throws Exception {
        //atm: ID || n
        String nonce = Utils.generateNonce();
        HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
        request.put(MessageHeaders.REQUESTTYPE, RequestTypes.SECURE_CONNECTION.toString());
        request.put(MessageHeaders.ID, id);
        request.put(MessageHeaders.NONCE, nonce);
        out.writeObject(request);
        out.flush();

        //bank: E(initialKey, puBK || f(n) || initialKey')
        byte[] response = (byte[]) in.readObject();
        HashMap<MessageHeaders, String> bankMessage = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(response, initialKey, Algorithms.AES.INSTANCE);
        nonce = secure.nonceFunction(nonce);
        if(!nonce.equals(bankMessage.get(MessageHeaders.NONCE)))
            return;
        PublicKey bankPuKey = RSA.stringToPublicKey(bankMessage.get(MessageHeaders.RESPONSE));
        secure.setpublicKeyPartner(bankPuKey);
        setInitialKey(bankMessage.get(MessageHeaders.SESSIONKEY));

        //atm: E(initialKey, puAK)
        request = new HashMap<MessageHeaders, String>();
        PublicKey atmPuKey = secure.getPublicKey();
        request.put(MessageHeaders.RESPONSE, Base64.getEncoder().encodeToString(atmPuKey.getEncoded())); //rebuild securinyle
        byte[] req = SecurityUtils.encrypt(request, initialKey, Algorithms.AES.INSTANCE);
        out.writeObject(req);
        out.flush();

        //bank: 200
        response = (byte[]) in.readObject();
        bankMessage = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(response, initialKey, Algorithms.AES.INSTANCE);
        System.out.println(bankMessage.get(MessageHeaders.RESPONSECODE));
    }

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
    }
    @Override
    public void execute() {
        try {
            initialExchange();
            generateMasterKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
