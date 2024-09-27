package com.bankserver;

import javax.crypto.SecretKey;

public class Atm {
    private String id;
    private SecretKey key;
    
    public Atm(String id, SecretKey key) {
        this.id = id;
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SecretKey getSecretkey() {
        return key;
    }

    public void setSecretkey(SecretKey key) {
        this.key = key;
    }
}
