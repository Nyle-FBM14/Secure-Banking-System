package com.atm.controllers;

import java.util.HashMap;

import com.atm.ATM;
import com.atm.ATMModel;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;
import com.nyle.enumerations.ResponseStatusCodes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ControllerWithdrawPage extends Controller {
    private ATMModel model = ATMModel.getATMModelInstance();

    @FXML
    private TextField fieldWithdrawAmount;

    @SuppressWarnings("unchecked")
    private boolean requestWithdraw(String amount) {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.WITHDRAW.toString());
            request.put(MessageHeaders.CARDNUM, model.getCardNum());
            request.put(MessageHeaders.PIN, model.getPin());
            request.put(MessageHeaders.WITHDRAWAMOUNT, amount);

            out.writeObject(request);
            out.flush();

            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) in.readObject();
            System.out.println(response.get(MessageHeaders.RESPONSECODE));

            return response.get(MessageHeaders.RESPONSECODE).equals(Integer.toString(ResponseStatusCodes.SUCCESS.CODE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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
            if(requestWithdraw(fieldWithdrawAmount.getText())){
                System.out.println("Make another withdrawal?");
            }
            else{
                System.out.println("Withdraw failed");;
            }
        }
        else{
            System.out.println("Input error");
        }
    }
}
