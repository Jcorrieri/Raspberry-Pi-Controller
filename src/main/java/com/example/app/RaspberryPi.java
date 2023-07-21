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
import java.util.concurrent.TimeUnit;

public class RaspberryPi {

    private final String model, title, host, username, password;
    private SSHClient ssh;
    private int port;

    private volatile boolean connected;

    private final long initTime;

    private Task<Void> tempMonitor;

    public RaspberryPi(String model, String title, String hostname, String username, String password, int port) throws IOException {
        this.model = model;
        this.title = title;
        this.host = hostname;
        this.username = username;
        this.password = password;
        this.port = port;
        connect();
        initTime = System.currentTimeMillis();
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
                tempMonitor.cancel();
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

        try {
           session = ssh.startSession();
           Command cmd = session.exec(command);
           String result = String.valueOf(IOUtils.readFully(cmd.getInputStream()));
           cmd.join(5, TimeUnit.SECONDS);
           return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (TransportException | ConnectionException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public String getTitle() { return title; }

    protected String getHost() { return host; }

    private long getTimeElapsed() { return System.currentTimeMillis() - initTime; }

    public boolean isConnected() {
        connected = ssh.isConnected();
        return connected;
    }

    public void initTempMonitor() {
        tempMonitor = new Task<>() {
            @Override
            protected Void call() {
                long start = System.currentTimeMillis();

                while (App.currentPi.isConnected()) {
                    String time = String.valueOf(getTimeElapsed() / 1000);

                    String result = executeCommand("vcgencmd measure_temp | grep  -o  -E '*.[[:digit:]].[[:digit:]]'");
                    if (result == null)
                        continue;

                    double temp = Double.parseDouble(result);

                    App.getController().updateTemp(time, temp);

                    // Better than Thread.sleep for performance reasons of sorts (for some reason)
                    while (System.currentTimeMillis() - start < 1000)
                        Thread.onSpinWait();
                    start = System.currentTimeMillis();
                }

                return null;
            }
        };
        tempMonitor.setOnCancelled(e -> System.out.println("tempMonitor cancelled"));
        tempMonitor.setOnSucceeded(e -> System.out.println("tempMonitor succeeded"));
        tempMonitor.setOnFailed(e ->
                System.out.println("Temperature monitoring for " + title + " ended (tempMonitor failed")
        );

        new Thread(tempMonitor).start();
    }
}
