package com.bankserver.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.crypto.SecretKey;

import com.bankserver.Atm;
import com.bankserver.Bank;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecurityUtils;
import com.security.enumerations.RequestTypes;

public class ConnectCommand implements Command{
    private Bank bank = Bank.getBankInstance();
    @SuppressWarnings("unused")
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message message;
    private SecureBanking secure;

    public ConnectCommand (ObjectInputStream in, ObjectOutputStream out, Message message, SecureBanking secure) {
        this.in = in;
        this.out = out;
        this.message = message;
        this.secure = secure;
    }

    @Override
    public void execute(){
        try {
            //atm: ID || n
            Atm atm = bank.getAtm(message.getMessage()); //id
            SecretKey initialKey = atm.getInitialkey();

            secure.setInitialKey(initialKey);

            //bank: E(initialKey, puBK || f(n) || initialKey')
            atm.newInitialKey();
            message = new Message(RequestTypes.SECURE_CONNECTION, SecurityUtils.keyToString(atm.getInitialkey()), 0, null, SecurityUtils.nonceFunction(message.getNonce()));
            out.writeObject(secure.encryptAndSignMessage(message));
            out.flush();
            bank.writeAtms(); //new initial key is recorded

            System.out.println("Connection secured with ATM ID " + atm.getId());
        } catch (Exception e) {
            System.out.println("Connect failed.");
            e.printStackTrace();
        }
    }
}
