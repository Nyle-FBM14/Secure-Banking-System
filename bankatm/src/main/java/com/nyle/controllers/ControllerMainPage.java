package com.nyle.controllers;

import java.util.HashMap;

import com.enumerations.MessageHeaders;
import com.enumerations.RequestTypes;
import com.nyle.ATM;
import com.nyle.ATMModel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ControllerMainPage extends Controller {
    private ATMModel model = ATMModel.getATMModelInstance();
    @FXML
    private Button buttonCancel;

    @FXML
    private Button buttonCheckBalance;

    @FXML
    private Button buttonDeposit;

    @FXML
    private Button buttonWithdraw;

    @SuppressWarnings("unchecked")
    private boolean requestLogout() {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.LOGOUT.toString());
            request.put(MessageHeaders.CARDNUM, model.getCardNum());
            request.put(MessageHeaders.PIN, model.getPin());
            
            out.writeObject(request);
            out.flush();

            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) in.readObject();
            System.out.println(response.get(MessageHeaders.RESPONSE_CODE));

            return response.get(MessageHeaders.RESPONSE_CODE).equals("200");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    private String requestCheckBalance() {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.CHECKBALANCE.toString());
            request.put(MessageHeaders.CARDNUM, model.getCardNum());
            request.put(MessageHeaders.PIN, model.getPin());
            
            out.writeObject(request);
            out.flush();

            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) in.readObject();
            System.out.println(response.get(MessageHeaders.RESPONSE_CODE));

            return response.get(MessageHeaders.RESPONSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @FXML
    void cancel(ActionEvent event) {
        /*
        if(requestLogout()){
            ATM.setRoot("login");
        } //*/
        ATM.setRoot("login");
    }

    @FXML
    void checkBalance(ActionEvent event) {
        System.out.println(requestCheckBalance());
    }

    @FXML
    void deposit(ActionEvent event) {
        ATM.setRoot("depositPage");
    }

    @FXML
    void withdraw(ActionEvent event) {
        ATM.setRoot("withdrawPage");
    }
}
