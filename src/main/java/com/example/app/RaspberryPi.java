package com.example.app;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class RaspberryPi {

    private final String model, title, host, username, password;

    protected String metricInfo;

    private SSHClient ssh;

    private Monitor<Void> monitor;

    public RaspberryPi(String model, String title, String hostname, String username, String password) throws IOException {
        this.model = model;
        this.title = title;
        this.host = hostname;
        this.username = username;
        this.password = password;
        connect();
        metricInfo = getMetricInfo();
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
        if (!isConnected())
            return;

        try {
            if (isMonitoring())
                monitor.cancel();
            ssh.disconnect();
            System.out.println(username + "@" + host + " disconnected successfully");
        } catch (IOException e) {
            System.out.println(username + "@" + host + " failed to disconnect");
            throw new RuntimeException(e);
        }
    }

    protected String executeCommand(String command) {
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
            System.out.println("Exception executing command");
            throw new RuntimeException(e);
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (TransportException | ConnectionException e) {
                System.out.println("Failed to close command session");
            }
        }
        return result;
    }

    public String getTitle() { return title; }

    protected String getHost() { return host; }

    public boolean isConnected() { return ssh != null && ssh.isConnected(); }

    public boolean isMonitoring() { return monitor != null && monitor.isRunning(); }

    protected void initMonitor() {
        monitor = new Monitor<>(this);
        new Thread(monitor).start();
    }

    private double[] getVoltageConfig() {
        String cmd = executeCommand("vcgencmd measure_volts core; vcgencmd measure_volts sdram_c");
        if (cmd == null)
            return null;

        Scanner scnr = new Scanner(cmd);
        String coreStr = scnr.nextLine();
        String ramStr = scnr.nextLine();
        scnr.close();

        double coreVolts = Double.parseDouble(coreStr.substring(coreStr.indexOf('=') + 1, coreStr.indexOf('V')));
        double ramVolts = Double.parseDouble(ramStr.substring(ramStr.indexOf('=') + 1, ramStr.indexOf('V')));

        return new double[]{coreVolts, ramVolts};
    }

    public String getMetricInfo() {
        double[] voltConfig = getVoltageConfig();
        if (voltConfig == null)
            return model;

        return model + "\n\n" + executeCommand("vcgencmd version") + "\nCore Voltage Config: " + voltConfig[0]
                + "v\n" + "SDRAM Voltage Config: " + voltConfig[1] + "v";
    }
}
