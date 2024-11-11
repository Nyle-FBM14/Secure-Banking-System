package com.bankserver;

import javax.crypto.SecretKey;
import com.security.AES;

public class Atm {
    private String id;
    private SecretKey initialKey;
    
    public Atm(String id, SecretKey initialKey) {
        this.id = id;
        this.initialKey = initialKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SecretKey getInitialkey() {
        return initialKey;
    }

    public void newInitialKey() {
        this.initialKey = AES.generateKey();
    }
}
