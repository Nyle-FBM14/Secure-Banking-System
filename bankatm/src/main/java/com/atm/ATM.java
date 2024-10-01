package com.atm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.atm.controllers.Controller;
import com.nyle.enumerations.MessageHeaders;
import com.nyle.enumerations.RequestTypes;

public class ATM extends Application {

    private static String id;
    private static SecretKey initialKey;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    private static void secureConnection(ObjectInputStream in, ObjectOutputStream out) {
        try {
            HashMap<MessageHeaders, String> request = new HashMap<MessageHeaders, String>();
            request.put(MessageHeaders.REQUESTTYPE, RequestTypes.SECURE_CONNECTION.toString());
            request.put(MessageHeaders.ID, id);

            out.writeObject(request);
            out.flush();

            HashMap<MessageHeaders, String> response = (HashMap<MessageHeaders, String>) in.readObject();
            System.out.println(response.get(MessageHeaders.RESPONSECODE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 15777;
        if (args.length == 1) {
            File atmFile = new File("bankatm\\src\\main\\resources\\com\\nyle\\" + args[0]);
            String atmData;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(atmFile));
                while((atmData = reader.readLine()) != null) {
                    String[] data = atmData.split(",");
                    id = data[0];
                    initialKey = new SecretKeySpec(Base64.getDecoder().decode(data[1]), "AES");
                    hostName = data[2];
                    portNumber = Integer.parseInt(data[3]);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            secureConnection(in, out);
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