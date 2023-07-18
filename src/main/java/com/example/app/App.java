package com.example.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;
    private static AppController appController;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        App.primaryStage = stage;
        App.appController = fxmlLoader.getController();

        String imageUrl = String.valueOf( App.class.getResource("images/program-icon.png") );
        stage.getIcons().add(new Image(imageUrl));

        stage.setTitle("Rpi Project - JFX Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(); }

    protected static Stage getPrimaryStage() { return primaryStage; }

    protected static AppController getController() { return appController; }
}