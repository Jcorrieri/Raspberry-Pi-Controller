package com.example.app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;

public class AppController {
    @FXML
    public Color x4;

    @FXML
    public Font x1, x3;

    @FXML
    private Label welcomeText, displayName, modelName;

    @FXML
    private MenuButton userOptions;

    @FXML
    private Pane gpioPane, testPane;

    @FXML
    protected void onHelloButtonClick() { welcomeText.setText("Welcome to JavaFX Application!"); }

    @FXML
    protected void addSystem() throws IOException { new AddSystem(); }

    @FXML
    protected void swapPanels(String type) {
        if (type.equals("Rpi4")) {
            gpioPane.toFront();
            modelName.setText("Raspberry Pi 4 Model B");
            gpioPane.setVisible(true);
            testPane.setVisible(false);
        } else {
            testPane.toFront();
            testPane.setVisible(true);
            gpioPane.setVisible(false);
        }
    }

    @FXML
    protected void accountOptions() { userOptions.show(); }

    @FXML
    protected void createLoginWindow() throws IOException { new Login(); }

    @FXML
    protected void exitApplication() { App.getPrimaryStage().close(); }
}