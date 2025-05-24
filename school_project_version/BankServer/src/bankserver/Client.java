/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankserver;

/**
 *
 * @author nmelegri
 */
public class Client {
    private String username; //used as ID
    private String password;
    private double motion;
    
    public Client(String username, String password, double motion){
        this.username = username;
        this.password = password;
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
    
    public boolean authenticate(String password){
        return (this.password.equals(password));
    }
    
    public String getUsername(){
        return username;
    }
}
