package com.nyle;

import java.util.HashMap;

import com.nyle.enumerations.MessageHeaders;

public class SecuredMessage {
    private HashMap<MessageHeaders, String> message;
    private byte[] macString;
    
    public SecuredMessage(HashMap<MessageHeaders, String> message, byte[] macString) {
        this.message = message;
        this.macString = macString;
    }

    public HashMap<MessageHeaders, String> getMessage() {
        return message;
    }

    public byte[] getMacString() {
        return macString;
    }
}
