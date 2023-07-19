package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddSysController {

    @FXML
    private ComboBox<String> selectModel;

    @FXML
    private TextField title, ip, password, port;

    @FXML
    private Label errorMessage;

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
        String passwordStr = password.getText();
        int portNum;

        if (selectModel.getSelectionModel().isEmpty()) {
            errorMessage.setText("*Must select a model");
            return;
        }
        if (titleStr.equals("") || ipStr.equals("") || passwordStr.equals("")) {
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

        System.out.println("Adding [" + model + ", " + titleStr + ", "
                + ipStr + ", " + passwordStr + "," + portNum + "]...");

        RaspberryPi raspberryPi = new RaspberryPi(model, titleStr, ipStr, passwordStr, portNum);
        App.getController().addSystemToUI(raspberryPi);
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
