package com.nyle;

public class ATMModel {
    private static volatile ATMModel modelInstance;
    private final int CARD_NUM_LEN = 16;
    private final int MIN_PIN_LEN = 4;
    private String cardNum;
    private String pin;

    private ATMModel(){
        //idk what 
    }
    public static ATMModel getATMModelInstance(){
        ATMModel modelTemp = modelInstance;
        if(modelTemp == null) {
            synchronized(ATMModel.class){
                modelTemp = modelInstance;
                if(modelTemp == null)
                modelInstance = modelTemp = new ATMModel();
            }
        }
        return modelTemp;
    }

    public void setCredentials(String cardNum, String pin) {
        this.cardNum = cardNum;
        this.pin = pin;
    }
    public String getCardNum() {
        return cardNum;
    }
    public String getPin() {
        return pin;
    }
    private boolean isEmpty(String str){
        return (str.length() == 0 || str == null);
    }
    private boolean isNumeric(String str){
        for(int i = 0; i < str.length(); i++){
            if( !(Character.isDigit(str.charAt(i))) ){
                System.out.println("Non-numeric input.");
                return false;
            }
        }
        return true;
    }
    public boolean checkLogin(String cardNum, String pin){
        if(isEmpty(cardNum) || isEmpty(pin)){
            System.out.println("Missing credentials.");
            return false;
        }

        if(!(isNumeric(cardNum) && isNumeric(pin))){
            System.out.println("Non-numeric input.");
            return false;
        }

        return (cardNum.length() == CARD_NUM_LEN && pin.length() >= MIN_PIN_LEN);
    }
}
