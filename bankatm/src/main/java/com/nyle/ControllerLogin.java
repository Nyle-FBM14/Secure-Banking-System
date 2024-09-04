package com.nyle;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControllerLogin {
    
    @FXML
    private Button buttonLogin;

    @FXML
    private TextField fieldCardNum;

    @FXML
    private PasswordField fieldPin;

    @FXML
    void login(ActionEvent event) {
        ATMModel model = ATMModel.getATMModelInstance();
        
        if(model.checkLogin(fieldCardNum.getText(), fieldPin.getText())){
            App.setRoot("mainPage");

        }
        else{
            //show error
        }
    }
}
