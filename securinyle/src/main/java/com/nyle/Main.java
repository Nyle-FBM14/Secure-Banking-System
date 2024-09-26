package com.nyle;

import java.security.KeyPair;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.nyle.enumerations.MessageHeaders;

public class Main {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        SecureBanking bank = new SecureBanking();
        SecureBanking atm = new SecureBanking();

        bank.setpublicKeyPartner(atm.getPublicKey());
        atm.setpublicKeyPartner(bank.getPublicKey());

        SecuredMessage bankPrimeM = bank.generateDHPrimeMessage();
        SecuredMessage atmPrimeM = atm.generateDHPrimeMessage();

        KeyPair bankKeyPair = bank.generateDHKeyPair(atmPrimeM, bankPrimeM.getMessage(), true);
        KeyPair atmKeyPair = atm.generateDHKeyPair(bankPrimeM, atmPrimeM.getMessage(), false);

        SecuredMessage bankPubKey = bank.generateDHPublicKeyMessage(bankKeyPair.getPublic());
        SecuredMessage atmPubKey = atm.generateDHPublicKeyMessage(atmKeyPair.getPublic());

        bank.generateMasterKey(atmPubKey, bankKeyPair.getPrivate());
        atm.generateMasterKey(bankPubKey, atmKeyPair.getPrivate());

        HashMap<MessageHeaders, String> credentials = new HashMap<MessageHeaders, String>();
        credentials.put(MessageHeaders.CARDNUM, "0000111133337777");
        credentials.put(MessageHeaders.PIN, "3737");
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        credentials.put(MessageHeaders.TIMESTAMP, sdf.format(ts));
        SecuredMessage credMssg = atm.generateCredentialsMessage(credentials);

        bank.generateMasterSessionKey(credMssg);
        SecuredMessage sessionKeys = bank.deriveSessionAndMacKeysAndGenerateMessage();
        atm.getDerivedKeys(sessionKeys);

        HashMap<MessageHeaders, String> test = new HashMap<MessageHeaders, String>();
        test.put(MessageHeaders.CARDNUM, "0000111133337777");
        test.put(MessageHeaders.PIN, "3737");
        test.put(MessageHeaders.ID, "Nyle");

        SecuredMessage send = atm.encryptAndSignMessage(test);
        HashMap<MessageHeaders, String> receive = (HashMap<MessageHeaders, String>) bank.decryptAndVerifyMessage(send);

        System.out.println(receive.get(MessageHeaders.CARDNUM));
        System.out.println(receive.get(MessageHeaders.PIN));
        System.out.println(receive.get(MessageHeaders.ID));
    }
}