package com.atm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ATMGUI extends Application{
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        
        stage.setX(0);
        stage.setY(0);
        stage.setMinHeight(400);
        stage.setMinWidth(700);
        stage.setMaximized(true);

        stage.setTitle("360 Task Demolisher");

        scene = new Scene(loadFXML("loginPage"), 640, 480);
        //scene.getStylesheets().add("@../../styles.css"); //wrong path
        stage.setScene(scene);
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ATMGUI.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
