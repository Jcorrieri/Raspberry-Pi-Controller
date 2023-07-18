package com.example.app;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    Button loginButton;

    @FXML
    TextField username;

    @FXML
    PasswordField password;

    @FXML
    protected void login() {
        if (username.getText().equals("") || password.getText().equals("")) {
            Alert nullText = new Alert(Alert.AlertType.ERROR, "Fields cannot be null");
            nullText.initOwner(App.getPrimaryStage());
            nullText.show();
            return;
        }

        System.out.println("Logging in [" + username.getText() + ", " + password.getText() + "]...");
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
}
