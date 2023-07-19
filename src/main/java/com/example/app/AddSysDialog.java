package com.example.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AddSysDialog {

    public AddSysDialog() throws IOException {
        FXMLLoader loginFxml = new FXMLLoader(LoginDialog.class.getResource("add-system.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setTitle("Add System");

        Scene scene = new Scene(loginFxml.load(), 335, 465);
        stage.setScene(scene);

        double centerXPosition = App.getPrimaryStage().getX() + (App.getPrimaryStage().getWidth() / 2d);
        double centerYPosition = App.getPrimaryStage().getY() + (App.getPrimaryStage().getHeight() / 2d);

        stage.setX(centerXPosition - 167.5);
        stage.setY(centerYPosition - 232.5);

        stage.show();
    }
}
