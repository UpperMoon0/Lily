package com.nhat.lily.models;

import ch.qos.logback.classic.Logger;
import javafx.animation.PauseTransition;
import javafx.stage.Stage;
import javafx.util.Duration;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandHandler {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(CommandHandler.class);
    private static final String NLP_MODEL_LOCATION = "src\\main\\resources\\com\\nhat\\lily\\opennlp\\";
    private static CommandHandler instance = null;
    public static final HashMap<String, String> COMMAND_NAMES = new HashMap<>() {{
        put("open browser", "open the browser");
        put("search for", "search for");
        put("clear memory", "clear my memory");
    }};
    private String lastCommand = null;
    private final Stage stage;

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
        try (InputStream tokenModelIn = new FileInputStream(NLP_MODEL_LOCATION + "en-token.bin")) {
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            TokenizerME tokenizer = new TokenizerME(tokenModel);
            input = input.toLowerCase();
            String[] tokens = tokenizer.tokenize(input);

            String command = checkCommand(input, tokens);
            if (command != null) {
                executeCommand(command);
                lastCommand = command;
            }

        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }

    public String checkCommand(String input, String[] tokens) {
        input = input.toLowerCase();
        if (isRepeatCommand(input)) {
            if (lastCommand != null) {
                return lastCommand;
            }
        } else if (input.contains("search for ")) {
            int index = input.indexOf("search for ") + "search for ".length();
            String query = input.substring(index);
            return "search for " + query;
        } else if (Arrays.asList(tokens).contains("browser")) {
            return "open browser";
        } else if (Arrays.asList(tokens).contains("clear") && Arrays.asList(tokens).contains("your") && Arrays.asList(tokens).contains("memory")) {
            return "clear memory";
        }
        return null;
    }

    private void executeCommand(String command) {
        switch (command) {
            case "open browser":
                openBrowser("");
                break;
            case "clear memory":
                ChatGPTResponseHandler.getInstance(stage).clearMemory();
                break;
            default:
                if (command.startsWith("search for ")) {
                    String searchQuery = command.substring("search for ".length());
                    openBrowser(searchQuery);
                }
                break;
        }
    }

    private boolean isRepeatCommand(String input) {
        try (InputStream tokenModelIn = new FileInputStream(NLP_MODEL_LOCATION + "en-token.bin")) {
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

    public void openBrowser(String searchQuery) {
        try {
            String encodedSearchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            String url = "https://www.google.com/search?q=" + encodedSearchQuery;

            Document doc = Jsoup.connect(url).get();

            String searchResults = doc.text();

            System.out.println(searchResults);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));

                stage.setWidth(800);
                stage.setHeight(600);

                stage.centerOnScreen();

                stage.setAlwaysOnTop(true);

                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(event -> stage.setAlwaysOnTop(false));
                pause.play();
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }
}