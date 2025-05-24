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

import java.security.PublicKey;
import javax.crypto.SecretKey;

public class ATM {
    private String id;
    private PublicKey puKey;
    private SecretKey masterKey;
    
    public ATM(String id, PublicKey p, SecretKey m) {
        this.id = id;
        puKey = p;
        masterKey = m;
    }
    
    public String getID(){
        return id;
    }
    
    public PublicKey getPK(){
        return puKey;
    }
    
    public SecretKey getMK(){
        return masterKey;
    }
}
