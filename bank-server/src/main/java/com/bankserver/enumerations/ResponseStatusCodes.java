package com.bankserver.enumerations;

public enum ResponseStatusCodes {
    SUCCESS(200),
    ERROR(500),
    TIMEOUT(501),
    INVALID_NONCE(502),
    DECRYPTION_ERROR(503),
    INVALID_MAC(504),
    INVALID_CARD(505),
    INVALID_PIN(506);

    public final int code;

    ResponseStatusCodes(int code){
        this.code = code;
    }
}