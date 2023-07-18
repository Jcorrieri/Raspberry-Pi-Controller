package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
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
        System.out.println("Adding [" + selectModel.getSelectionModel().getSelectedItem()
                + ", " + title.getText() + ", " + ip.getText() + ", "
                + password.getText() + "," + port.getText() + "]...");

        Stage stage = (Stage) selectModel.getScene().getWindow();
        stage.close();
    }
}
