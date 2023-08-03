package com.example.app;

import javafx.application.Platform;
import javafx.concurrent.Task;
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
    private ScrollPane gpioPane, filePane, shellPane, scriptPane, metricPane, infoPane;

    @FXML
    private StackPane stackPane, details;

    @FXML
    private AnchorPane metricsDetails;

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
        Button power = createSystemButton("Shutdown/Reboot");

        Button removeDevice = createSystemButton("Remove Device");
        removeDevice.getStyleClass().add("system-remove-button");
        removeDevice.setOnAction(e -> removeSystemFromUI(newPi));

        vBox.getChildren().addAll(gpio, fileManager, sshShell, scripts, metrics, power, removeDevice);
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
        String piId = App.currentPi.getTitle();

        switch (name) {
            case "GPIO" -> button.setOnAction(e -> swapPanels(gpioPane, piId, button));
            case "File Manager" -> button.setOnAction(e -> swapPanels(filePane, piId, button));
            case "SSH Shell" -> button.setOnAction(e -> swapPanels(shellPane, piId, button));
            case "Scripts </>" -> button.setOnAction(e -> swapPanels(scriptPane, piId, button));
            case "CPU, RAM, and Disk Metrics" -> button.setOnAction(e -> swapPanels(metricPane, piId, button));
            case "Shutdown/Reboot" -> button.setOnAction(e -> swapPanels(infoPane, piId, button));
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
    protected void swapPanels(ScrollPane pane, String piId, Button button) {
        long start = System.currentTimeMillis();

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
        for (Node node : details.getChildren())
            node.setVisible(false);

        if (pane != null) {

            switch (pane.getId()) {
                case "GPIO" -> {}
                case "File Manager" -> {}
                case "SSH Shell" -> {}
                case "Scripts" -> {}
                case "Metrics" -> displayMetrics();
                case "Shutdown/Reboot" -> {}
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
    private ProgressBar diskIndicator1, diskIndicator2, diskIndicator3;
    @FXML
    private ProgressBar cpuUsageRatio, ramUsageRatio, swapUsageRatio;

    @FXML
    private Label cpuUsagePercent, cpuAvg1, cpuAvg5, cpuAvg15;
    @FXML
    private Label ramUsagePercent, ramTotal, ramUsed, ramFree;
    @FXML
    private Label swapUsagePercent, swapTotal, swapUsed, swapFree;

    @FXML
    private Label disk1, diskUsage1, disk1Total, disk1Used, disk1Free;
    @FXML
    private Label disk2, diskUsage2, disk2Total, disk2Used, disk2Free;
    @FXML
    private Label disk3, diskUsage3, disk3Total, disk3Used, disk3Free;

    @FXML
    private Label uptime, tasks, temperature;

    private XYChart.Series<String, Number> temperatureData;

    @FXML
    private TextArea metricsTextArea;

    private void displayMetrics() {
        temperatureData = new XYChart.Series<>();
        temperatureData.setName("Core");
        tempChart.getData().clear();
        tempChart.getData().add(temperatureData);

        if (!App.currentPi.isMonitoring())
            App.currentPi.initMonitor();

        Task<Void> checkDetails = new Task<>() {
            @Override
            protected Void call() {
                String currentInfo = App.currentPi.getMetricInfo();
                if (!currentInfo.equals(App.currentPi.metricInfo))
                    App.currentPi.metricInfo = currentInfo;
                return null;
            }
        };
        new Thread(checkDetails).start();

        metricsDetails.toFront();
        metricsDetails.setVisible(true);
        metricsTextArea.setText(App.currentPi.metricInfo);
    }

    /*
        diskMetrics -
            [0] disk1:
                [0] name,
                [1] usage (ratio),
                [2] usage (percentage)
            [1] disk2:
                [0] name,
                [1] usage (ratio),
                [2] usage (percentage)
            [2] disk3:
                [0] name,
                [1] usage (ratio),
                [2] usage (percentage)

        usageMetrics -
            [0] cpu:
                [0] current usage
                [1] average load (1 minute),
                [2] average load (5 minutes),
                [3] average load (15 minutes)
            [1] ram:
                [0] current usage
                [1] total space,
                [2] used space,
                [3] free space
            [2] swap:
                [0] current usage
                [1] total space,
                [2] used space,
                [3] free space
     */
    protected void updateMetrics(String[] timeAndTasks, double temp, String[][] diskMetrics, double[][] usageMetrics) {
        // Update GUI on JavaFX app thread
        if (!App.getSelectedButton().getText().equals("CPU, RAM, and Disk Metrics"))
            return;

        Platform.runLater(() -> {
            temperatureData.getData().add(new XYChart.Data<>(timeAndTasks[0], temp));
            temperature.setText(temp + "°C");
            if (temperatureData.getData().size() == 8)
                temperatureData.getData().remove(0);

            uptime.setText("Uptime: " + timeAndTasks[1]);
            tasks.setText("Tasks: " + timeAndTasks[2]);

            cpuUsagePercent.setText(Math.round(usageMetrics[0][0] * 100) + "%");
            cpuUsageRatio.setProgress(usageMetrics[0][0]);
            cpuAvg1.setText("1 Minute Average: " + String.format("%.1f", usageMetrics[0][1] * 100) + "%");
            cpuAvg5.setText("5 Minute Average: " + String.format("%.1f", usageMetrics[0][2] * 100) + "%");
            cpuAvg15.setText("15 Minute Average: " + String.format("%.1f", usageMetrics[0][3] * 100) + "%");

            ramUsagePercent.setText(Math.round(usageMetrics[1][0] * 100) + "%");
            ramUsageRatio.setProgress(usageMetrics[1][0]);
            ramTotal.setText("Total: " + kilobytesToReadable(usageMetrics[1][1]));
            ramUsed.setText("Used: " + kilobytesToReadable(usageMetrics[1][2]));
            ramFree.setText("Free: " + kilobytesToReadable(usageMetrics[1][3]));

            swapUsagePercent.setText(Math.round(usageMetrics[2][0] * 100) + "%");
            swapUsageRatio.setProgress(usageMetrics[2][0]);
            swapTotal.setText("Total: " + kilobytesToReadable(usageMetrics[2][1]));
            swapUsed.setText("Used: " + kilobytesToReadable(usageMetrics[2][2]));
            swapFree.setText("Free: " + kilobytesToReadable(usageMetrics[2][3]));

            for (int i = 1; i < 4; i++) {
                String[] disk = diskMetrics[i - 1];

                String name = disk[0];
                String total = disk[1];
                String used = disk[2];
                String free = disk[3];
                double percentage = Double.parseDouble(disk[4]);

                switch(i) {
                    case 1 -> {
                        disk1.setText(name);
                        diskUsage1.setText(percentage * 100 + "%");
                        disk1Total.setText("Total: " + total + "B");
                        disk1Used.setText("Used: " + used + "B");
                        disk1Free.setText("Free: " + free + "B");
                        diskIndicator1.setProgress(percentage);
                    }
                    case 2 -> {
                        disk2.setText(name);
                        diskUsage2.setText(percentage * 100 + "%");
                        disk2Total.setText("Total: " + total + "B");
                        disk2Used.setText("Used: " + used + "B");
                        disk2Free.setText("Free: " + free + "B");
                        diskIndicator2.setProgress(percentage);
                    }
                    case 3 -> {
                        disk3.setText(name);
                        diskUsage3.setText(percentage * 100 + "%");
                        disk3Total.setText("Total: " + total + "B");
                        disk3Used.setText("Used: " + used + "B");
                        disk3Free.setText("Free: " + free + "B");
                        diskIndicator3.setProgress(percentage);
                    }
                }
            }
        });
    }

    private String kilobytesToReadable(double kb) {
        if (kb < 0 || kb >= Double.MAX_VALUE)
            return null;

        if (kb > 999999999)
            return String.format("%.1f%cB", kb / 1000000000d, 'T');
        else if (kb > 999999)
            return String.format("%.1f%cB", kb / 1000000d, 'G');
        else if (kb > 999)
            return String.format("%.1f%cB", kb / 1000d, 'M');
        else
            return kb + " KB";
    }
}