package com.example.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AddSystem {

    public AddSystem() throws IOException {
        FXMLLoader loginFxml = new FXMLLoader(Login.class.getResource("add-system.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setTitle("Add System");

        Scene scene = new Scene(loginFxml.load(), 335, 465);
        stage.setScene(scene);

        double centerXPosition = App.primaryStage.getX() + App.primaryStage.getWidth()/2d;
        double centerYPosition = App.primaryStage.getY() + App.primaryStage.getHeight()/2d;

        stage.setX(centerXPosition - 167.5);
        stage.setY(centerYPosition - 232.5);

        stage.show();
    }
}
