package com.example.app;

import com.example.app.Controllers.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;

public class App extends Application {

    private static Stage primaryStage;
    private static AppController appController;

    private static boolean loggedIn = false;

    private static Button selectedButton;

    public static ArrayList<RaspberryPi> systems;
    public static RaspberryPi currentPi;
    public static final int MAX_SYSTEMS = 8;

    public static final int GPIO = 0, FILE_MAN = 1, SSH = 2, SCRIPTS = 3, METRICS = 4;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/app.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        App.primaryStage = stage;
        App.appController = fxmlLoader.getController();
        App.systems = new ArrayList<>();

        String imageUrl = String.valueOf(App.class.getResource("images/program-icon.png") );
        stage.getIcons().add(new Image(imageUrl));

        stage.setTitle("Rpi Project - JFX Demo");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> appController.exitApplication());
        stage.show();
    }

    public static void main(String[] args) { launch(); }

    public static Alert createAlert(String title, Alert.AlertType type) {
        Alert alert = (title == null) ? new Alert(type) : new Alert(type, title);

        double centerXPosition = primaryStage.getX() + (primaryStage.getWidth() / 2d);
        double centerYPosition = primaryStage.getY() + (primaryStage.getHeight() / 2d);
        alert.setX(centerXPosition - 188);
        alert.setY(centerYPosition - 85);

        return alert;
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static AppController getController() { return appController; }

    public static boolean isLoggedIn() { return loggedIn; }

    public static void setLoggedIn() { loggedIn = true; }

    public static void setLoggedOut() { loggedIn = false; }

    public static void selectButton(Button button) {
        if (selectedButton != null)
            selectedButton.getStyleClass().remove("selected-system-button");
        button.getStyleClass().add("selected-system-button");
        selectedButton = button;
    }

    public static Button getSelectedButton() { return selectedButton; }

    public static boolean alreadyExists(String identifier) {
        for (RaspberryPi pi : App.systems)
            if (pi.getTitle().equals(identifier) || pi.getHost().equals(identifier))
                return true;
        return false;
    }
}