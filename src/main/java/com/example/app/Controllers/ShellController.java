package com.example.app.Controllers;

import com.example.app.App;
import com.example.app.pty.CustomTextFormatter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

public class ShellController {

    @FXML
    private TextArea terminalWindow;

    private Task<Void> ptyTask;

    public void startPTY() {
        terminalWindow.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);

        ptyTask = new Task<>() {
            @Override
            protected Void call() {
                App.currentPi.startPTY(terminalWindow);
                return null;
            }
        };

        new Thread(ptyTask).start();
        ptyTask.setOnCancelled(event -> {System.out.println("yurr");});
    }

    private void handleKeyPress(KeyEvent event) {
        int caretPosition = terminalWindow.getCaretPosition();
        int inputStartPosition =
                (terminalWindow.getTextFormatter() instanceof CustomTextFormatter f) ? f.getInputStartPos() : 0;

        switch (event.getCode()) {
            case LEFT, KP_LEFT, DELETE -> {
                if (caretPosition < inputStartPosition || terminalWindow.getSelection().getStart() < inputStartPosition) {
                    event.consume();
                }
            }
            case BACK_SPACE -> {
                if (caretPosition == inputStartPosition && terminalWindow.getSelection().getLength() == 0) {
                    event.consume();
                } else if (caretPosition < inputStartPosition || terminalWindow.getSelection().getStart() < inputStartPosition) {
                    event.consume();
                }
            }
            case HOME -> {
                if (event.isControlDown() || caretPosition <= inputStartPosition) {
                    Platform.runLater(() -> terminalWindow.positionCaret(inputStartPosition));
                }
            }
            default -> {
                if (terminalWindow.getCaretPosition() < inputStartPosition) {
                    Platform.runLater(() -> terminalWindow.positionCaret(inputStartPosition));
                }
            }
        }
    }

    public void closePtyThread() { ptyTask.cancel(); }
}
