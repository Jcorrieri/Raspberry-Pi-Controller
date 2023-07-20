package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddSysController {

    @FXML
    private ComboBox<String> selectModel;

    @FXML
    private TextField title, ip, username, port;

    @FXML
    private PasswordField password;

    @FXML
    private Label errorMessage;

    @FXML
    private Button submitButton;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    protected void initComboBox() {
        ObservableList<String> models = FXCollections.observableArrayList(
                "Raspberry Pi 4 Model B",
                "Raspberry Pi 3 Model A+",
                "Raspberry Pi Zero 2 W"
        );
        selectModel.setItems(models);
    }

    @FXML
    protected void add() {
        String model = selectModel.getSelectionModel().getSelectedItem();
        String titleStr = title.getText();
        String ipStr = ip.getText();
        String userStr = username.getText();
        String passwordStr = password.getText();
        String portStr = port.getText();

        if (!validate(model, titleStr, ipStr, userStr, passwordStr, portStr))
            return;
        int portNum = Integer.parseInt(portStr);

        try {
            Task<RaspberryPi> createPi = new Task<>() {
                @Override
                protected RaspberryPi call() throws Exception {
                    return new RaspberryPi(model, titleStr, ipStr, userStr, passwordStr, portNum);
                }
            };
            new Thread(createPi).start();
            Stage stage = (Stage) selectModel.getScene().getWindow();
            stage.setOnCloseRequest(e -> createPi.cancel(true));

            progressIndicator.setVisible(true);
            submitButton.setCursor(Cursor.WAIT);
            submitButton.setDisable(true);

            createPi.setOnSucceeded(e -> {
                App.getController().addSystemToUI(createPi.getValue());
                stage.close();
            });
            createPi.setOnFailed(e -> {
                errorMessage.setText("*Failed to connect");
                progressIndicator.setVisible(false);
                submitButton.setCursor(Cursor.DEFAULT);
                submitButton.setDisable(false);
            });
        } catch (Exception e) {
            System.out.println("Thread Error");
            throw new RuntimeException(e);
        }
    }

    private boolean validate(String model, String title, String host, String user, String password, String port) {
        if (model.isEmpty()) {
            errorMessage.setText("*Must select a model");
            return false;
        }
        if (title.equals("") || host.equals("") || password.equals("") || user.equals("")) {
            errorMessage.setText("*Fields cannot be blank");
            return false;
        }
        if (alreadyExists(title) || alreadyExists(host)) {
            errorMessage.setText("*System already exists");
            return false;
        }
        if (title.length() > 20) {
            errorMessage.setText("*Title must no more than 20 characters");
            return false;
        }

        try {
            int portNum = Integer.parseInt(port);
            if (portNum < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorMessage.setText("*Port must be a positive integer");
            return false;
        }
        return true;
    }

    private boolean alreadyExists(String identifier) {
        for (RaspberryPi pi : App.systems)
            if (pi.getTitle().equals(identifier) || pi.getHost().equals(identifier))
                return true;
        return false;
    }
}
