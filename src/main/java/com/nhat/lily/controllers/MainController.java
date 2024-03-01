package com.nhat.lily.controllers;

import com.nhat.lily.Lily;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController extends BaseController {
    @FXML
    public BorderPane borderPane;
    @FXML
    private Button hubButton;
    @FXML
    private Button settingButton;
    @FXML
    private String currentView;
    @FXML
    public void initialize() {
    }

    @FXML
    protected void onHubButtonAction(ActionEvent event) {
        if (!currentView.equals("hub")) {
            loadView("hub");
        }
    }

    @FXML
    protected void onSettingButtonAction(ActionEvent event) {
        if (!currentView.equals("setting")) {
            loadView("setting");
        }
    }

    public void loadView(String viewName) {
        String selectedStyle = "-fx-background-color: #505050; -fx-text-fill: white; -fx-background-radius: 0;";
        switch (viewName) {
            case "hub":
                try {
                    FXMLLoader loader = new FXMLLoader(Lily.class.getResource("views/hub-view.fxml"));
                    Pane hubView = loader.load();
                    HubController hubController = loader.getController();
                    hubController.setStage(stage);
                    borderPane.setCenter(hubView);
                    currentView = "hub";
                    resetButtonStyles();
                    hubButton.setStyle(selectedStyle);
                } catch (IOException e) {
                    LOGGER.error("Failed to load hub view", e);
                }
                break;
            case "setting":
                try {
                    FXMLLoader loader = new FXMLLoader(Lily.class.getResource("views/setting-view.fxml"));
                    Pane settingView = loader.load();
                    SettingController settingController = loader.getController();
                    settingController.setStage(stage);
                    borderPane.setCenter(settingView);
                    currentView = "setting";
                    resetButtonStyles();
                    settingButton.setStyle(selectedStyle);
                } catch (IOException e) {
                    LOGGER.error("Failed to load setting view", e);
                }
                break;
        }
    }

    private void resetButtonStyles() {
        String style = "-fx-background-color: #282828; -fx-text-fill: white; -fx-background-radius: 0;";
        hubButton.setStyle(style);
        settingButton.setStyle(style);
    }
}