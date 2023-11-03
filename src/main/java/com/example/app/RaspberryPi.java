package com.example.app;

import javafx.scene.control.TitledPane;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.StatefulSFTPClient;
import net.schmizz.sshj.transport.TransportException;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class RaspberryPi {

    private final String model;
    private String title, host, username, password;

    private TitledPane titledPane;

    private SSHClient ssh;

    private StatefulSFTPClient statefulSFTPClient;

    public String config;

    private Monitor<Void> monitor;

    public RaspberryPi(String model, String title, String hostname, String username, String password) throws IOException {
        this.model = model;
        this.title = title;
        this.host = hostname;
        this.username = username;
        this.password = password;
        connect();
        config = execConfigCmd();
    }

    public void connect() throws IOException {
        // Connect via ssh...
        ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.setTimeout(5000);
        ssh.connect(host); // See tutorial on loading new hosts
        ssh.authPassword(username, password);

        if (App.systems.contains(this))
            App.getController().toggleSystemButtons(this, true);

        System.out.println(ssh.isAuthenticated() + ", " + ssh.isAuthenticated());
    }

    public void disconnect() {
        if (!isConnected())
            return;

        try {
            if (isMonitoring())
                monitor.cancel();
            statefulSFTPClient.close();
            ssh.disconnect();
            System.out.println(username + "@" + host + " disconnected successfully");
        } catch (IOException e) {
            System.out.println(username + "@" + host + " failed to disconnect");
            throw new RuntimeException(e);
        }
    }

    public String executeCommand(String command) {
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

    public StatefulSFTPClient createStatefulSTFPClient() {
        try {
            statefulSFTPClient = (StatefulSFTPClient) ssh.newStatefulSFTPClient();
            return statefulSFTPClient;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TitledPane getTitledPane() { return titledPane; }

    public void setTitledPane(TitledPane pane) { titledPane = pane; }

    public String getTitle() { return title; }

    public void setTitle(String newTitle) {
        title = newTitle;
        titledPane.setText(newTitle);
        titledPane.setId(newTitle);
        App.getController().setSystemName(newTitle + " - " + "Settings");
    }

    public String getHost() { return host; }

    public void setHost(String newHost) { host = newHost; }

    public String getUser() { return username; }

    public void setUser(String newUser) { username = newUser; }

    public String getPass() { return password; }

    public void setPass(String newPass) {password = newPass; }

    public boolean isConnected() { return ssh != null && ssh.isConnected(); }

    public boolean isMonitoring() { return monitor != null && monitor.isRunning(); }

    public void initMonitor() {
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

    public String execConfigCmd() {
        double[] voltConfig = getVoltageConfig();
        if (voltConfig == null)
            return model;

        return model + "\n\n" + executeCommand("vcgencmd version") + "\nCore Voltage Config: " + voltConfig[0]
                + "v\n" + "SDRAM Voltage Config: " + voltConfig[1] + "v";
    }

    public void restart() {
        if (isMonitoring())
            monitor.cancel();
        Session session = null;
        if (ssh == null || !ssh.isConnected())
            return;

        try {
            session = ssh.startSession();
            session.exec("sudo shutdown -r now");
        } catch (IOException e) {
            System.out.println("Exception shutting down");
            throw new RuntimeException(e);
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (TransportException | ConnectionException e) {
                System.out.println("Failed to close command session");
            }
        }
    }

    public void shutdown() {
        if (isMonitoring())
            monitor.cancel();
        Session session = null;
        if (ssh == null || !ssh.isConnected())
            return;

        try {
            session = ssh.startSession();
            session.exec("sudo shutdown -h now");
        } catch (IOException e) {
            System.out.println("Exception shutting down");
            throw new RuntimeException(e);
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (TransportException | ConnectionException e) {
                System.out.println("Failed to close command session");
            }
        }
    }
}
