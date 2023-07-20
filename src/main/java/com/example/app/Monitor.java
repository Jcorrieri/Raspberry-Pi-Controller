package com.example.app;

public class Monitor {

    private final RaspberryPi pi;

    public Monitor(RaspberryPi raspberryPi) { pi = raspberryPi; }

    public void getTemp() { pi.executeCommand("vcgencmd measure_temp"); }
}
