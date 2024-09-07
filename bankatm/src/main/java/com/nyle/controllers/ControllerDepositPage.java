package com.nyle.controllers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.nyle.ATM;
import com.nyle.ATMModel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ControllerDepositPage extends Controller {
    private ATMModel model = ATMModel.getATMModelInstance();
    private ObjectOutputStream out = this.getOutStream();
    private ObjectInputStream in = this.getInStream();

    @FXML
    private TextField fieldDepositAmount;

    @SuppressWarnings("unchecked")
    private boolean requestDeposit(String amount) {
        try {
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("REQUESTTYPE", "DEPOSIT");
            request.put("CARDNUM", model.getCardNum());
            request.put("PIN", model.getPin());
            request.put("DEPOSITAMOUNT", amount);

            out.writeObject(request);
            out.flush();

            HashMap<String, String> response = (HashMap<String, String>) in.readObject();
            System.out.println(response.get("RESPONSE"));

            return response.get("RESPONSE").equals("Deposit successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    void buttonInputAmount(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        fieldDepositAmount.setText(clickedButton.getText());
    }

    @FXML
    void cancelDeposit(ActionEvent event) {
        ATM.setRoot("mainPage");
    }

    @FXML
    void confirmDeposit(ActionEvent event) {

    }
}
