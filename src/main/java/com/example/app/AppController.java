package com.example.app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.StackedAreaChart;
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
    private AreaChart<String, Number> tempChart, voltageChart;

    @FXML
    private ProgressBar diskUsageIndicator;

    @FXML
    private Label tempLabel, diskUsageLabel;

    private XYChart.Series<String, Number> temperatureData, v1, v2, v3, v4;

    private void displayMetrics() {
        temperatureData = new XYChart.Series<>();
        temperatureData.setName("Core");
        tempChart.getData().clear();
        tempChart.getData().add(temperatureData);

        v1 = new XYChart.Series<>();
        v1.setName("Core");

        v2 = new XYChart.Series<>();
        v2.setName("SDRAM Core");

        v3 = new XYChart.Series<>();
        v3.setName("SDRAM I/O");

        v4 = new XYChart.Series<>();
        v4.setName("SDRAM Physical");

        voltageChart.getData().clear();
        voltageChart.getData().addAll(v1, v2, v3, v4);

        if (!App.currentPi.isMonitoring())
            App.currentPi.initMonitor();
    }

    protected void updateMetrics(String time, double temp, String[] diskMetrics, double[] voltage) {
        // Update GUI on JavaFX app thread
        if (!App.getSelectedButton().getText().equals("CPU, RAM, and Disk Metrics"))
            return;

        Platform.runLater(() -> {
            temperatureData.getData().add(new XYChart.Data<>(time, temp));
            tempLabel.setText(temp + "°C");
            if (temperatureData.getData().size() == 8)
                temperatureData.getData().remove(0);

            String diskUsage = diskMetrics[0];
            double percentage = Double.parseDouble(diskMetrics[1]);

            diskUsageLabel.setText(diskUsage);
            diskUsageIndicator.setProgress(percentage);

            v1.getData().add(new XYChart.Data<>(time, voltage[0]));
            v2.getData().add(new XYChart.Data<>(time, voltage[1]));
            v3.getData().add(new XYChart.Data<>(time, voltage[2]));
            v4.getData().add(new XYChart.Data<>(time, voltage[3]));

            // all voltage data is updated at the same rate
            if (v1.getData().size() == 8) {
                v1.getData().remove(0);
                v2.getData().remove(0);
                v3.getData().remove(0);
                v4.getData().remove(0);
            }
        });
    }
}