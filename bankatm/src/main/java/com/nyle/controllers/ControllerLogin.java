package com.nyle.controllers;

import com.nyle.ATMModel;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.nyle.ATM;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControllerLogin extends Controller{
    
    @FXML
    private Button buttonLogin;

    @FXML
    private TextField fieldCardNum;

    @FXML
    private PasswordField fieldPin;

    @SuppressWarnings("unchecked")
    private boolean executeRequest() {
        try {
            ObjectOutputStream out = this.getOutStream();
            ObjectInputStream in = this.getInStream();
            
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("REQUESTTYPE", "LOGIN");
            request.put("CARDNUM", fieldCardNum.getText());
            request.put("PIN", fieldPin.getText());
            
            out.writeObject(request);
            out.flush();

            HashMap<String, String> response = (HashMap<String, String>) in.readObject();
            System.out.println(response.get("RESPONSE"));

            return response.get("RESPONSE").equals("User login successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @FXML
    void login(ActionEvent event) {
        ATMModel model = ATMModel.getATMModelInstance();
        
        if(model.checkLogin(fieldCardNum.getText(), fieldPin.getText())){
            if(executeRequest()) {
                ATM.setRoot("mainPage");
            }
        }
        else{
            //show error
        }
    }
}
