package com.example.app;

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

    public RaspberryPi(String model, String title, String hostname, String username, String password, int port) throws IOException {
        this.model = model;
        this.title = title;
        this.host = hostname;
        this.username = username;
        this.password = password;
        this.port = port;
        createConnection();
    }

    private void createConnection() throws IOException {
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
                System.out.println(username + "@" + title + " disconnected successfully");
            }
        } catch (IOException e) {
            System.out.println(username + "@" + title + " failed to disconnect");
            throw new RuntimeException(e);
        }
    }

    protected void executeCommand(String command) {
        Session session = null;
        if (ssh == null || !ssh.isConnected())
            return;

        try {
           session = ssh.startSession();
           Command cmd = session.exec(command);
           System.out.println(IOUtils.readFully(cmd.getInputStream()));
           cmd.join(5, TimeUnit.SECONDS);
           System.out.println("** exit status: " + cmd.getExitStatus());
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
}
