package com.example.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private Button loginButton;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label errorMessage;

    @FXML
    protected void login() {
        if (username.getText().equals("") || password.getText().equals("")) {
            errorMessage.setVisible(true);
            return;
        }
        System.out.println("Logging in [" + username.getText() + ", " + password.getText() + "]...");

        App.getController().setDisplayName(username.getText());
        App.setLoggedIn();
        App.getController().updateUserOptions();

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
}
