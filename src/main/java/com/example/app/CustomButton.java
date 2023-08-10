package com.example.app;

import javafx.scene.control.Button;

public class CustomButton extends Button {

    private final RaspberryPi pi;

    public CustomButton(String name, RaspberryPi pi) {
        super(name);
        this.pi = pi;
        getStylesheets().add(String.valueOf(getClass().getResource("css/app.css")));
        getStyleClass().add("system-dropdown-button");
        setMaxWidth(Double.MAX_VALUE);
    }

    public RaspberryPi getPi() { return pi; }
}
