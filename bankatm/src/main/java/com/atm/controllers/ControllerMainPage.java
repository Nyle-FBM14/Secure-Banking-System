package com.atm.controllers;

import com.atm.ATM;
import com.atm.ATMModel;
import com.atm.commands.CheckBalanceCommand;
import com.atm.commands.LogoutCommand;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ControllerMainPage extends Controller {
    @SuppressWarnings("unused")
    private ATMModel model = ATMModel.getATMModelInstance();
    @FXML
    private Button buttonCancel;

    @FXML
    private Button buttonCheckBalance;

    @FXML
    private Button buttonDeposit;

    @FXML
    private Button buttonWithdraw;

    @FXML
    void cancel(ActionEvent event) {
        LogoutCommand command = new LogoutCommand(in, out, secure);
        command.execute();
        ATM.setRoot("login");
    }

    @FXML
    void checkBalance(ActionEvent event) {
        CheckBalanceCommand command = new CheckBalanceCommand(in, out, secure);
        command.execute();
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
