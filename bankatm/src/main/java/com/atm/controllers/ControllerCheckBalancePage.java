package com.atm.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.atm.ATM;
import com.atm.ATMModel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ControllerCheckBalancePage extends Controller implements Initializable {

    @FXML
    private Button buttonBack;

    @FXML
    private Label labelBalance;

    @FXML
    void back(ActionEvent event) {
        ATM.setRoot("mainPage");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ATMModel model = ATMModel.getATMModelInstance();
        labelBalance.setText(model.getBalance());
    }
}