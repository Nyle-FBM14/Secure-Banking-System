package com.bankserver;

import java.security.MessageDigest;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;

import com.security.SecurityUtils;
import com.security.enumerations.Algorithms;

public class EncryptingHandler extends Handler implements AutoCloseable {
    private FileHandler handler;
    private byte[] keyBytes = "Ave Christus Rex!".getBytes();
    private SecretKey key;

    public EncryptingHandler() {
        try {
            handler = new FileHandler("bank-server\\src\\main\\resources\\bank_logs.txt", true);
            handler.setFormatter(new SimpleFormatter());

            MessageDigest hashFunction = MessageDigest.getInstance(Algorithms.HASH256.INSTANCE);
            key = new SecretKeySpec(hashFunction.digest(keyBytes), Algorithms.AES.INSTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void publish(LogRecord record) {
        String message = "Original Log: " + record.getMessage() + "\nEncrypted Log: " + SecurityUtils.encryptString(record.getMessage(), key, Algorithms.AES.INSTANCE);
        record.setMessage(message);
        handler.publish(record);
    }

    @Override
    public void flush() {
        handler.flush();
    }

    @Override
    public void close() throws SecurityException {
        handler.close();
    }
}
