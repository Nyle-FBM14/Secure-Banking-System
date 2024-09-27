package com.bankserver;

import java.util.Queue;

public class Atm {
    private String id;
    private Queue<String> secretkeys;
    
    public Atm(String id, Queue<String> secretkeys) {
        this.id = id;
        this.secretkeys = secretkeys;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecretkey() {
        return secretkeys.poll();
    }

    public void addSecretkey(String secretkey) {
        secretkeys.offer(secretkey);
    }
}
