package com.example.app.pty;

import javafx.scene.control.TextFormatter;

public class CustomTextFormatter extends TextFormatter<String> {
    private final int inputStartPos;

    public CustomTextFormatter(int inputStartPosition) {
        super(change -> {
            int changeStart = change.getRangeStart();

            if (changeStart < inputStartPosition) {
                if (change.getText().isEmpty() ) {
                    return change;
                }
                return null;
            }

            return change;
        });

        inputStartPos = inputStartPosition;
    }

    public int getInputStartPos() { return inputStartPos; }
}
