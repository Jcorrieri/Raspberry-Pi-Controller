package com.example.app.pty;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TextAreaInputStream extends InputStream {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public TextAreaInputStream(TextArea textArea) {
        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER
                    && !event.isControlDown() && !event.isAltDown() && !event.isMetaDown() && !event.isShiftDown()) {

                int inputStartPosition =
                        (textArea.getTextFormatter() instanceof CustomTextFormatter f) ? f.getInputStartPos() : 0;

                String line = textArea.getText().substring(inputStartPosition);

                // Add the new line to the queue
                queue.offer(line);
                // Clear the input area or remove processed text
                Platform.runLater(() ->
                        textArea.replaceText(inputStartPosition, textArea.getText().length(), ""));
            }
        });
    }

    @Override
    public int read() throws IOException {
        try {
            String line = queue.take();
            BufferedReader bufferedReader = new BufferedReader(new StringReader(line));
            return bufferedReader.read();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading", e);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            String line = queue.take();
            byte[] lineBytes = line.getBytes();
            int readLength = Math.min(len, lineBytes.length);
            System.arraycopy(lineBytes, 0, b, off, readLength);
            return readLength;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading", e);
        }
    }
}
