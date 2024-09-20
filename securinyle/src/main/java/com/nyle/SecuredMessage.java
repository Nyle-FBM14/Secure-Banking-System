package com.nyle;

import java.io.Serializable;

public class SecuredMessage implements Serializable {
    private byte[] message;
    private byte[] mac;
    private byte[] signature;
    
    public SecuredMessage(byte[] message, byte[] mac, byte[] signature) {
        this.message = message;
        this.mac = mac;
        this.signature = signature;
    }

    public byte[] getMessage() {
        return message;
    }

    public byte[] getMac() {
        return mac;
    }

    public byte[] getSignature() {
        return signature;
    }
}
