package com.example.app;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
    private Label displayName, systemName;

    @FXML
    private MenuButton userOptions;

    @FXML
    private Pane gpioPane, filePane, shellPane, scriptPane, metricPane, infoPane;

    @FXML
    private StackPane stackPane;

    @FXML
    private VBox systemContainer;

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
        App.currentPi = raspberryPi;

        TitledPane titledPane = new TitledPane();
        AnchorPane anchorPane = new AnchorPane();
        VBox vBox = new VBox();
        vBox.setFillWidth(true);
        vBox.setMaxWidth(Double.MAX_VALUE);

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
        AnchorPane.setLeftAnchor(vBox, 0d);
        AnchorPane.setRightAnchor(vBox, 0d);

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
        button.setMaxWidth(Double.MAX_VALUE);
        String piId = App.currentPi.getTitle(); // currentPi is already set correctly by this point in the thread

        switch (name) {
            case "GPIO" -> button.setOnAction(e -> swapPanels(gpioPane, piId));
            case "File Manager" -> button.setOnAction(e -> swapPanels(filePane, piId));
            case "SSH Shell" -> button.setOnAction(e -> swapPanels(shellPane, piId));
            case "Scripts </>" -> button.setOnAction(e -> swapPanels(scriptPane, piId));
            case "CPU, RAM, and Disk Metrics" -> button.setOnAction(e -> swapPanels(metricPane, piId));
            case "Device Info" -> button.setOnAction(e -> swapPanels(infoPane, piId));
        }

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
                systemName.setText("No System Selected");
                swapPanels(null, null);
            }
        });
    }

    @FXML
    protected void swapPanels(Pane pane, String piId) {
        for (RaspberryPi pi : App.systems)
            if (pi.getTitle().equals(piId)) {
                App.currentPi = pi;
                systemName.setText(pi.getTitle() + " - " + pane.getId());
                break;
            }

        for (Node node : stackPane.getChildren())
            node.setVisible(false);
        if (pane != null) {
            pane.toFront();
            pane.setVisible(true);
        }
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