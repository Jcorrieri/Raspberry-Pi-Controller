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
    private int port;

    private volatile boolean connected;

    private final long initTime;

    private Task<Void> monitor;

    public RaspberryPi(String model, String title, String hostname, String username, String password, int port) throws IOException {
        this.model = model;
        this.title = title;
        this.host = hostname;
        this.username = username;
        this.password = password;
        this.port = port;
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
                ssh.disconnect();
                monitor.cancel();
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
                    String time = String.valueOf( (System.currentTimeMillis() - initTime) / 1000);

                    double temperature = getTemperature();
                    String[] diskMetrics = getDiskUsage();

                    if (temperature == Double.MAX_VALUE || diskMetrics == null)
                        continue;

                    if (App.currentPi.equals(RaspberryPi.this)) // Paranoia
                        App.getController().updateMetrics(time, temperature, diskMetrics);

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
        monitor.setOnFailed(e ->
                System.out.println("Monitoring for " + title + " ended (Monitor failed)")
        );

        new Thread(monitor).start();
    }

    private double getTemperature() {
        String temperature = executeCommand("vcgencmd measure_temp | grep  -o  -E '*.[[:digit:]].[[:digit:]]'");
        if (temperature != null)
            return Double.parseDouble(temperature);
        return Double.MAX_VALUE;
    }

    private String[] getDiskUsage() {
        if (App.currentPi.equals(RaspberryPi.this) && isConnected()) {
            String result = executeCommand("df -h");
            if (result == null)
                return null;

            Scanner scnr = new Scanner(result);

            while (scnr.hasNextLine()) {
                scnr.nextLine();
                if (!scnr.hasNext())
                    break;

                String[] data = new String[6];
                for (int i = 0; i < 6; i++)
                    data[i] = scnr.next();

                if (data[0].equals("/dev/root")) {
                    String desc = "Root Drive (SD): " + data[2] + "/" + data[1] + " used";
                    double percentage = ( Double.parseDouble(data[4].substring(0, data[4].length() - 1)) ) / 100;
                    return new String[]{desc, String.valueOf(percentage)};
                }
                System.out.println(Arrays.toString(data));
            }
        }
        return null;
    }
}
