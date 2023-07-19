package com.example.app;

public class RaspberryPi {

    private final String model, title, hostname, hostpass;
    private int port;
    private boolean connected;

    public RaspberryPi(String model, String title, String hostname, String hostpass, int port) {
        this.model = model;
        this.title = title;
        this.hostname = hostname;
        this.hostpass = hostpass;
        this.port = port;
        this.connected = createConnection();
    }

    private boolean createConnection() {
        // Connect via ssh...
        return false;
    }

    public String getTitle() { return title; }
}
