package com.nhat.lily.controllers;

import ch.qos.logback.classic.Logger;
import com.nhat.lily.models.AzureTTSHandler;
import com.nhat.lily.models.ChatGPTResponseHandler;
import com.nhat.lily.models.CommandHandler;
import com.nhat.lily.views.AudioLineVisualizer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

public class MainController {
    @FXML
    public Circle imageClip;
    @FXML
    public AudioLineVisualizer audioCircleVisualizer;
    @FXML
    private VBox vbox;
    @FXML
    public ImageView imageView;
    @FXML
    private TextArea botResponses;
    @FXML
    private TextField userInput;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(MainController.class);
    private Stage stage;
    public void initialize() {
        Scene scene = vbox.getScene();
        if (scene == null) {
            vbox.sceneProperty().addListener((observable, oldScene, newScene) -> bindImageViewSize(newScene));
        } else {
            bindImageViewSize(scene);
        }
    }
    private void bindImageViewSize(Scene scene) {
        imageView.fitWidthProperty().bind(scene.widthProperty().multiply(0.2));
        imageView.fitHeightProperty().bind(scene.widthProperty().multiply(0.2));
        Circle clipCircle = (Circle)imageView.getClip();
        clipCircle.radiusProperty().bind(imageView.fitWidthProperty().divide(2));
        clipCircle.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
        clipCircle.centerYProperty().bind(imageView.fitHeightProperty().divide(2));

        audioCircleVisualizer.prefWidthProperty().bind(imageView.fitWidthProperty());
        audioCircleVisualizer.prefHeightProperty().bind(imageView.fitHeightProperty());
    }
    @FXML
    protected void onUserInputAction(ActionEvent event) {
        String input = userInput.getText();

        new Thread(() -> {
            botResponses.appendText("You: " + input + "\n");

            CommandHandler commandHandler = CommandHandler.getInstance(this);
            String response = ChatGPTResponseHandler.getInstance(commandHandler).getResponse(input);

            // Update UI on JavaFX Application Thread
            Platform.runLater(() -> {
                botResponses.appendText("Lily: " + response + "\n");
            });

            AzureTTSHandler.getInstance().speak(response, "ja-JP", audioCircleVisualizer);
            commandHandler.processCommand(input);
        }).start();

        userInput.clear();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public TextArea getBotResponses() {
        return botResponses;
    }

    public Stage getStage() {
        return stage;
    }
}