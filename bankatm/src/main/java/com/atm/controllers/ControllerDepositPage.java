package com.atm.controllers;

import com.atm.ATM;
import com.atm.ATMModel;
import com.atm.commands.DepositCommand;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ControllerDepositPage extends Controller {
    private ATMModel model = ATMModel.getATMModelInstance();

    @FXML
    private TextField fieldDepositAmount;

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
            DepositCommand command = new DepositCommand(fieldDepositAmount.getText(), in, out, secure);
            command.execute();
        }
        else{
            System.out.println("Input error");
        }
    }
}
