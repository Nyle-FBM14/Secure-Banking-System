package com.security.enumerations;

public enum KeySizes {
    AES(256),
    RSA_KEYGEN(2048),
    //DH_KEY(2048),
    DH_PRIME(2048),
    MASTERKEY_ITERATIONS(3737);

    public final int SIZE;

    KeySizes(int SIZE){
        this.SIZE = SIZE;
    }
}
