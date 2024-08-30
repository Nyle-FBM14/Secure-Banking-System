package com.nyle;

public class BankUser {
    private String cardNum;
    private String pin;
    private double motion; //bank balance
    
    public BankUser(String cardNum, String pin, double motion){
        this.cardNum = cardNum;
        this.pin = pin;
        this.motion = motion;
    }
    public void deposit(double bands){
        motion += bands;
    }
    
    public void withdraw(double bands){
        motion -= bands;
    }
    
    public double checkBalance(){
        return motion;
    }
    
    public boolean authenticate(String pin){ //upgrade to compare a hash or sumn
        return (this.pin.equals(pin));
    }
    
    public String getCardNum(){
        return cardNum;
    }
}
