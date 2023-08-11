package com.example.app.Controllers;

import com.example.app.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;

public class AppController {

    @FXML
    private Parent metrics, gpio;

    @FXML
    private MetricsController metricsController;

    @FXML
    private GpioController gpioController;

    @FXML
    public Color x4;

    @FXML
    public Font x1, x3;

    @FXML
    private Label displayName, systemName;

    @FXML
    private MenuButton userOptions;

    @FXML
    private ScrollPane gpioPane, filePane, shellPane, scriptPane, metricPane;

    @FXML
    private StackPane panels, details;

    @FXML
    private VBox systemContainer;

    @FXML
    protected void createAddSystemWindow() throws IOException {
        if (App.systems.size() >= App.MAX_SYSTEMS) {
            Alert alert = App.createAlert("System Limit Reached", Alert.AlertType.ERROR);
            alert.show();
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

        createButtons(newPi, vBox);

        anchorPane.getChildren().add(vBox);
        AnchorPane.setLeftAnchor(vBox, 0d);
        AnchorPane.setRightAnchor(vBox, 0d);

        titledPane.setContent(anchorPane);
        titledPane.setExpanded(false);
        titledPane.setText(newPi.getTitle());
        titledPane.setId(newPi.getTitle());
        titledPane.getStylesheets().add(String.valueOf(App.class.getResource("css/app.css")));
        titledPane.getStyleClass().add("system-titled-pane");
        systemContainer.getChildren().add(titledPane);
        App.currentPi.setTitledPane(titledPane);
    }

    private void createButtons(RaspberryPi newPi, VBox vBox) {
        CustomButton gpio = new CustomButton("GPIO", newPi);
        gpio.setOnAction(e -> swapPanels(gpioPane, gpio.getPi().getTitle(), gpio));

        CustomButton fileManager = new CustomButton("File Manager", newPi);
        fileManager.setOnAction(e -> swapPanels(filePane, fileManager.getPi().getTitle(), fileManager));

        CustomButton sshShell = new CustomButton("SSH Shell", newPi);
        sshShell.setOnAction(e -> swapPanels(shellPane, sshShell.getPi().getTitle(), sshShell));

        CustomButton scripts = new CustomButton("Scripts </>", newPi);
        scripts.setOnAction(e -> swapPanels(scriptPane, scripts.getPi().getTitle(), scripts));

        CustomButton metrics = new CustomButton("CPU, RAM, and Disk Metrics", newPi);
        metrics.setOnAction(e -> swapPanels(metricPane, metrics.getPi().getTitle(), metrics));

        CustomButton settings = new CustomButton("Settings", newPi);
        settings.setOnAction(e -> {
            try {
                App.selectButton(settings);
                systemName.setText(settings.getPi().getTitle() + " - " + "Settings");

                for (Node node : panels.getChildren())
                    node.setVisible(false);

                new Popup(Popup.SETTINGS);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        CustomButton removeDevice = new CustomButton("Remove Device", newPi);
        removeDevice.getStyleClass().add("system-remove-button");
        removeDevice.setOnAction(e -> removeSystemFromUI(newPi));

        vBox.getChildren().addAll(gpio, fileManager, sshShell, scripts, metrics, settings, removeDevice);
    }

    @FXML
    protected void removeSystemFromUI(RaspberryPi raspberryPi) {
        Alert confirm = App.createAlert(null, Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Are you sure you want to remove this device?");
        confirm.setContentText("Remove " + raspberryPi.getTitle());

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

    public void toggleSystemButtons(RaspberryPi pi, boolean enabled) {
        AnchorPane anchorPane = (AnchorPane) pi.getTitledPane().getContent();
        VBox buttons = (VBox) anchorPane.getChildren().get(0);

        if (enabled) {
            for (Node node : buttons.getChildren()){
                CustomButton b = (CustomButton) node;
                b.setDisable(false);
            }
        } else {
            for (Node node : buttons.getChildren()) {
                CustomButton b = (CustomButton) node;
                if (b.getText().equals("Settings") || b.getText().equals("Remove Device"))
                    continue;
                b.setDisable(true);
            }
        }
    }

    @FXML
    protected void swapPanels(ScrollPane pane, String piId, CustomButton customButton) {
        long start = System.currentTimeMillis();

        for (RaspberryPi pi : App.systems)
            if (pi.getTitle().equals(piId) && pane != null) {
                App.currentPi = pi;
                systemName.setText(pi.getTitle() + " - " + pane.getId());
                break;
            }

        if (customButton != null)
            App.selectButton(customButton);

        for (Node node : panels.getChildren())
            node.setVisible(false);
        for (Node node : details.getChildren())
            node.setVisible(false);

        if (pane != null) {
            switch (pane.getId()) {
                case "GPIO" -> App.getController().toFront(App.GPIO);
                case "File Manager" -> {}
                case "SSH Shell" -> {}
                case "Scripts" -> {}
                case "Metrics" -> metricsController.displayMetrics();
                default -> {}
            }

            pane.toFront();
            pane.setVisible(true);
        }
        System.out.println("Swapped panels in: " + (System.currentTimeMillis() - start) + "ms");
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
    protected void createHelpWindow() throws IOException { new Popup(Popup.HELP); }

    @FXML
    public void exitApplication() {
        for (RaspberryPi pi : App.systems)
            if (pi != null)
                pi.disconnect();
        App.getPrimaryStage().close();
    }

    protected void setDisplayName(String name) { displayName.setText(name); }

    public void setSystemName(String text) { systemName.setText(text); }

    /*  *  *  *  *  *  *  *  *
     * START DETAIL METHODS  *
     *  *  *  *  *  *  *  *  */

    @FXML
    private AnchorPane metricsDetails, gpioDetails;

    @FXML
    private TextArea metricsTextArea, gpioTextArea;

    @FXML
    private Label pinLabel;

    @FXML
    private TextField levelNonEditable, modeNonEditable, functionNonEditable, pullNonEditable;

    protected void toFront(int type) {
        if (type == App.METRICS) {
            metricsDetails.toFront();
            metricsDetails.setVisible(true);
            metricsTextArea.setText(App.currentPi.metricInfo);
        } else if (type == App.GPIO) {
            gpioDetails.toFront();
            gpioDetails.setVisible(true);
        }
    }

    public void updateMetrics(String[] timeAndTasks, double temp, String[][] diskMetrics, double[][] usageMetrics) {
        metricsController.updateMetrics(timeAndTasks, temp, diskMetrics, usageMetrics);
    }

    public void updateGPIO(int pin) {
        pinLabel.setText("GPIO " + pin);
        gpioTextArea.setText("Information about pin#" + pin);

        String currentConfig = App.currentPi.executeCommand("raspi-gpio get " + pin);
        currentConfig = currentConfig.substring(currentConfig.indexOf(':') + 2);

        String[] data = currentConfig.split(" ");
        levelNonEditable.setText(data[0].substring(data[0].indexOf('=') + 1));
        modeNonEditable.setText(data[1].substring(data[1].indexOf('=') + 1));
        functionNonEditable.setText(data[2].substring(data[2].indexOf('=') + 1));
        pullNonEditable.setText(data[3].substring(data[3].indexOf('=') + 1));
    }
}