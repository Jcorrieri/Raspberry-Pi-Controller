package com.example.app.Testing;

import com.example.app.App;
import com.example.app.RaspberryPi;
import javafx.scene.control.TextArea;
import net.schmizz.sshj.sftp.StatefulSFTPClient;

import java.io.IOException;

public class TestPi extends RaspberryPi {

    private boolean isConnected, isMonitoring;

    public TestPi(String model, String title, String hostname, String username, String password) throws IOException {
        super(model, title, hostname, username, password);
        isConnected = true;
    }

    @Override
    public void connect() {
        if (App.systems.contains(this))
            App.getController().toggleSystemButtons(this, true);
    }

    @Override
    public void disconnect() {
        if (!isConnected()) {
            isMonitoring = false;
            isConnected = false;
        }
    }

    @Override
    public String executeCommand(String command) {
        return "result of " + command;
    }

    @Override
    public StatefulSFTPClient createStatefulSTFPClient() {
        return null;
    }

    @Override
    public void startPTY(TextArea textArea) throws IOException {
        textArea.appendText("Test text...");
    }

    @Override
    public boolean isConnected() { return isConnected; }

    @Override
    public boolean isMonitoring() { return isMonitoring; }

    public void initMonitor() {
//        monitor = new Monitor<>(this);
//        new Thread(monitor).start();
        isMonitoring = true;
    }

    @Override
    public double[] getVoltageConfig() {
        return new double[]{0.8, 1.1};
    }

    public void restart() {
        if (isMonitoring())
            isMonitoring = false;

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println("test restart failed for some reason");
        }
    }

    public void shutdown() {
        if (isMonitoring())
            isMonitoring = false;
    }
}
