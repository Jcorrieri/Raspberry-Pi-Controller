package com.example.app;

import javafx.concurrent.Task;

import java.util.Scanner;

public class Monitor<E> extends Task<E> {

    private final RaspberryPi OWNER;
    private long initTime;

    public Monitor(RaspberryPi pi) {
        OWNER = pi;
        setOnCancelled(e -> System.out.println("Monitor cancelled"));
        setOnSucceeded(e -> System.out.println("Monitor succeeded"));
        setOnFailed(e -> System.out.println("Monitoring for " + OWNER.getTitle() + " ended (Monitor failed)" + e));
    }

    @Override
    protected E call() {
        long cycleStart = System.currentTimeMillis();
        initTime = cycleStart;

        while (App.currentPi.equals(OWNER) && OWNER.isConnected()) {
            double temperature = getTemperature();
            String[][] diskMetrics = getDriveInfo();
            double[][] usages = getSystemUsage();
            String[] uptimeAndTasks = getUptimeAndTasks();

            if (temperature == Double.MAX_VALUE || diskMetrics == null || usages == null || uptimeAndTasks == null) {
                System.out.println("Issue reading one or more metrics");
                continue;
            }

            if (App.currentPi.equals(OWNER)) // Paranoia
                App.getController().updateMetrics(uptimeAndTasks, temperature, diskMetrics, usages);

            // Better than Thread.sleep for performance reasons of sorts (for some reason)
            while (System.currentTimeMillis() - cycleStart < 1000)
                Thread.onSpinWait();
            cycleStart = System.currentTimeMillis();
        }
        return null;
    }

    private double getTemperature() {
        String temperature = OWNER.executeCommand("vcgencmd measure_temp | grep  -o  -E '*.[[:digit:]].[[:digit:]]'");
        if (temperature != null)
            return Double.parseDouble(temperature);
        return Double.MAX_VALUE;
    }

    private String[] getUptimeAndTasks() {
        String programTime = String.valueOf( (System.currentTimeMillis() - initTime) / 1000 );

        String cmd = OWNER.executeCommand("uptime | grep -o -E 'up.*.user'; top -n 1 -b | grep -o -E 'Tasks:'.*");
        if (cmd == null)
            return null;

        Scanner scanner = new Scanner(cmd);
        String uptime = scanner.nextLine();
        String tasks = scanner.nextLine();
        scanner.close();

        uptime = uptime.substring(uptime.indexOf("up") + 2);

        tasks = tasks.replace("Tasks: ", "");
        tasks = tasks.substring(0, tasks.indexOf("total"));

        return new String[]{programTime, uptime, tasks};
    }

    private double[][] getSystemUsage() {
        double[] cpuDataArray, ramDataArray, swapDataArray;

        /*  *  *  *  *  *  *  *
         *  CPU Load Metrics  *
         *  *  *  *  *  *  *  */

        String cmd = OWNER.executeCommand("top -n 1 -b | grep -o -E '%Cpu'.*'id'; " +
                "uptime | grep -o -E 'load average:'.*");
        if (cmd == null)
            return null;

        Scanner scanner = new Scanner(cmd);
        String topCmd = scanner.nextLine();
        String loads = scanner.nextLine();
        scanner.close();

        double cpuLoadPercentage = Double.parseDouble(topCmd.substring(topCmd.indexOf("ni,") + 3, topCmd.indexOf("id")));
        cpuLoadPercentage = (100 - cpuLoadPercentage) / 100;

        loads = loads.substring(loads.indexOf(':') + 1);
        String[] loadStrArray = loads.split(",");

        double oneMin = Double.parseDouble(loadStrArray[0]);
        double fiveMin = Double.parseDouble(loadStrArray[1]);
        double fifteenMin = Double.parseDouble(loadStrArray[2]);

        cpuDataArray = new double[]{cpuLoadPercentage, oneMin, fiveMin, fifteenMin};

        /*  *  *  *  *  *  *  *  *
         *  Memory Load Metrics  *
         *  *  *  *  *  *  *  *  */

        String freeCmd = OWNER.executeCommand("free -k");
        if (freeCmd == null)
            return null;

        Scanner scnr = new Scanner(freeCmd);
        scnr.nextLine();

        double ramPercentage;
        if (scnr.hasNext() && scnr.next().contains("Mem:")) {
            double total = scnr.nextDouble();
            double used = scnr.nextDouble();
            double free = scnr.nextDouble();
            ramPercentage = used / total;

            ramDataArray = new double[]{ramPercentage, total, used, free};
        } else {
            ramDataArray = null;
        }

        scnr.nextLine();

        double swapPercentage;
        if (scnr.hasNext() && scnr.next().contains("Swap:")) {
            double total = scnr.nextDouble();
            double used = scnr.nextDouble();
            double free = scnr.nextDouble();
            swapPercentage = used / total;

            swapDataArray = new double[]{swapPercentage, total, used, free};
        } else {
            swapDataArray = null;
        }

        scnr.close();

        if (ramDataArray == null || swapDataArray == null)
            return null;

        return new double[][]{cpuDataArray, ramDataArray, swapDataArray};
    }

    private String[][] getDriveInfo() {
        if (App.currentPi.equals(OWNER) && OWNER.isConnected()) {
            String result = OWNER.executeCommand("df -h");
            if (result == null)
                return null;

            Scanner scnr = new Scanner(result);
            String[][] diskData = new String[3][5];

            int count = 0;
            while (scnr.hasNextLine()) {
                scnr.nextLine();
                if (!scnr.hasNext())
                    break;

                String[] data = new String[6];
                for (int i = 0; i < 6; i++)
                    data[i] = scnr.next();

                double percentage = (Double.parseDouble(data[4].substring(0, data[4].length() - 1))) / 100;

                if (count == 3) {
                    scnr.close();
                    return diskData;
                } else {
                    diskData[count] = new String[]{data[0], data[1], data[2], data[3], String.valueOf(percentage)};
                    count++;
                }
            }
        }
        return null;
    }
}