package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.SecretKey;

import com.bankserver.Atm;
import com.bankserver.Bank;
import com.nyle.AES;
import com.nyle.RSA;
import com.nyle.SecureBanking;
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
    private PublicKey bankPuKey;
    private SecureBanking secure;

    public ConnectCommand (ObjectInputStream in, ObjectOutputStream out, HashMap<MessageHeaders, String> request, SecureBanking secure, PublicKey bankPuKey) {
        this.in = in;
        this.out = out;
        this.request = request;
        this.bankPuKey = bankPuKey;
        this.secure = secure;
    }

    @Override
    public void execute(){
        try {
            //atm: ID || n
            String id = request.get(MessageHeaders.ID);
            String nonce = request.get(MessageHeaders.NONCE);
            Atm atm = bank.getAtm(id);
            
            //bank: E(initialKey, puBK || f(n) || initialKey')
            HashMap<MessageHeaders, String> response = new HashMap<MessageHeaders, String>();
            response.put(MessageHeaders.REQUESTTYPE, request.get(MessageHeaders.REQUESTTYPE));
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

            System.out.println("Connection secured with ATM ID " + id);
        } catch (Exception e) {
            System.out.println("Connection failed");
        }
        
    }
}
