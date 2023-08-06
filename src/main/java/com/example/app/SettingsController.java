package com.example.app;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML
    private volatile TextField deviceName, hostname, username;

    @FXML
    private PasswordField password;

    public SettingsController() {
        Task<Void> task = new Task<>() {

            @Override
            protected Void call() {
                while (deviceName == null || hostname == null || username == null)
                    Thread.onSpinWait();
                Platform.runLater(() -> {
                    deviceName.setText(App.currentPi.getTitle());
                    hostname.setText(App.currentPi.getHost());
                    username.setText(App.currentPi.getUser());
                    password.setText(App.currentPi.getPass());
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void shutdown() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Are you sure you want to shut down this device?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                App.currentPi.shutdown();
            }
        });
    }

    @FXML
    private void reset() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Are you sure you want to restart this device?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                App.currentPi.restart();
            }
        });
    }

    @FXML
    private void applyChanges() {

    }
}
