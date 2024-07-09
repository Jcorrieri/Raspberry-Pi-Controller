package com.example.app.IO;

import java.io.IOException;
import java.io.InputStream;

public class CustomInputStream extends InputStream {
    private String input = "";

    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public int read() throws IOException {
        if (input.length() > 0) {
            char inputChar = input.charAt(0);
            input = input.substring(1);
            return inputChar;
        } else {
            return -1;
        }
    }
}
