package com.example.app;

import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;

public class Login extends Alert{

    public Login() {
        super(AlertType.CONFIRMATION);
        setTitle("Sign In");

        String URL = String.valueOf(App.class.getResource("images/profile-icon.png"));
        setGraphic(new ImageView(URL));

        setContent();
        setHeaderText("Header Text");
        initOwner(App.stage);
        showAndWait();
    }

    private void setContent() {
        PasswordField password = new PasswordField();
        setContentText("Content Text" + password);
    }
}
