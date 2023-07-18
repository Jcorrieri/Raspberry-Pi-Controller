package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddSystemController {

    @FXML
    private ComboBox<String> selectModel;

    @FXML
    private TextField title, ip, password, port;

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
            Alert nullModel = new Alert(Alert.AlertType.ERROR, "No Model Selected");
            nullModel.initOwner(App.getPrimaryStage());
            nullModel.show();
            return;
        }
        if (titleStr.equals("") || ipStr.equals("") || passwordStr.equals("")) {
            Alert nullText = new Alert(Alert.AlertType.ERROR, "Fields cannot be null");
            nullText.initOwner(App.getPrimaryStage());
            nullText.show();
            return;
        }

        try {
            portNum = Integer.parseInt(port.getText());
        } catch (NumberFormatException e) {
            Alert badPort = new Alert(Alert.AlertType.ERROR, "Port must be a postitive integer");
            badPort.initOwner(App.getPrimaryStage());
            badPort.show();
            return;
        }

        System.out.println("Adding [" + model + ", " + titleStr + ", "
                + ipStr + ", " + passwordStr + "," + portNum + "]...");

        App.getController().swapPanels();
        Stage stage = (Stage) selectModel.getScene().getWindow();
        stage.close();
    }
}
