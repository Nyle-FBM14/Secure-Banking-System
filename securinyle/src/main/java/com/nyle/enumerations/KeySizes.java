package com.nyle.enumerations;

public enum KeySizes {
    AES(256),
    RSA_KEYGEN(2048),
    DH_KEY(2048),
    DH_PRIME(2048);

    public final int SIZE;

    KeySizes(int SIZE){
        this.SIZE = SIZE;
    }
}
