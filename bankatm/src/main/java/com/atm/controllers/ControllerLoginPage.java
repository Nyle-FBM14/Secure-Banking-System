package com.atm.controllers;

import com.atm.ATM;
import com.atm.ATMModel;
import com.atm.commands.LoginCommand;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControllerLoginPage extends Controller{
    private ATMModel model = ATMModel.getATMModelInstance();
    
    @FXML
    private Button buttonLogin;

    @FXML
    private TextField fieldCardNum;

    @FXML
    private PasswordField fieldPin;

    @FXML
    void login(ActionEvent event) {
        if(model.checkLogin(fieldCardNum.getText(), fieldPin.getText())) {
            LoginCommand command = new LoginCommand(in, out, secure, fieldCardNum.getText(), fieldPin.getText());
            command.execute();
            ATM.setRoot("mainPage");
        }
    }
}
