package com.nhat.lily.controllers;

import ch.qos.logback.classic.Logger;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

public abstract class BaseController {
    protected static final Logger LOGGER = (Logger) LoggerFactory.getLogger(MainController.class);
    protected Stage stage;
    protected Scene scene;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.scene = stage.getScene();
    }

    public Stage getStage() {
        return stage;
    }
}
