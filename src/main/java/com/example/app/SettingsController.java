package com.example.app;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SettingsController {

    @FXML
    private volatile TextField deviceName, hostname, username;

    @FXML
    private PasswordField password;

    @FXML
    private Label message;

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
        String title = deviceName.getText();
        String host = hostname.getText();
        String user = username.getText();
        String pass = password.getText();

        if (title.equals("") || host.equals("") || user.equals("") || pass.equals("")) {
            message.setText("*Fields cannot be empty");
            return;
        }

        boolean updated = false;
        if (!App.currentPi.getTitle().equals(title)) {
            if (updateTitle(title) == -1) {
                return;
            } else {
                updated = true;
            }
        }

        if (!App.currentPi.getHost().equals(host)) {
            if (updateHost(host) == -1) {
                return;
            } else {
                updated = true;
            }
        }

        if (!App.currentPi.getUser().equals(user)) {
            App.currentPi.setUser(user);
            updated = true;
        }
        if (!App.currentPi.getPass().equals(pass)) {
            App.currentPi.setPass(pass);
            updated = true;
        }

        if (updated) {
            Stage stage = (Stage) deviceName.getScene().getWindow();
            stage.setTitle(title);
            message.setText("Changes saved!");
        }
    }

    private int updateTitle(String title) {
        if (App.alreadyExists(title)) {
            message.setText("*System already exists");
            return -1;
        } else if (title.length() > 20) {
            message.setText("*Title must no more than 20 characters");
            return -1;
        } else {
            App.currentPi.setTitle(title);
        }
        return 0;
    }

    private int updateHost(String host) {
        if (App.alreadyExists(host)) {
            message.setText("*System already exists");
            return -1;
        } else {
            App.currentPi.setHost(host);
        }
        return 0;
    }
}
