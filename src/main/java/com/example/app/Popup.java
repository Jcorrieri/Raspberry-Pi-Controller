package com.example.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Popup {

    public static final int LOGIN = 0, SETTINGS = 1, HELP = 2, ADD_SYS = 3;
    private String resource, title;
    private double width, height;

    public Popup(int type) throws IOException {
        if (init(type) == -1)
            return;

        FXMLLoader fxml = new FXMLLoader(Popup.class.getResource(resource));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setTitle(title);

        Scene scene = new Scene(fxml.load(), width, height);
        stage.setScene(scene);

        double centerXPosition = App.getPrimaryStage().getX() + (App.getPrimaryStage().getWidth() / 2d);
        double centerYPosition = App.getPrimaryStage().getY() + (App.getPrimaryStage().getHeight() / 2d);

        stage.setX(centerXPosition - (width / 2d));
        stage.setY(centerYPosition - (height / 2d));

        stage.show();
    }

    private int init(int type) {
        switch (type) {
            case LOGIN -> {
                resource = "login.fxml";
                title = "Login";
                width = 300;
                height = 335;
            }
            case SETTINGS -> {
                resource = "settings.fxml";
                title = "Settings";
                width = 300;
                height = 335;
            }
            case HELP -> {
                resource = "help.fxml";
                title = "Help";
                width = 335;
                height = 465;
            }
            case ADD_SYS -> {
                resource = "add-system.fxml";
                title = "Add System";
                width = 335;
                height = 465;
            }
            default -> { return -1; }
        }
        return 0;
    }
}
