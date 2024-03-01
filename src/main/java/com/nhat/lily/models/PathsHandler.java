package com.nhat.lily.models;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PathsHandler {
    public static final String LILY_APP_DATA_PATH = System.getenv("APPDATA") + "\\NsTut\\Lily";
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(PathsHandler.class);
    private static final String DEFAULT_LILY_BASE_PATH = LILY_APP_DATA_PATH + "\\Base";
    private static String lilyBaseDir;
    private static String downloadsDir;
    private static PathsHandler instance;

    public String getLilyBaseDir() {
        return lilyBaseDir;
    }

    public String getDownloadsDir() {
        return downloadsDir;
    }

    private PathsHandler() {
        Path filePath = Paths.get(LILY_APP_DATA_PATH, "lily.bin");
        try {
            List<String> lines = Files.readAllLines(filePath);
            lilyBaseDir = lines.get(0);
            downloadsDir = lines.get(1);
        } catch (IOException e) {
            setDefaultValues();
            LOGGER.error("Failed to read settings", e);
        }
    }

    public static PathsHandler getInstance() {
        if (instance == null) {
            instance = new PathsHandler();
        }
        return instance;
    }

    private void setDefaultValues() {
        lilyBaseDir = DEFAULT_LILY_BASE_PATH;
        downloadsDir = System.getProperty("user.home") + "\\Downloads";
    }
}