package com.example.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class App extends Application {

    private static Stage primaryStage;
    private static AppController appController;

    private static boolean loggedIn = false;

    protected static ArrayList<RaspberryPi> systems;
    protected static RaspberryPi currentPi;
    public static final int MAX_SYSTEMS = 8;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        App.primaryStage = stage;
        App.appController = fxmlLoader.getController();
        App.systems = new ArrayList<>();

        String imageUrl = String.valueOf( App.class.getResource("images/program-icon.png") );
        stage.getIcons().add(new Image(imageUrl));

        stage.setTitle("Rpi Project - JFX Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(); }

    protected static Stage getPrimaryStage() { return primaryStage; }

    protected static AppController getController() { return appController; }

    public static boolean isLoggedIn() { return loggedIn; }

    protected static void setLoggedIn() { loggedIn = true; }

    protected static void setLoggedOut() { loggedIn = false; }
}