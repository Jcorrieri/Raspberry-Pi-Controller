package com.example.app;

import javafx.concurrent.Task;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class RaspberryPi {

    private final String model, title, host, username, password;
    private SSHClient ssh;

    private volatile boolean connected;

    private final long initTime;

    private Task<Void> monitor;

    public RaspberryPi(String model, String title, String hostname, String username, String password) throws IOException {
        this.model = model;
        this.title = title;
        this.host = hostname;
        this.username = username;
        this.password = password;
        initTime = System.currentTimeMillis();
        connect();
    }

    private void connect() throws IOException {
        // Connect via ssh...
        ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.setTimeout(5000);
        ssh.connect(host); // See tutorial on loading new hosts
        ssh.authPassword(username, password);
        System.out.println(ssh.isAuthenticated() + ", " + ssh.isAuthenticated());
    }

    protected void disconnect() {
        try {
            if (ssh != null && ssh.isConnected()) {
                monitor.cancel();
                ssh.disconnect();
                System.out.println(username + "@" + host + " disconnected successfully");
            }
        } catch (IOException e) {
            System.out.println(username + "@" + host + " failed to disconnect");
            throw new RuntimeException(e);
        }
    }

    private String executeCommand(String command) {
        Session session = null;
        if (ssh == null || !ssh.isConnected())
            return null;

        String result;
        try {
           session = ssh.startSession();
           Command cmd = session.exec(command);
           result = String.valueOf(IOUtils.readFully(cmd.getInputStream()));
           cmd.join(5, TimeUnit.SECONDS);
        } catch (IOException e) {
            System.out.println(e.getMessage() + "SDSDSD");
            throw new RuntimeException(e);
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (TransportException | ConnectionException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                System.out.println("Failed to close command session");
            }
        }
        return result;
    }

    public String getTitle() { return title; }

    protected String getHost() { return host; }

    public boolean isConnected() {
        connected = ssh.isConnected();
        return connected;
    }

    public boolean isMonitoring() { return monitor != null && monitor.isRunning(); }

    protected void initMonitor() {
        monitor = new Task<>() {
            @Override
            protected Void call() {
                long start = System.currentTimeMillis();

                while (App.currentPi.equals(RaspberryPi.this) && isConnected()) {
                    String time = String.valueOf( (System.currentTimeMillis() - initTime) / 1000 );

                    double temperature = getTemperature();
                    double[] usages = getSystemUsage();
                    String[][] diskMetrics = getDriveInfo();

                    if (temperature == Double.MAX_VALUE || diskMetrics == null || usages == null) {
                        System.out.println("Issue reading one or more metrics");
                        continue;
                    }

                    if (App.currentPi.equals(RaspberryPi.this)) // Paranoia
                        App.getController().updateMetrics(time, temperature, diskMetrics, usages);

                    // Better than Thread.sleep for performance reasons of sorts (for some reason)
                    while (System.currentTimeMillis() - start < 1000)
                        Thread.onSpinWait();
                    start = System.currentTimeMillis();
                }

                return null;
            }
        };
        monitor.setOnCancelled(e -> System.out.println("Monitor cancelled"));
        monitor.setOnSucceeded(e -> System.out.println("Monitor succeeded"));
        monitor.setOnFailed(e -> System.out.println("Monitoring for " + title + " ended (Monitor failed)" + e));

        new Thread(monitor).start();
    }

    private double getTemperature() {
        String temperature = executeCommand("vcgencmd measure_temp | grep  -o  -E '*.[[:digit:]].[[:digit:]]'");
        if (temperature != null)
            return Double.parseDouble(temperature);
        return Double.MAX_VALUE;
    }

    private double[] getVoltage() {
        String coreStr = executeCommand("vcgencmd measure_volts | grep -o -E '[[:digit:]].*[[:digit:]]'");
        String sdramCoreStr = executeCommand("vcgencmd measure_volts sdram_c | grep -o -E '[[:digit:]].*[[:digit:]]'");
        String sdramIOStr = executeCommand("vcgencmd measure_volts sdram_i | grep -o -E '[[:digit:]].*[[:digit:]]'");
        String sdramPhyStr = executeCommand("vcgencmd measure_volts sdram_p | grep -o -E '[[:digit:]].*[[:digit:]]'");

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
        String topCmd = executeCommand(" top -n 1 -b | grep -o -E '%Cpu'.*'id'");
        String freeCmd = executeCommand("free -k");
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
        if (App.currentPi.equals(RaspberryPi.this) && isConnected()) {
            String result = executeCommand("df -h");
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
