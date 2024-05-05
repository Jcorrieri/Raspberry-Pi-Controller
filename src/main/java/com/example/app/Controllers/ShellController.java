package com.example.app.Controllers;

import com.example.app.App;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ShellController {

    @FXML
    private TextArea terminalWindow;

    public void testPTY() {
        Task<Void> ptyTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                App.currentPi.startPTY(terminalWindow);
                return null;
            }
        };

        new Thread(ptyTask).start();
    }


}
