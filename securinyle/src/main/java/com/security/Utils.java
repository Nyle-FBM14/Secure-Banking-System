package com.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import com.security.enumerations.KeySizes;

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

    public static String keyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        return random.generateSeed(16);
    }
    public static BigInteger generateLargePrime() {
        SecureRandom random = new SecureRandom();
        return BigInteger.probablePrime(KeySizes.DH_PRIME.SIZE, random);
    }
    public static String generateNonce() {
        SecureRandom random = new SecureRandom();
        int nonce = random.nextInt();
        return Integer.toString(nonce);
    }
}
