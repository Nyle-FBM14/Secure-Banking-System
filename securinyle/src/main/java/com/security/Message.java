package com.security;

import java.io.Serializable;

import com.security.enumerations.RequestTypes;
//import java.security.Key;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private RequestTypes requestType;
    private String message;
    private double amount;
    private String timestamp;
    private String nonce;

    public Message(RequestTypes requestType, String message, double amount, String timestamp, String nonce) {
        this.requestType = requestType;
        this.message = message;
        this.amount = amount;
        this.timestamp = timestamp;
        this.nonce = nonce;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
