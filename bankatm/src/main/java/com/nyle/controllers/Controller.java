package com.nyle.controllers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Controller {
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void setStreams(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
    }
    public ObjectInputStream getInStream() {
        return in;
    }
    public ObjectOutputStream getOutStream() {
        return out;
    }
}
