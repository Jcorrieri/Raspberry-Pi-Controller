package com.example.app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Controller {
    @FXML
    public Color x4;

    @FXML
    public Font x1, x3;

    @FXML
    private Label welcomeText, displayName;

    @FXML
    private MenuButton userOptions;

    @FXML
    protected void onHelloButtonClick() { welcomeText.setText("Welcome to JavaFX Application!"); }

    @FXML
    protected void addSystem() { System.out.println("Add System"); }

    @FXML
    protected void accountOptions() { userOptions.show(); }

    @FXML
    protected void signInAlert() { new Login(); }
}