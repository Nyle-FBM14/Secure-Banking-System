package com.nyle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKey;

import com.nyle.enumerations.KeySizes;

public class Utils {
    public static byte[] serialize(Object object) {
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
    public static Object deserialize(byte[] buffer) {
        try {
            ByteArrayInputStream arrayToStream = new ByteArrayInputStream(buffer);
            ObjectInputStream deserializer = new ObjectInputStream(arrayToStream);
            return deserializer.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static BigInteger getLargePrime() {
        SecureRandom randNum = new SecureRandom();
        return BigInteger.probablePrime(KeySizes.DH_PRIME.SIZE, randNum);
    }
}
