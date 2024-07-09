package com.example.app.pty;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

public class TextAreaOutputStream extends OutputStream {
    private final TextArea textArea;

    public TextAreaOutputStream(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        final char c = (char) b;
        Platform.runLater(() -> {
            textArea.appendText(String.valueOf(c));
            textArea.setText(handleEscapeSequences(textArea.getText()));
            textArea.setTextFormatter(new CustomTextFormatter(textArea.getLength()));
        });
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        String text = new String(b, off, len);
        Platform.runLater(() -> {
            textArea.appendText(handleEscapeSequences(text));
            textArea.setTextFormatter(new CustomTextFormatter(textArea.getLength()));
        });
    }

    private String handleEscapeSequences(String text) {
        // ANSI escape codes start with ESC (0x1B) followed by '[' and ends with 'm', 'K', etc.
        return text.replaceAll("\u001B\\[(0|[\\d;]*)m", "")
                .replaceAll("\u001B\\[\\d*;\\d*m", "")
                .replaceAll("\u001B\\[\\d*;\\d*H", "")
                .replaceAll("\u001B\\[\\d*;\\d*;\\d*H", "")
                .replaceAll("\u001B\\[\\d*;\\d*;\\d*;\\d*H", "")
                .replaceAll("\u001B\\[K", "")
                .replaceAll("\u001B\\[m", "")
                .replaceAll("\u001B\\[\\?[\\d;]*[hl]", "")
                .replaceAll("\u001B\\[[0-9]*;[0-9]*H", "")
                .replaceAll("\u001B\\[[0-9]*J", "");
    }
}
