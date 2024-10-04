package com.atm.controllers;

import java.util.HashMap;

import com.atm.ATM;
import com.atm.ATMModel;
import com.atm.commands.DepositCommand;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;
import com.nyle.enumerations.ResponseStatusCodes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ControllerDepositPage extends Controller {
    private ATMModel model = ATMModel.getATMModelInstance();

    @FXML
    private TextField fieldDepositAmount;

    private void requestDeposit(String amount) {
        DepositCommand command = new DepositCommand(model, amount, in, out, secure);
        command.execute();
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
        if(model.checkAmount(fieldDepositAmount.getText())){
            requestDeposit(fieldDepositAmount.getText());
        }
        else{
            System.out.println("Input error");
        }
    }
}
