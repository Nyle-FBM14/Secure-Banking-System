package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.SecretKey;

import com.bankserver.Atm;
import com.bankserver.Bank;
import com.security.AES;
import com.security.RSA;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.SecurityUtils;
import com.security.Utils;
import com.security.enumerations.Algorithms;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.ResponseStatusCodes;

public class ConnectCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<MessageHeaders, String> request;
    private SecureBanking secure;

    public ConnectCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.request = request;
        this.secure = secure;
    }

    @SuppressWarnings("unchecked")
    public void initialExchange() throws Exception {
        //atm: ID || n
        String id = request.get(MessageHeaders.ID);
        String nonce = request.get(MessageHeaders.NONCE);
        Atm atm = bank.getAtm(id);
        SecretKey initialKey = atm.getInitialkey();

        //bank: E(initialKey, puBK || f(n) || initialKey')
        HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
        response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
        PublicKey bankPuKey = secure.getPublicKey();
        response.put(MessageHeaders.RESPONSE, Base64.getEncoder().encodeToString(bankPuKey.getEncoded())); //rebuild securinyle
        nonce = secure.nonceFunction(nonce);
        response.put(MessageHeaders.NONCE, nonce);

        //new initial key
        atm.newInitialKey();
        response.put(MessageHeaders.SESSIONKEY, Utils.keyToString(atm.getInitialkey()));
        byte[] res = SecurityUtils.encrypt(response, initialKey, Algorithms.AES.INSTANCE);
        out.writeObject(res);
        out.flush();
        bank.writeAtms();

        //atm: E(initialKey, puAK)
        byte[] atmResponse = (byte[]) in.readObject();
        request = (HashMap<MessageHeaders, String>) SecurityUtils.decrypt(atmResponse, initialKey, Algorithms.AES.INSTANCE);
        PublicKey atmPuKey = RSA.stringToPublicKey(request.get(MessageHeaders.RESPONSE));
        secure.setpublicKeyPartner(atmPuKey);

        //bank: 200
        response = new HashMap<MessageHeaders, String>();
        response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
        response.put(MessageHeaders.RESPONSECODE, ResponseStatusCodes.SUCCESS.toString());
        res = SecurityUtils.encrypt(response, initialKey, Algorithms.AES.INSTANCE);
        out.writeObject(res);
        out.flush();

        System.out.println("Connection secured with ATM ID " + id);
    }
    public void generateMasterKey() throws Exception {
        //exchange primes
        SecuredMessage atmdhPrimeMssg =  (SecuredMessage) in.readObject();
        SecuredMessage dhPrimeMssg = secure.generateDHPrimeMessage();
        out.writeObject(dhPrimeMssg);
        out.flush();
        

        //exchange public keys
        KeyPair kp = secure.generateDHKeyPair(atmdhPrimeMssg, dhPrimeMssg.getMessage(), true);
        SecuredMessage atmdhPuKeyMssg =  (SecuredMessage) in.readObject();
        SecuredMessage dhPuKeyMssg = secure.generateDHPublicKeyMessage(kp.getPublic());
        out.writeObject(dhPuKeyMssg);
        out.flush();
        

        //generate masterkey
        secure.generateMasterKey(atmdhPuKeyMssg, kp.getPrivate());
    }
    @Override
    public void execute(){
        try {
            initialExchange();
            generateMasterKey();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection failed");
        }
    }
}
