package com.atm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import com.atm.commands.ConnectCommand;
import com.atm.controllers.Controller;
import com.security.SecureBanking;

public class ATM extends Application {

    private static String id = "ATM1";
    private static Scene scene;
    /*
        I think it's better to just make getter methods for the Object Input/Output Streams.
        But I wanted to experiment with super classes and subclasses.
        A Controller superclass will hold instance variables for the input and output streams.
        Individual controller subclasses should be able to access the streams in the superclass' getter methods.
    */
    private static ObjectInputStream inputStream;
    private static ObjectOutputStream outputStream;
    private static SecureBanking secure = new SecureBanking();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setX(0);
        stage.setY(0);
        stage.setMinHeight(400);
        stage.setMinWidth(700);
        stage.setMaximized(true);
        stage.setTitle("Denarii Dispenser");

        stage.setOnCloseRequest(event -> {
            event.consume();
            //Command command = new EndCommand(inputStream, outputStream, secure);
            //command.execute();
            stage.close();
        });

        scene = new Scene(loadFXML("login"), 700, 400);
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) {
        try {
            scene.setRoot(loadFXML(fxml));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ATM.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();
        controller.setStreams(inputStream, outputStream, secure);
        return root;
    }

    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 15777;
        if(args.length == 1){
            id = args[0];
            System.out.println(id);
        }
        try (
                Socket socket = new Socket(hostName, portNumber);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            )
        {
            inputStream = in;
            outputStream = out;
            ConnectCommand command = new ConnectCommand(in, out, secure, id);
            command.execute();
            launch();
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