package com.atm.controllers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.nyle.SecureBanking;

public class Controller {
    protected ObjectOutputStream out;
    protected ObjectInputStream in;
    protected SecureBanking secure;

    public void setStreams(ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) { //added secure banking
        this.in = in;
        this.out = out;
        this.secure = secure;
    }
    public ObjectInputStream getInStream() { //unnecessary
        return in;
    }
    public ObjectOutputStream getOutStream() { //unnecessary
        return out;
    }
}
