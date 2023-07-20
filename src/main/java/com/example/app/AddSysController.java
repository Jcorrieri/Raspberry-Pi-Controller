package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

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
        int portNum;

        if (selectModel.getSelectionModel().isEmpty()) {
            errorMessage.setText("*Must select a model");
            return;
        }
        if (titleStr.equals("") || ipStr.equals("") || passwordStr.equals("") || userStr.equals("")) {
            errorMessage.setText("*Fields cannot be blank");
            return;
        }
        if (alreadyExists(titleStr)) {
            errorMessage.setText("*System already exists with this title");
            return;
        }
        if (titleStr.length() > 20) {
            errorMessage.setText("*Title must no more than 20 characters");
            return;
        }

        try {
            portNum = Integer.parseInt(port.getText());
            if (portNum < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorMessage.setText("*Port must be a positive integer");
            return;
        }

        try {
            RaspberryPi raspberryPi = new RaspberryPi(model, titleStr, ipStr, userStr, passwordStr, portNum);
            App.getController().addSystemToUI(raspberryPi);
        } catch (IOException e) {
            errorMessage.setText("*Could not connect to device");
            return;
        }

        Stage stage = (Stage) selectModel.getScene().getWindow();
        stage.close();
    }
    
    private boolean alreadyExists(String title) {
        for (RaspberryPi pi : App.systems)
            if (pi.getTitle().equals(title))
                return true;
        return false;
    }    
}
