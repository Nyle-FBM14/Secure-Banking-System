package com.nyle.controllers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Controller {
    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    public void setStreams(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
        System.out.println(in);
        System.out.println(out);
    }
    public ObjectInputStream getInStream() {
        return in;
    }
    public ObjectOutputStream getOutStream() {
        return out;
    }
}
