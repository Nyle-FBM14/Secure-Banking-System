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
import com.security.Message;
import com.security.RSA;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.SecurityUtils;
import com.security.Utils;
import com.security.enumerations.Algorithms;
import com.security.enumerations.MessageHeaders;
import com.security.enumerations.RequestTypes;
import com.security.enumerations.ResponseStatusCodes;

public class ConnectCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message inMessage;
    private SecureBanking secure;

    public ConnectCommand (ObjectInputStream in, ObjectOutputStream out, Message inMessage, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.inMessage = inMessage;
        this.secure = secure;
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
            //atm: ID || n
            Atm atm = bank.getAtm(inMessage.getMessage()); //id
            SecretKey initialKey = atm.getInitialkey();

            //bank: E(initialKey, puBK || f(n) || initialKey')
            atm.newInitialKey();
            Message outMessage = new Message(RequestTypes.SECURE_CONNECTION, "", 0, null, Utils.nonceFunction(inMessage.getNonce()), atm.getInitialkey());
            out.writeObject(SecurityUtils.encrypt(outMessage, initialKey, Algorithms.AES.INSTANCE));
            out.flush();
            bank.writeAtms(); //new initial key is recorded

            System.out.println("Connection secured with ATM ID " + atm.getId());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection failed");
        }
    }
}
