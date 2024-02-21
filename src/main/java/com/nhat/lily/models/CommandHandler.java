package com.nhat.lily.models;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class CommandHandler {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(CommandHandler.class);
    private static final String MODEL_LOCATION = "src/main/resources/com/nhat/lily/opennlp/";
    private static CommandHandler instance = null;
    private String lastCommand = null;
    private Stage stage;

    private CommandHandler(Stage stage) {
        this.stage = stage;
    }

    public static CommandHandler getInstance(Stage stage) {
        if (instance == null) {
            instance = new CommandHandler(stage);
        }
        return instance;
    }

    public void processCommand(String input) {
        try (InputStream tokenModelIn = new FileInputStream(MODEL_LOCATION + "en-token.bin")) {
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            TokenizerME tokenizer = new TokenizerME(tokenModel);
            String[] tokens = tokenizer.tokenize(input);

            if (isRepeatCommand(input)) {
                if (lastCommand != null) {
                    repeatCommand(lastCommand);
                }
            } else if (Arrays.asList(tokens).contains("browser")) {
                openBrowser();
                lastCommand = "browser";
            }

        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }

    private boolean isRepeatCommand(String input) {
        try (InputStream tokenModelIn = new FileInputStream(MODEL_LOCATION + "en-token.bin")) {
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            TokenizerME tokenizer = new TokenizerME(tokenModel);
            String[] tokens = tokenizer.tokenize(input);

            List<String> repeatWords = Arrays.asList("again", "repeat", "another", "one more time");
            for (String token : tokens) {
                if (repeatWords.contains(token)) {
                    return true;
                }
            }
        } catch (IOException e) {
            logger.error("Exception: ", e);
        }
        return false;
    }

    private void repeatCommand(String command) {
        if ("browser".equals(command)) {
            openBrowser();
        }
    }

    private void openBrowser() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("http://www.google.com"));
                Platform.runLater(() -> stage.toFront());
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        }
    }
}