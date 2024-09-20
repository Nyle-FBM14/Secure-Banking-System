package com.nyle;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import com.nyle.enumerations.Algorithms;

public class BankServerSecurity {
    private SecretKey masterKey; //initially created with each new ATM connection
    private SecretKey masterSessionKey; //created with each client login - a session key for encryption key and a mac key will be derived from this
    private SecretKey sessionKey;
    private SecretKey macKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey publicKeyATM;

    public BankServerSecurity() {
        masterKey = AES.generateKey();

        KeyPair keypair = RSA.generateRSAkeypair();
        privateKey = keypair.getPrivate();
        publicKey = keypair.getPublic();
    }

    public SecuredMessage GenerateDHPrimeMessage() {
        byte[] prime = Utils.serialize(Utils.getLargePrime());
        byte[] signedPrime = RSA.signDigitalSignature(privateKey, prime);
        return new SecuredMessage(prime, signedPrime);
    }
    public SecuredMessage GenerateDHPublicKeyMessage(SecuredMessage dhPrime) {
        if(RSA.verifyDigitalSignature(publicKeyATM, dhPrime.getMessage(), dhPrime.getMessageIntegrityAuthentication())) {
            
        }
        return null;
    }
}
