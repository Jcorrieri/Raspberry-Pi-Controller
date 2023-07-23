package com.example.app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
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
    private AnchorPane gpioPane, filePane, shellPane, scriptPane, metricPane, infoPane;

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
            new Popup(Popup.ADD_SYS);
        }
    }

    protected void addSystemToUI(RaspberryPi newPi) {
        App.systems.add(newPi);
        App.currentPi = newPi;

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
        removeDevice.setOnAction(e -> removeSystemFromUI(newPi));

        vBox.getChildren().addAll(gpio, fileManager, sshShell, scripts, metrics, deviceInfo, removeDevice);
        anchorPane.getChildren().add(vBox);
        AnchorPane.setLeftAnchor(vBox, 0d);
        AnchorPane.setRightAnchor(vBox, 0d);

        titledPane.setContent(anchorPane);
        titledPane.setExpanded(false);
        titledPane.setText(newPi.getTitle());
        titledPane.setId(newPi.getTitle());
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
            case "GPIO" -> button.setOnAction(e -> swapPanels(gpioPane, piId, button));
            case "File Manager" -> button.setOnAction(e -> swapPanels(filePane, piId, button));
            case "SSH Shell" -> button.setOnAction(e -> swapPanels(shellPane, piId, button));
            case "Scripts </>" -> button.setOnAction(e -> swapPanels(scriptPane, piId, button));
            case "CPU, RAM, and Disk Metrics" -> button.setOnAction(e -> swapPanels(metricPane, piId, button));
            case "Device Info" -> button.setOnAction(e -> swapPanels(infoPane, piId, button));
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
                systemContainer.getChildren().removeIf(node -> node.getId().equals(raspberryPi.getTitle()));
                App.systems.remove(raspberryPi);
                raspberryPi.disconnect();
                systemName.setText("No System Selected");
                swapPanels(null, null, null);
            }
        });
    }

    @FXML
    protected void swapPanels(AnchorPane pane, String piId, Button button) {
        for (RaspberryPi pi : App.systems)
            if (pi.getTitle().equals(piId) && pane != null) {
                App.currentPi = pi;
                systemName.setText(pi.getTitle() + " - " + pane.getId());
                break;
            }

        if (button != null) {
            if (App.getSelectedButton() != null)
                App.getSelectedButton().getStyleClass().remove("selected-system-button");
            button.getStyleClass().add("selected-system-button");
            App.selectButton(button);
        }

        for (Node node : stackPane.getChildren())
            node.setVisible(false);

        if (pane != null) {

            switch (pane.getId()) {
                case "GPIO" -> {}
                case "File Manager" -> {}
                case "SSH Shell" -> {}
                case "Scripts" -> {}
                case "Metrics" -> displayMetrics();
                case "Info" -> {}
                default -> {}
            }

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
    protected void createLoginWindow() throws IOException { new Popup(Popup.LOGIN); }

    @FXML
    protected void createSettingsWindow() throws IOException { new Popup(Popup.SETTINGS); }

    @FXML
    protected void createHelpWindow() throws IOException { new Popup(Popup.HELP); }

    @FXML
    protected void exitApplication() {
        for (RaspberryPi pi : App.systems)
            if (pi != null)
                pi.disconnect();
        App.getPrimaryStage().close();
    }

    protected void setDisplayName(String name) { displayName.setText(name); }

    /*  *  *  *  *  *  *  *  *
     * START SYSTEM METHODS  *
     *  *  *  *  *  *  *  *  */

    @FXML
    private AreaChart<String, Number> tempChart;

    @FXML
    private ProgressBar diskUsageIndicator1, diskUsageIndicator2, diskUsageIndicator3;

    @FXML
    private Label temperature, diskUsage1, diskUsage2, diskUsage3, disk1, disk2, disk3;

    private XYChart.Series<String, Number> temperatureData;

    private void displayMetrics() {
        temperatureData = new XYChart.Series<>();
        temperatureData.setName("Core");
        tempChart.getData().clear();
        tempChart.getData().add(temperatureData);

        if (!App.currentPi.isMonitoring())
            App.currentPi.initMonitor();
    }

    protected void updateMetrics(String time, double temp, String[][] diskMetrics) {
        // Update GUI on JavaFX app thread
        if (!App.getSelectedButton().getText().equals("CPU, RAM, and Disk Metrics"))
            return;

        Platform.runLater(() -> {
            temperatureData.getData().add(new XYChart.Data<>(time, temp));
            temperature.setText(temp + "°C");
            if (temperatureData.getData().size() == 8)
                temperatureData.getData().remove(0);

            for (int i = 1; i < 4; i++) {
                String[] disk = diskMetrics[i - 1];

                String name = disk[0];
                String diskUsage = disk[1];
                double percentage = Double.parseDouble(disk[2]);

                switch(i) {
                    case 1 -> {
                        disk1.setText(name);
                        diskUsage1.setText(diskUsage);
                        diskUsageIndicator1.setProgress(percentage);
                    }
                    case 2 -> {
                        disk2.setText(name);
                        diskUsage2.setText(diskUsage);
                        diskUsageIndicator2.setProgress(percentage);
                    }
                    case 3 -> {
                        disk3.setText(name);
                        diskUsage3.setText(diskUsage);
                        diskUsageIndicator3.setProgress(percentage);
                    }
                }
            }
        });
    }
}