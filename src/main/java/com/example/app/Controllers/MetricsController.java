package com.example.app.Controllers;

import com.example.app.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class MetricsController {

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

    protected void initMetrics() {
        temperatureData = new XYChart.Series<>();
        temperatureData.setName("Core");
        tempChart.getData().clear();
        tempChart.getData().add(temperatureData);

        if (!App.currentPi.isMonitoring())
            App.currentPi.initMonitor();
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
