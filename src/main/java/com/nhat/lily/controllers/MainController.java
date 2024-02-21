package com.nhat.lily.controllers;

import com.nhat.lily.models.AzureTTSHandler;
import com.nhat.lily.models.ChatbotResponseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class MainController {

    @FXML
    private TextArea botResponses;

    @FXML
    private TextField userInput;

    @FXML
    protected void onUserInputAction(ActionEvent event) {
        String input = userInput.getText();
        String response = ChatbotResponseHandler.getInstance().getResponse(input);
        botResponses.appendText("User: " + input + "\n");
        botResponses.appendText("Lily: " + response + "\n");

        new Thread(() -> {
            AzureTTSHandler.getInstance().speak(response, "ja-JP");
        }).start();

        userInput.clear();
    }
}