package com.example.app.Controllers;

import com.example.app.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.Scanner;

public class AppController {

    @FXML
    private Parent gpio, files, metrics, shell;

    @FXML
    private GpioController gpioController;

    @FXML
    private FileController filesController;

    @FXML
    private MetricsController metricsController;

    @FXML
    private ShellController shellController;

    @FXML
    public Color x4;

    @FXML
    public Font x1, x3;

    @FXML
    private Label displayName, systemName;

    @FXML
    private MenuButton userOptions;

    @FXML
    private ScrollPane gpioPane, filePane, shellPane, metricPane;

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

    protected void addPi(RaspberryPi newPi) {
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

        metricsController.initMetrics();
        filesController.init();
    }

    private void createButtons(RaspberryPi newPi, VBox vBox) {
        CustomButton gpio = new CustomButton("GPIO", newPi);
        gpio.setOnAction(e -> swapPanels(gpioPane, gpio.getPi().getTitle(), gpio));

        CustomButton fileManager = new CustomButton("File Manager", newPi);
        fileManager.setOnAction(e -> swapPanels(filePane, fileManager.getPi().getTitle(), fileManager));

        CustomButton sshShell = new CustomButton("SSH Shell", newPi);
        sshShell.setOnAction(e -> swapPanels(shellPane, sshShell.getPi().getTitle(), sshShell));

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

        vBox.getChildren().addAll(gpio, fileManager, sshShell, metrics, settings, removeDevice);
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

        System.out.println(pane);
        if (pane != null) {
            switch (pane.getId()) {
                case "GPIO" -> toFront(App.GPIO);
                case "File Manager" -> toFront(App.FILE_MAN);
                case "SSH Shell" -> {toFront(App.SSH);}
                case "Metrics" -> toFront(App.METRICS);
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
    private AnchorPane metricsDetails, gpioDetails, fileDetails;

    @FXML
    private TextArea metricsTextArea, gpioTextArea;

    @FXML
    private Label pinLabel;

    @FXML
    private Slider levelSlider;

    @FXML
    private RadioButton inputMode, outputMode;

    @FXML
    private ToggleGroup functionGroup;

    @FXML
    private ChoiceBox<String> modeToggle, pullToggle;

    @FXML
    private TextField levelNonEditable, modeNonEditable, functionNonEditable, pullNonEditable;

    protected void toFront(int type) {
        if (type == App.METRICS) {
            metricsDetails.toFront();
            metricsDetails.setVisible(true);
            metricsTextArea.setText(App.currentPi.config);
        } else if (type == App.GPIO) {
            gpioDetails.toFront();
            gpioDetails.setVisible(true);
        } else if (type == App.FILE_MAN) {
            fileDetails.toFront();
            fileDetails.setVisible(true);
        } else if (type == App.SSH) {
            shellPane.toFront();
            shellController.testPTY();
        }
    }

    // GPIO
    public void selectBcmPin(int pin) {
        long start = System.currentTimeMillis();

        String config = App.currentPi.executeCommand("raspi-gpio funcs " + pin + "; raspi-gpio get " + pin);
        Scanner piOut = new Scanner(config);

        pinLabel.setText("GPIO " + pin);
        gpioTextArea.setText(piOut.nextLine() + "\n\n" + piOut.nextLine());

        config = piOut.nextLine();
        config = config.substring(config.indexOf(':') + 2);
        piOut.close();

        String[] data = config.split(" ");
        levelNonEditable.setText(data[0].substring(data[0].indexOf('=') + 1));
        modeNonEditable.setText(data[1].substring(data[1].indexOf('=') + 1));
        functionNonEditable.setText(data[2].substring(data[2].indexOf('=') + 1));
        pullNonEditable.setText(data[3].substring(data[3].indexOf('=') + 1));

        levelSlider.setValue(Double.parseDouble(levelNonEditable.getText()));
        if (functionNonEditable.getText().equalsIgnoreCase("INPUT")) {
            inputMode.setSelected(true);
        } else {
            outputMode.setSelected(true);
        }

        ObservableList<String> modes = FXCollections.observableArrayList(
                "0",
                "1",
                "2",
                "3",
                "4",
                "5"
        );

        modeToggle.setValue(modeNonEditable.getText());
        modeToggle.setItems(modes);
        pullToggle.setValue(pullNonEditable.getText());
        pullToggle.setItems(FXCollections.observableArrayList("UP", "DOWN"));

        System.out.println(System.currentTimeMillis() - start + "ms");
    }

    public void updateBcmPin() {
        Alert confirm = App.createAlert(null, Alert.AlertType.WARNING);
        confirm.setHeaderText("GPIO PIN MODIFICATION WARNING");
        confirm.setContentText(
                """
                WARNING! raspi-gpio set writes directly to the GPIO control registers
                ignoring whatever else may be using them (such as Linux drivers) - it is designed as a debug tool,
                only use it if you know what you are doing and at your own risk!
                """
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    double currentLevel = Double.parseDouble(levelNonEditable.getText());
                    int currentMode = Integer.parseInt(modeNonEditable.getText());
                    String currentFunction = ((RadioButton)functionGroup.getSelectedToggle()).getText();
                    String currentPull = pullToggle.getValue();

                    boolean updateLevel = levelSlider.getValue() != currentLevel;
                    boolean updateMode = currentMode != Integer.parseInt(modeToggle.getValue());
                    boolean updateFunc = !functionNonEditable.getText().equalsIgnoreCase(currentFunction);
                    boolean updatePull = !pullNonEditable.getText().equals(currentPull);

                    System.out.println(gpioController.currentPin);
                    System.out.println(updateLevel + " " + updateMode + " " + updateFunc + " " + updatePull);
                } catch (NumberFormatException nfe) {
                    Alert blank = App.createAlert(null, Alert.AlertType.ERROR);
                    blank.setHeaderText("There are blank fields");
                    blank.show();
                }
            }
        });
    }

    // METRICS
    public void updateMetrics(String[] timeAndTasks, double temp, String[][] diskMetrics, double[][] usageMetrics) {
        metricsController.updateMetrics(timeAndTasks, temp, diskMetrics, usageMetrics);
    }

    public void refreshMetricDetails() {
        App.currentPi.config = App.currentPi.execConfigCmd();
        metricsTextArea.setText(App.currentPi.config);
    }

    // FILE_MAN
    @FXML
    private Label fileName, fileType, filePath, fileSize, fileUid, fileGid, fileCr, fileMd;

    public void updateFileDetails(String[] details) {
        fileName.setText(details[0]);
        fileType.setText(details[1]);
        filePath.setText(details[2]);
        fileSize.setText(details[3]);
        fileUid.setText(details[4]);
        fileGid.setText(details[5]);
        fileCr.setText(details[6]);
        fileMd.setText(details[7]);
    }

}