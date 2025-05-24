/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package registeringusers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author nmelegri
 */
public class RegisteringUsers {

    private static Scanner userInput;
    
    private static void registerUser(BufferedReader in, PrintWriter out) throws Exception{
        System.out.println("Registering new user... ");
        System.out.print("Username: ");
        String user = userInput.nextLine();
        System.out.print("Password: ");
        String pass = userInput.nextLine();
        System.out.print("Starting deposit: ");
        String cash = userInput.nextLine();
        
        String output = user + " " + pass + " " + cash;
        out.println(output);
        
        System.out.println(in.readLine());
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 15777;
        //instantiate Scanner for getting user input
        userInput = new Scanner(System.in);
        
        if (args.length == 2) {
            hostName = args[0];
            portNumber = Integer.parseInt(args[1]);
        }
        System.out.println("Host Name: " + hostName);
        System.out.println("Port #: " + portNumber);

        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out
                = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in
                = new BufferedReader(
                        new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn
                = new BufferedReader(
                        new InputStreamReader(System.in)))
        {
            do{
                try{
                    out.println("6");
                    registerUser(in, out);
                } catch(Exception e){
                    e.printStackTrace();
                }
                System.out.print("register again ('1' = yes, anything else = no)? ");
                String choice = userInput.nextLine();
                
                if(!choice.equals("1"))
                    break;
            }while(true);
            echoSocket.close();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to "
                    + hostName);
            System.exit(1);
        }
    }
    
}
