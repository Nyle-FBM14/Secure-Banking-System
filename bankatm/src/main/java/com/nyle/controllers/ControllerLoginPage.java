package com.nyle.controllers;

import com.nyle.ATMModel;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private ObjectOutputStream out = this.getOutStream();
    private ObjectInputStream in = this.getInStream();
    
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
            System.out.println(response.get(MessageHeaders.RESPONSE_CODE));

            return response.get(MessageHeaders.RESPONSE_CODE).equals(Integer.toString(ResponseStatusCodes.SUCCESS.code));
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
