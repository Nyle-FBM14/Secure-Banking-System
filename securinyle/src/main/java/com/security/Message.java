package com.security;

import com.security.enumerations.RequestTypes;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private RequestTypes requestType;
    private String message;
    private double amount;
    private LocalDateTime timestamp;
    private String nonce;
    private Key key;

    public Message(RequestTypes requestType, String message, double amount, LocalDateTime timestamp, String nonce, Key key) {
        this.requestType = requestType;
        this.message = message;
        this.amount = amount;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.key = key;
    }

    public RequestTypes getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestTypes requestType) {
        this.requestType = requestType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

}
