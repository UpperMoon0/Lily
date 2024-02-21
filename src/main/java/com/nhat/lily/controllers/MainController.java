package com.nhat.lily.controllers;

import ch.qos.logback.classic.Logger;
import com.nhat.lily.models.AzureTTSHandler;
import com.nhat.lily.models.ChatGPTResponseHandler;
import com.nhat.lily.models.CommandHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

public class MainController {

    @FXML
    private TextArea botResponses;

    @FXML
    private TextField userInput;
    private  static final Logger logger = (Logger) LoggerFactory.getLogger(MainController.class);
    private CommandHandler commandHandler;
    private Stage stage;
    @FXML
    protected void onUserInputAction(ActionEvent event) {
        String input = userInput.getText();
        String response = "";

        response = ChatGPTResponseHandler.getInstance(stage).getResponse(input);

        botResponses.appendText("User: " + input + "\n");
        botResponses.appendText("Lily: " + response + "\n");

        String finalResponse = response;
        new Thread(() -> {
            AzureTTSHandler.getInstance().speak(finalResponse, "ja-JP");
        }).start();

        commandHandler.processCommand(input);

        userInput.clear();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.commandHandler = CommandHandler.getInstance(stage);
    }
}