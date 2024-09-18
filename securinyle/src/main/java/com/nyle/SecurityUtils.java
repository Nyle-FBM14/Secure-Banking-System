package com.nyle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class SecurityUtils {
    private byte[] serialize(Object object) {
        try {
            ByteArrayOutputStream byteArrMaker = new ByteArrayOutputStream();
            ObjectOutputStream serializer = new ObjectOutputStream(byteArrMaker);
            serializer.writeObject(object);
            serializer.flush();
            return byteArrMaker.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings("unused")
    private Object deserialize(byte[] buffer) {
        try {
            ByteArrayInputStream arrayToStream = new ByteArrayInputStream(buffer);
            ObjectInputStream deserializer = new ObjectInputStream(arrayToStream);
            return deserializer.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] makeMac(Object message, SecretKey macKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(macKey);
            byte[] macBytes = mac.doFinal(serialize(message));
            return macBytes;
            //return Base64.getEncoder().encodeToString(macBytes); //for String - import java.util.Base64;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifyMac(Object message, byte[] receivedMac, SecretKey macKey) {
        byte[] generatedMac = makeMac(message, macKey);
        return generatedMac.equals(receivedMac);
    }
}
