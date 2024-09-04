package com.nyle;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Controller {
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void setInStream(ObjectInputStream in) {
        this.in = in;
    }
    public void setOutStream(ObjectOutputStream out) {
        this.out = out;
    }
    public ObjectInputStream getInStream() {
        return in;
    }
    public ObjectOutputStream getOutStream() {
        return out;
    }
}
