package com.nyle;

import java.util.HashMap;

import com.nyle.enumerations.MessageHeaders;

public class SecuredMessage {
    private HashMap<MessageHeaders, String> message;
    private byte[] mac;
    
    public SecuredMessage(HashMap<MessageHeaders, String> message, byte[] mac) {
        this.message = message;
        this.mac = mac;
    }

    public HashMap<MessageHeaders, String> getMessage() {
        return message;
    }

    public byte[] getMac() {
        return mac;
    }
}
