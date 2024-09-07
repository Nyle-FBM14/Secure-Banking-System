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

public class ControllerWithdrawPage extends Controller {
    private ATMModel model = ATMModel.getATMModelInstance();
    private ObjectOutputStream out = this.getOutStream();
    private ObjectInputStream in = this.getInStream();

     @FXML
    private TextField fieldWithdrawAmount;

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
    void buttonInputAmount(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        fieldWithdrawAmount.setText(clickedButton.getText());
    }

    @FXML
    void cancelWithdraw(ActionEvent event) {
        ATM.setRoot("mainPage");
    }

    @FXML
    void confirmWithdraw(ActionEvent event) {

    }
}
