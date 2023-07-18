package com.example.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Login {

    public Login() throws IOException {
        FXMLLoader loginFxml = new FXMLLoader(Login.class.getResource("login.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setTitle("Login Dialog");

        Scene scene = new Scene(loginFxml.load(), 300, 335);
        stage.setScene(scene);

        double centerXPosition = App.getPrimaryStage().getX() + App.getPrimaryStage().getWidth()/2d;
        double centerYPosition = App.getPrimaryStage().getY() + App.getPrimaryStage().getHeight()/2d;

        stage.setX(centerXPosition - 150);
        stage.setY(centerYPosition - 167.5);

        stage.show();
    }
}
