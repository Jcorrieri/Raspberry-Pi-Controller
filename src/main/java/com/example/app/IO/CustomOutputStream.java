package com.example.app.IO;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CustomOutputStream extends OutputStream {

    private final TextArea outputArea;
    private final Charset charset = StandardCharsets.UTF_8;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    public CustomOutputStream(TextArea textArea) {
        outputArea = textArea;
    }

    @Override
    public void write(int b) {
        System.out.println(b);
        if (b == '\n' || b == '\r') {
            flushBuffer();
        } else {
            buffer.write(b);
        }
    }

    @Override
    public void flush() {
        flushBuffer();
    }

    @Override
    public void close() throws IOException {
        flushBuffer();
    }

    private void flushBuffer() {
        if (buffer.size() > 0) {
            Platform.runLater(() -> outputArea.appendText(buffer.toString()));
            buffer.reset();
        }
    }
}
