package com.example.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
    private VBox systemContainer;

    @FXML
    protected void onHelloButtonClick() { welcomeText.setText("Welcome to JavaFX Application!"); }

    @FXML
    protected void createAddSystemWindow() throws IOException {
        if (App.systems.size() >= App.MAX_SYSTEMS) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "System Limit Reached");

            double centerXPosition = App.getPrimaryStage().getX() + (App.getPrimaryStage().getWidth() / 2d);
            double centerYPosition = App.getPrimaryStage().getY() + (App.getPrimaryStage().getHeight() / 2d);
            alert.setX(centerXPosition - 188);
            alert.setY(centerYPosition - 92.5);

            alert.show();
            System.out.println(alert.getWidth() + ", " + alert.getHeight());
        } else {
            new AddSysDialog();
        }
    }

    protected void addSystemToUI(RaspberryPi raspberryPi) {
        App.systems.add(raspberryPi);

        TitledPane titledPane = new TitledPane();
        AnchorPane anchorPane = new AnchorPane();
        VBox vBox = new VBox();

        Button gpio = createSystemButton("GPIO");
        Button fileManager = createSystemButton("File Manager");
        Button sshShell = createSystemButton("SSH Shell");
        Button scripts = createSystemButton("Scripts </>");
        Button metrics = createSystemButton("CPU, RAM, and Disk Metrics");
        Button deviceInfo = createSystemButton("Device Info");

        Button removeDevice = createSystemButton("Remove Device");
        removeDevice.getStyleClass().add("system-remove-button");
        removeDevice.setOnAction(e -> removeSystemFromUI(raspberryPi));

        vBox.getChildren().addAll(gpio, fileManager, sshShell, scripts, metrics, deviceInfo, removeDevice);
        anchorPane.getChildren().add(vBox);

        titledPane.setContent(anchorPane);
        titledPane.setExpanded(false);
        titledPane.setText(raspberryPi.getTitle());
        titledPane.setId(raspberryPi.getTitle());
        titledPane.getStylesheets().add(String.valueOf(AppController.class.getResource("app.css")));
        titledPane.getStyleClass().add("system-titled-pane");
        systemContainer.getChildren().add(titledPane);
    }

    private Button createSystemButton(String name) {
        Button button = new Button(name);
        button.getStylesheets().add(String.valueOf(AppController.class.getResource("app.css")));
        button.getStyleClass().add("system-dropdown-button");
        return button;
    }

    @FXML
    protected void removeSystemFromUI(RaspberryPi raspberryPi) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Are you sure you want to remove this device?");
        confirm.setContentText("Remove " + raspberryPi.getTitle());

        double centerXPosition = App.getPrimaryStage().getX() + (App.getPrimaryStage().getWidth() / 2d);
        double centerYPosition = App.getPrimaryStage().getY() + (App.getPrimaryStage().getHeight() / 2d);
        confirm.setX(centerXPosition - 188);
        confirm.setY(centerYPosition - 92.5);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                App.systems.remove(raspberryPi);
                systemContainer.getChildren().removeIf(node -> node.getId().equals(raspberryPi.getTitle()));
            }
        });
    }

    @FXML
    protected void swapPanels(String type) {
        gpioPane.toFront();
        gpioPane.setVisible(true);
        modelName.setText(type);
    }

    @FXML
    protected void accountOptions() { userOptions.show(); }

    protected void updateUserOptions() {
        CustomMenuItem menuItem = (CustomMenuItem) userOptions.getItems().get(0);
        Hyperlink link = (Hyperlink) menuItem.getContent();

        if (App.isLoggedIn()) {
            link.setText("Sign Out");
            link.setOnAction(e -> {
                App.setLoggedOut();
                updateUserOptions();
            });
        } else {
            link.setText("Sign In");
            link.setOnAction(e -> {
                try { createLoginWindow(); } catch (IOException ex) { throw new RuntimeException(ex); }
            });
            displayName.setText("Guest User");
        }
    }

    @FXML
    protected void createLoginWindow() throws IOException { new LoginDialog(); }

    @FXML
    protected void exitApplication() { App.getPrimaryStage().close(); }

    protected void setDisplayName(String name) { displayName.setText(name); }
}