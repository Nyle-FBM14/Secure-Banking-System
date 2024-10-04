package com.atm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import com.atm.commands.Command;
import com.atm.commands.ConnectCommand;
import com.atm.controllers.Controller;
import com.nyle.SecureBanking;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;

public class ATM extends Application {

    private static String id;
    private static SecureBanking secure = new SecureBanking();
    private static Scene scene;
    /*
        I think it's better to just make getter methods for the Object Input/Output Streams.
        But I wanted to experiment with super classes and subclasses.
        A Controller superclass will hold instance variables for the input and output streams.
        Individual controller subclasses should be able to access the streams in the superclass' getter methods.
    */
    private static ObjectInputStream inputStream;
    private static ObjectOutputStream outputStream;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setX(0);
        stage.setY(0);
        stage.setMinHeight(400);
        stage.setMinWidth(700);
        stage.setMaximized(true);
        stage.setTitle("Denarii Dispenser");

        stage.setOnCloseRequest(event -> {
            //event.consume();
            terminateConnection();
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
        controller.setStreams(inputStream, outputStream);
        return root;
    }

    @SuppressWarnings("unchecked")
    private static void terminateConnection() {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.END.toString());
            outputStream.writeObject(request);
            outputStream.flush();

            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) inputStream.readObject();
            System.out.println(response.get(MessageHeaders.RESPONSECODE));

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 15777;
        if(args.length == 1){
            id = args[0];
        }
        try (
                Socket socket = new Socket(hostName, portNumber);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
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