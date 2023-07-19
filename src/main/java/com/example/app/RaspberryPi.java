package com.example.app;

import net.schmizz.sshj.SSHClient;

import java.io.Console;
import java.io.IOException;

public class RaspberryPi {

    private final String model, title, hostname, hostpass;
    private final Console con = System.console();
    private int port;

    public RaspberryPi(String model, String title, String hostname, String hostpass, int port) {
        this.model = model;
        this.title = title;
        this.hostname = hostname;
        this.hostpass = hostpass;
        this.port = port;

        try {
            createConnection();
        } catch (IOException e) {
            return;
        }

    }

    private void createConnection() throws IOException {
        // Connect via ssh...
        final SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.connect(hostname);

        try {
            //...
        } finally {
            ssh.disconnect();
        }
    }

    public String getTitle() { return title; }
}
