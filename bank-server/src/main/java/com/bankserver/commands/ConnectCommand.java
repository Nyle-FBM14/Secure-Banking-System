package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;

import com.bankserver.Atm;
import com.bankserver.Bank;
import com.nyle.AES;
import com.nyle.SecureBanking;
import com.nyle.SecuredMessage;
import com.nyle.SecurityUtils;
import com.nyle.Utils;
import com.nyle.enumerations.Algorithms;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.ResponseStatusCodes;

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

    public void initialExchange() throws Exception {
        //atm: ID || n
        String id = request.get(MessageHeaders.ID);
        String nonce = request.get(MessageHeaders.NONCE);
        Atm atm = bank.getAtm(id);
        
        //bank: E(initialKey, puBK || f(n) || initialKey')
        HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
        response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
        PublicKey bankPuKey = secure.getPublicKey();
        response.put(MessageHeaders.RESPONSE, Base64.getEncoder().encodeToString(bankPuKey.getEncoded())); //rebuild securinyle
        nonce = secure.nonceFunction(nonce);
        request.put(MessageHeaders.NONCE, nonce);
        request.put(MessageHeaders.SESSIONKEY, Utils.keyToString(AES.generateKey()));
        byte[] res = SecurityUtils.encrypt(response, atm.getSecretkey(), Algorithms.AES.INSTANCE);
        out.writeObject(res);
        out.flush();

        //atm: E(initialKey, puAK)
        byte[] atmResponse = (byte[]) in.readObject();
        PublicKey atmPuKey = (PublicKey) SecurityUtils.decrypt(atmResponse, atm.getSecretkey(), Algorithms.AES.INSTANCE);
        secure.setpublicKeyPartner(atmPuKey);

        //bank: 200
        response = new HashMap<MessageHeaders, String>();
        response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
        response.put(MessageHeaders.RESPONSECODE, ResponseStatusCodes.SUCCESS.toString());
        res = SecurityUtils.encrypt(response, atm.getSecretkey(), Algorithms.AES.INSTANCE);
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
            System.out.println("Connection failed");
        }
    }
}
