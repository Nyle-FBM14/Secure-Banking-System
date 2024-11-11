package com.security.enumerations;

/*
* SunPKCS11 algorithms
* RSA/ECB/PKCS1Padding
* AES/CBC/PKCS5Padding
* AES/ECB/PKCS5Padding or AES
*/
public enum Algorithms {
    RSA("RSA/ECB/PKCS1Padding"),
    AES("AES"),
    MAC_HASH("HmacSHA256"),
    RSA_SIGNATURE("SHA256withRSA"),
    HASH256("SHA-256");

    public final String INSTANCE;

    Algorithms(String INSTANCE){
        this.INSTANCE = INSTANCE;
    }
}
