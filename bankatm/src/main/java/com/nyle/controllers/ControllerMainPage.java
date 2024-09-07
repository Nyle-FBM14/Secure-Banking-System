package com.nyle.controllers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.nyle.ATM;
import com.nyle.ATMModel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ControllerMainPage extends Controller {
    private ATMModel model = ATMModel.getATMModelInstance();
    private ObjectOutputStream out = this.getOutStream();
    private ObjectInputStream in = this.getInStream();
    @FXML
    private Button buttonCancel;

    @FXML
    private Button buttonCheckBalance;

    @FXML
    private Button buttonDeposit;

    @FXML
    private Button buttonWithdraw;

    @SuppressWarnings("unchecked")
    private boolean requestLogout() {
        try {
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("REQUESTTYPE", "LOGOUT");
            request.put("CARDNUM", model.getCardNum());
            request.put("PIN", model.getPin());
            
            out.writeObject(request);
            out.flush();

            HashMap<String, String> response = (HashMap<String, String>) in.readObject();
            System.out.println(response.get("RESPONSE"));

            return response.get("RESPONSE").equals("Logout successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    private String requestCheckBalance() {
        try {
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("REQUESTTYPE", "CHECK");
            request.put("CARDNUM", model.getCardNum());
            request.put("PIN", model.getPin());
            
            out.writeObject(request);
            out.flush();

            HashMap<String, String> response = (HashMap<String, String>) in.readObject();
            System.out.println(response.get("RESPONSE"));

            return response.get("RESPONSE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private boolean requestWithdraw(String amount) {
        try {
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("REQUESTTYPE", "WITHDRAW");
            request.put("CARDNUM", model.getCardNum());
            request.put("PIN", model.getPin());
            request.put("WITHDRAWAMOUNT", amount);

            out.writeObject(request);
            out.flush();

            HashMap<String, String> response = (HashMap<String, String>) in.readObject();
            System.out.println(response.get("RESPONSE"));

            return response.get("RESPONSE").equals("Withdraw successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @FXML
    void cancel(ActionEvent event) {
        ATM.setRoot("login");
    }

    @FXML
    void checkBalance(ActionEvent event) {
        System.out.println(requestCheckBalance());
    }

    @FXML
    void deposit(ActionEvent event) {
        ATM.setRoot("depositPage");
    }

    @FXML
    void withdraw(ActionEvent event) {

    }
}
