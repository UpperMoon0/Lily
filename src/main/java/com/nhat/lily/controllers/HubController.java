package com.nhat.lily.controllers;

import com.nhat.lily.models.AzureTTSHandler;
import com.nhat.lily.models.ChatGPTResponsesHandler;
import com.nhat.lily.models.CommandsHandler;
import com.nhat.lily.views.AudioLineVisualizer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class HubController extends BaseController {
    @FXML
    public Circle imageClip;
    @FXML
    public AudioLineVisualizer audioCircleVisualizer;
    @FXML
    public ImageView imageView;
    @FXML
    public VBox vbox;
    @FXML
    public VBox vbox1;
    @FXML
    public VBox vbox2;
    @FXML
    public VBox vbox3;
    @FXML
    private TextArea botResponses;
    @FXML
    private TextField userInput;
    @FXML
    private Button hubButton;
    @FXML
    private Button settingButton;

    public TextArea getBotResponses() {
        return botResponses;
    }

    @FXML
    public void initialize() {
        vbox1.prefHeightProperty().bind(vbox.heightProperty().multiply(0.3));
        vbox2.prefHeightProperty().bind(vbox.heightProperty().multiply(0.55));
        vbox3.prefHeightProperty().bind(vbox.heightProperty().multiply(0.15));
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        Scene scene = stage.getScene();
        bindImageViewSize(scene);
    }

    private void bindImageViewSize(Scene scene) {
        imageView.fitWidthProperty().bind(scene.widthProperty().add(scene.heightProperty()).divide(2).multiply(0.2));
        imageView.fitHeightProperty().bind(scene.widthProperty().add(scene.heightProperty()).divide(2).multiply(0.2));
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

            CommandsHandler commandHandler = CommandsHandler.getInstance(this);
            try {
                String response = ChatGPTResponsesHandler.getInstance(commandHandler).getResponse(input);

                Platform.runLater(() -> {
                    botResponses.appendText("Lily: " + response + "\n");
                    userInput.setDisable(true);
                });

                AzureTTSHandler.getInstance().speak(response, "ja-JP", audioCircleVisualizer, () -> Platform.runLater(() -> userInput.setDisable(false)));

                commandHandler.processCommand(input);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    botResponses.appendText("Error: " + e.getMessage() + "\n");
                    userInput.setDisable(false);
                });
            }
        }).start();

        userInput.clear();
    }
}