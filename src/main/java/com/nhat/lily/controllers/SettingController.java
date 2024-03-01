package com.nhat.lily.controllers;

import com.nhat.lily.models.PathsHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SettingController extends BaseController {
    @FXML
    public Label statusLabel;
    @FXML
    private TextField lilyBasePathTextField;
    @FXML
    private TextField downloadsPathTextField;

    @FXML
    public void initialize() {
        lilyBasePathTextField.setText(PathsHandler.getInstance().getLilyBaseDir());
        downloadsPathTextField.setText(PathsHandler.getInstance().getDownloadsDir());
    }
    @FXML
    public void onSaveButtonAction() {
        try {
            Paths.get(PathsHandler.getInstance().getLilyBaseDir());
            Paths.get(PathsHandler.getInstance().getDownloadsDir());
        } catch (InvalidPathException e) {
            statusLabel.setText("Invalid path(s)");
            LOGGER.error("Invalid path(s)", e);
            return;
        }
        Path filePath = Paths.get(PathsHandler.LILY_APP_DATA_PATH, "lily.bin");
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            Files.write(filePath, List.of(PathsHandler.getInstance().getLilyBaseDir(), PathsHandler.getInstance().getDownloadsDir()));
            statusLabel.setText("Settings saved");
        } catch (IOException e) {
            statusLabel.setText("Failed to save settings");
            LOGGER.error("Failed to save settings", e);
        }
    }
}