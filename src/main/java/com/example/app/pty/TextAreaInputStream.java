package com.example.app.pty;

import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TextAreaInputStream extends InputStream {

    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
    private String bufferedText = "";

    public TextAreaInputStream(TextArea textArea) {
        // Start a background thread to read from the TextArea
        new Thread(() -> {
            while (true) {
                try {
                    String text = textArea.getText();
                    if (!text.equals(bufferedText)) {
                        bufferedText = text;
                        queue.offer(text);
                    }
                    Thread.sleep(100); // Check every 100ms for new input
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    @Override
    public int read() throws IOException {
        try {
            String text = queue.take();
            if (text.isEmpty()) {
                return -1; // End of stream
            }
            return text.charAt(0); // Return the first character
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading", e);
        }
    }
}
