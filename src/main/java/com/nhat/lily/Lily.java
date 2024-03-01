package com.nhat.lily;

import com.nhat.lily.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Lily extends Application {
    public static final boolean DEBUG = true;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Lily.class.getResource("views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Lily");
        stage.setScene(scene);

        MainController mainController = fxmlLoader.getController();
        mainController.setStage(stage);
        mainController.loadView("hub");

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}