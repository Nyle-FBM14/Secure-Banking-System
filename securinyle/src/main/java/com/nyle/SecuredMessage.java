package com.nyle;

import java.io.Serializable;

public class SecuredMessage implements Serializable {
    private byte[] message;
    private byte[] messageIntegrityAuthentication; //mac or digital signature
    
    public SecuredMessage(byte[] message, byte[] messageIntegrityAuthentication) {
        this.message = message;
        this.messageIntegrityAuthentication = messageIntegrityAuthentication;
    }
    public byte[] getMessage() {
        return message;
    }
    public byte[] getMessageIntegrityAuthentication() {
        return messageIntegrityAuthentication;
    }
}
