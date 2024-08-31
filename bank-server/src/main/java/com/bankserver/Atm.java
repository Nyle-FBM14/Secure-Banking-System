package com.bankserver;

import java.security.PublicKey;
import javax.crypto.SecretKey;

public class Atm {
    private String id;
    
    public Atm(String id) {
        this.id = id;
    }
    
    public String getID(){
        return id;
    }
}
