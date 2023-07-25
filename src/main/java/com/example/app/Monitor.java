package com.example.app;

import javafx.concurrent.Task;

import java.util.Scanner;

public class Monitor<E> extends Task<E> {

    private final RaspberryPi OWNER;

    public Monitor(RaspberryPi pi) {
        OWNER = pi;
        setOnCancelled(e -> System.out.println("Monitor cancelled"));
        setOnSucceeded(e -> System.out.println("Monitor succeeded"));
        setOnFailed(e -> System.out.println("Monitoring for " + OWNER.getTitle() + " ended (Monitor failed)" + e));
    }

    @Override
    protected E call() throws Exception {
        long cycleStart = System.currentTimeMillis();
        long initTime = cycleStart;

        while (App.currentPi.equals(OWNER) && OWNER.isConnected()) {
            String time = String.valueOf( (System.currentTimeMillis() - initTime) / 1000 );

            double temperature = getTemperature();
            double[] usages = getSystemUsage();
            String[][] diskMetrics = getDriveInfo();

            if (temperature == Double.MAX_VALUE || diskMetrics == null || usages == null) {
                System.out.println("Issue reading one or more metrics");
                continue;
            }

            if (App.currentPi.equals(OWNER)) // Paranoia
                App.getController().updateMetrics(time, temperature, diskMetrics, usages);

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

    private double[] getVoltage() {
        String coreStr = OWNER.executeCommand("vcgencmd measure_volts | grep -o -E '[[:digit:]].*[[:digit:]]'");
        String sdramCoreStr = OWNER.executeCommand("vcgencmd measure_volts sdram_c | grep -o -E '[[:digit:]].*[[:digit:]]'");
        String sdramIOStr = OWNER.executeCommand("vcgencmd measure_volts sdram_i | grep -o -E '[[:digit:]].*[[:digit:]]'");
        String sdramPhyStr = OWNER.executeCommand("vcgencmd measure_volts sdram_p | grep -o -E '[[:digit:]].*[[:digit:]]'");

        if (coreStr != null && sdramCoreStr != null && sdramIOStr != null && sdramPhyStr != null) {
            double core = Double.parseDouble(coreStr);
            double sdramCore = Double.parseDouble(sdramCoreStr);
            double sdramIO = Double.parseDouble(sdramIOStr);
            double sdramPhy = Double.parseDouble(sdramPhyStr);

            return new double[]{core, sdramCore, sdramIO, sdramPhy};
        }
        return null;
    }

    private double[] getSystemUsage() {
        String topCmd = OWNER.executeCommand(" top -n 1 -b | grep -o -E '%Cpu'.*'id'");
        String freeCmd = OWNER.executeCommand("free -k");
        if (freeCmd == null || topCmd == null)
            return null;

        double cpuLoadPercentage = Double.parseDouble(topCmd.substring(topCmd.indexOf("ni,") + 3, topCmd.indexOf("id")));
        cpuLoadPercentage = (100 - cpuLoadPercentage) / 100;

        Scanner scnr = new Scanner(freeCmd);
        scnr.nextLine();

        double memoryPercentage = Double.MAX_VALUE;
        if (scnr.hasNext() && scnr.next().contains("Mem:")) {
            double total = scnr.nextDouble();
            double used = scnr.nextDouble();
            memoryPercentage = used / total;
        }

        scnr.nextLine();

        double swapPercentage = Double.MAX_VALUE;
        if (scnr.hasNext() && scnr.next().contains("Swap:")) {
            double total = scnr.nextDouble();
            double used = scnr.nextDouble();
            swapPercentage = used / total;
        }

        scnr.close();
        return new double[]{cpuLoadPercentage, memoryPercentage, swapPercentage};
    }

    private String[][] getDriveInfo() {
        if (App.currentPi.equals(OWNER) && OWNER.isConnected()) {
            String result = OWNER.executeCommand("df -h");
            if (result == null)
                return null;

            Scanner scnr = new Scanner(result);
            String[][] diskData = new String[3][3];

            int count = 0;
            while (scnr.hasNextLine()) {
                scnr.nextLine();
                if (!scnr.hasNext())
                    break;

                String[] data = new String[6];
                for (int i = 0; i < 6; i++)
                    data[i] = scnr.next();

                String desc = data[2] + "B/" + data[1] + "B used";
                double percentage = (Double.parseDouble(data[4].substring(0, data[4].length() - 1))) / 100;

                if (count == 3) {
                    scnr.close();
                    return diskData;
                } else {
                    diskData[count] = new String[]{data[0], desc, String.valueOf(percentage)};
                    count++;
                }
            }
        }
        return null;
    }
}