package com.nyle.controllers;

import java.util.HashMap;

import com.enumerations.MessageHeaders;
import com.enumerations.RequestTypes;
import com.enumerations.ResponseStatusCodes;
import com.nyle.ATM;
import com.nyle.ATMModel;

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
            System.out.println(response.get(MessageHeaders.RESPONSE_CODE));

            return response.get(MessageHeaders.RESPONSE_CODE).equals(Integer.toString(ResponseStatusCodes.SUCCESS.code));
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
