package com.nyle.controllers;

import com.nyle.ATMModel;

import java.util.HashMap;

import com.enumerations.MessageHeaders;
import com.enumerations.RequestTypes;
import com.enumerations.ResponseStatusCodes;
import com.nyle.ATM;

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

    @SuppressWarnings("unchecked")
    private boolean requestLogin() {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.LOGIN.toString());
            request.put(MessageHeaders.CARDNUM, fieldCardNum.getText());
            request.put(MessageHeaders.PIN, fieldPin.getText());
            
            out.writeObject(request);
            out.flush();

            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) in.readObject();
            System.out.println(response.get(MessageHeaders.RESPONSECODE));

            return response.get(MessageHeaders.RESPONSECODE).equals(Integer.toString(ResponseStatusCodes.SUCCESS.code));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @FXML
    void login(ActionEvent event) {
        if(model.checkLogin(fieldCardNum.getText(), fieldPin.getText())){
            if(requestLogin()) {
                model.setCredentials(fieldCardNum.getText(), fieldPin.getText());
                ATM.setRoot("mainPage");
            }
        }
        else{
            //show error
            System.out.println("login error");
        }
    }
}
