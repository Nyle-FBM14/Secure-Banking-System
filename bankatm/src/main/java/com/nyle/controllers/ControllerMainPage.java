package com.nyle.controllers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.nyle.ATM;
import com.nyle.ATMModel;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;

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
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.CHECKBALANCE.toString());
            request.put(MessageHeaders.CARDNUM, model.getCardNum());
            request.put(MessageHeaders.PIN, model.getPin());
            
            out.writeObject(request);
            out.flush();

            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) in.readObject();
            System.out.println(response.get(MessageHeaders.RESPONSE));

            return response.get(MessageHeaders.RESPONSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        ATM.setRoot("withdrawPage");
    }
}
