package com.example.app;

import net.schmizz.sshj.SSHClient;

import java.io.Console;
import java.io.IOException;

public class RaspberryPi {

    private final String model, title, host, username, password;
    private final Console con = System.console();
    private int port;

    public RaspberryPi(String model, String title, String hostname, String username, String password, int port) {
        this.model = model;
        this.title = title;
        this.host = hostname;
        this.username = username;
        this.password = password;
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
        ssh.connect(host);

        try {
            //...
            ssh.authPassword(username, password);
            System.out.println(ssh.isAuthenticated() + ", " + ssh.isAuthenticated());
        } finally {
            ssh.disconnect();
        }
    }

    public String getTitle() { return title; }
}
