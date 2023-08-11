package com.example.app.Controllers;

import com.example.app.App;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    @FXML
    private volatile TextField deviceName, hostname, username;

    @FXML
    private PasswordField password;

    @FXML
    private Label message;

    private final int HOST = 0, USER = 1, PASS = 2;

    public SettingsController() {
        Task<Void> init = new Task<>() {

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
        new Thread(init).start();
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

        boolean titleUpdate = false;
        if (!App.currentPi.getTitle().equals(title)) {
            if (updateTitle(title) == -1) {
                return;
            } else {
                titleUpdate = true;
            }
        }

        boolean connectionUpdate = false;
        if (!App.currentPi.getHost().equals(host)) {
            if (updateSSHInfo(host, HOST) == -1)
                return;
            else
                connectionUpdate = true;
        }
        if (!App.currentPi.getUser().equals(user)) {
            if (updateSSHInfo(user, USER) == -1)
                return;
            else
                connectionUpdate = true;
        }
        if (!App.currentPi.getPass().equals(pass)) {
            if (updateSSHInfo(pass, PASS) == -1)
                return;
            else
                connectionUpdate = true;
        }

        if (titleUpdate || connectionUpdate) {
            if (connectionUpdate) {
                try {
                    App.currentPi.disconnect();
                    App.currentPi.connect();
                } catch (IOException e) {
                    Alert alert = App.createAlert("Failed to connect, please try again", Alert.AlertType.ERROR);
                    alert.show();
                    return;
                }
            }
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

    private int updateSSHInfo(String info, int type) {
        if (type == HOST) {
            if (App.alreadyExists(info)) {
                message.setText("*System already exists");
                return -1;
            } else {
                App.currentPi.setHost(info);
            }
            return 0;
        } else if (type == USER) {
            App.currentPi.setUser(info);
        } else if (type == PASS){
            App.currentPi.setPass(info);
        } else {
            return -1;
        }
        return 0;
    }
}
