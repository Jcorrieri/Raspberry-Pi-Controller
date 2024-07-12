package com.example.app.Controllers;

import com.example.app.App;
import com.example.app.RaspberryPi;
import javafx.application.Platform;
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
    private TextField title, ip, username;

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

        if (model == null || model.isEmpty()) {
            errorMessage.setText("*Must select a model");
            return;
        }
        if (titleStr.equals("") || ipStr.equals("") || passwordStr.equals("") || userStr.equals("")) {
            errorMessage.setText("*Fields cannot be blank");
            return;
        }
        if (App.alreadyExists(titleStr) || App.alreadyExists(ipStr)) {
            errorMessage.setText("*System already exists");
            return;
        }
        if (titleStr.length() > 20) {
            errorMessage.setText("*Title must no more than 20 characters");
            return;
        }

        try {
            Stage stage = (Stage) selectModel.getScene().getWindow();

            Task<RaspberryPi> createPi = new Task<>() {
                @Override
                protected RaspberryPi call() throws Exception {
                    return new RaspberryPi(model, titleStr, ipStr, userStr, passwordStr);
                }
            };
            stage.setOnCloseRequest(e -> createPi.cancel(true));

            progressIndicator.setVisible(true);
            submitButton.setCursor(Cursor.WAIT);
            submitButton.setDisable(true);

            createPi.setOnSucceeded(e -> {
                App.getController().addPi(createPi.getValue());
                stage.close();
            });
            createPi.setOnFailed(e -> {
                errorMessage.setText("*Failed to connect");
                progressIndicator.setVisible(false);
                submitButton.setCursor(Cursor.DEFAULT);
                submitButton.setDisable(false);
            });

            new Thread(createPi).start();
        } catch (Exception e) {
            System.out.println("Thread Error");
            throw new RuntimeException(e);
        }
    }

    /**
     * Manually add Rpi system (For loading saved data)
     * @param data The model, title, hostname, username, and password in an array
     */
    protected void setSystem(String[] data) {
        String model = data[0];
        String titleStr = data[1];
        String ipStr = data[2];
        String userStr = data[3];

        Platform.runLater( () -> {
            selectModel.getSelectionModel().select(model);
            title.setText(titleStr);
            ip.setText(ipStr);
            username.setText(userStr);
        });
    }
}
