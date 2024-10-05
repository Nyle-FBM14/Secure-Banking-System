package com.atm.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.atm.ATMModel;
import com.nyle.SecureBanking;

public class LogoutCommand implements Command{
    private ATMModel model = ATMModel.getATMModelInstance();
    private String amount;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureBanking secure;
    
    public LogoutCommand(String amount, ObjectInputStream in, ObjectOutputStream out, SecureBanking secure) {
        this.amount = amount;
        this.in = in;
        this.out = out;
        this.secure = secure;
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }
    
}
