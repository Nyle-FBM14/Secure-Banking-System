package com.atm.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.security.AES;
import com.security.Message;
import com.security.SecureBanking;
import com.security.SecuredMessage;
import com.security.SecurityUtils;
import com.security.enumerations.RequestTypes;

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
        BufferedReader reader = new BufferedReader(new FileReader("bankatm\\src\\main\\resources\\com\\atm\\atm_" + id + "_data.txt"));
        while((atmData = reader.readLine()) != null) {
            String[] data = atmData.split(",");
            this.initialKey = new SecretKeySpec(Base64.getDecoder().decode(data[1]), "AES");
        }
        reader.close();
    }
    private void setInitialKey(Key key) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("bankatm\\src\\main\\resources\\com\\atm\\atm_" + id + "_data.txt"));
        writer.write(id + "," + SecurityUtils.keyToString(key));
        writer.close();
    }
    @Override
    public void execute() {
        try {
            //atm: ID || n
            Message message = new Message(RequestTypes.SECURE_CONNECTION, id, 0, null, SecurityUtils.generateNonce());
            out.writeObject(message);
            out.flush();

            secure.setInitialKey(initialKey);

            //bank: E(initialKey, f(n) || initialKey')
            SecuredMessage sMessage = (SecuredMessage) in.readObject();
            message = secure.decryptAndVerifyMessage(sMessage);
            
            this.setInitialKey(AES.stringToKey(message.getMessage()));

            //acknowledgements?
        } catch (Exception e) {
            System.out.println("Connect failed.");
            e.printStackTrace();
        }
    }
}
