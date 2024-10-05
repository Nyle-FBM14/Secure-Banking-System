package com.atm.controllers;

import com.atm.ATM;
import com.atm.ATMModel;
import com.atm.commands.WithdrawCommand;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ControllerWithdrawPage extends Controller {
    private ATMModel model = ATMModel.getATMModelInstance();

    @FXML
    private TextField fieldWithdrawAmount;

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
        if(model.checkAmount(fieldWithdrawAmount.getText())){
            WithdrawCommand command = new WithdrawCommand(fieldWithdrawAmount.getText(), in, out, secure);
            command.execute();
        }
        else{
            System.out.println("Input error");
        }
    }
}
