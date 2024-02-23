package com.nhat.lily.models;

import ch.qos.logback.classic.Logger;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nhat.lily.controllers.MainController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandHandler {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CommandHandler.class);
    public static final HashMap<String, String> COMMAND_NAMES = new HashMap<>() {{
        put("open browser", "open the browser");
        put("search for", "search for");
        put("search youtube for", "search Youtube for");
        put("clear memory", "clear my memory");
        put("sort downloads", "organize your downloads");
        put("list commands", "list available commands");
    }};
    private static final List<String> STOPWORDS = Arrays.asList("is", "the", "and");
    private static final String[] SUB_DIRS = {"Archives", "Jars", "Audios", "Images", "Videos", "Executables"};
    private static final String[][] EXTENSIONS = {
            {"zip", "rar", "7z"},
            {"jar"},
            {"mp3", "wav", "aac"},
            {"jpg", "jpeg", "png", "gif"},
            {"mp4", "avi", "mov"},
            {"exe", "msi"}
    };
    private static final Set<String> NO_STEMMING = Set.of("youtube", "memory");
    private static final String DOWNLOAD_DIR = "D:\\Downloads";
    private static final String YOUTUBE_DATA_API_KEY = System.getenv("YouTubeDATAAPIKey");
    private static CommandHandler INSTANCE = null;
    private final Stage stage;
    private final PorterStemmer stemmer = new PorterStemmer();
    private final TokenizerModel tokenModel;
    private final MainController mainController;

    private CommandHandler(MainController mainController) {
        TokenizerModel tempTokenModel;
        this.mainController = mainController;
        this.stage = mainController.getStage();
        try {
            tempTokenModel = new TokenizerModel(new FileInputStream("src\\main\\resources\\com\\nhat\\lily\\opennlp\\en-token.bin"));
        } catch (IOException e) {
            LOGGER.error("An error occurred while loading the Tokenizer model: ", e);
            tempTokenModel = null;
        }
        this.tokenModel = tempTokenModel;
    }

    public static CommandHandler getInstance(MainController mainController) {
        if (INSTANCE == null) {
            INSTANCE = new CommandHandler(mainController);
        }
        return INSTANCE;
    }

    public void processCommand(String input) {
        try {
            // Apply preprocessing steps
            String[] tokens = preprocess(input);

            String command = checkCommand(tokens);
            if (command != null) {
                executeCommand(command);
            }

        } catch (Exception e) {
            LOGGER.error("An error occurred while processing the command: ", e);
        }
    }

    public String[] preprocess(String input) {
        // Step 1: Lowercase the input
        input = input.toLowerCase();

        // Step 2: Tokenization
        TokenizerME tokenizer = new TokenizerME(tokenModel);
        String[] tokens = tokenizer.tokenize(input);

        // Step 3: Remove stopwords
        tokens = Arrays.stream(tokens)
                .filter(token -> !STOPWORDS.contains(token))
                .toArray(String[]::new);

        // Step 4: Stemming
        for (int i = 0; i < tokens.length; i++) {
            if (!NO_STEMMING.contains(tokens[i])) {
                tokens[i] = stemmer.stem(tokens[i]);
            }
        }

        // Step 5: Removing Punctuation/Special Characters
        tokens = Arrays.stream(tokens)
                .map(token -> token.replaceAll("[^a-zA-Z0-9]", ""))
                .toArray(String[]::new);

        return tokens;
    }

    public String checkCommand(String[] tokens) {
        List<String> tokenList = Arrays.asList(tokens);
        if ((tokenList.contains("open") || tokenList.contains("start")) && tokenList.contains("browser")) {
            return "open browser";
        } else if (tokenList.contains("search") && tokenList.contains("youtube") && tokenList.contains("for")) {
            int index = tokenList.indexOf("for") + 1;
            if (index < tokenList.size()) {
                String query = String.join(" ", tokenList.subList(index, tokenList.size()));
                return "search youtube for " + query;
            }
        } else if (tokenList.contains("search") && tokenList.contains("for")) {
            int index = tokenList.indexOf("for") + 1;
            if (index < tokenList.size()) {
                String query = String.join(" ", tokenList.subList(index, tokenList.size()));
                return "search for " + query;
            }
        } else if ((tokenList.contains("clear") || tokenList.contains("delete") || tokenList.contains("reset")) && tokenList.contains("your") && tokenList.contains("memory")) {
            return "clear memory";
        } else if ((tokenList.contains("sort") || tokenList.contains("organize")) && tokenList.contains("download")) {
            return "sort downloads";
        } else if ((tokenList.contains("list") || tokenList.contains("show")) && tokenList.contains("command")) {
            return "list commands";
        }
        return null;
    }

    private void executeCommand(String command) {
        switch (command) {
            case "open browser":
                openBrowser();
                break;
            case "clear memory":
                ChatGPTResponseHandler.getInstance(this).clearMemory();
                break;
            case "sort downloads":
                sortDownloads();
                break;
            case "list commands":
                listCommands();
                break;
            default:
                if (command.startsWith("search youtube for")) {
                    String searchQuery = command.substring("search youtube for".length()).trim(); // Trim the search query
                    searchYoutube(searchQuery);
                } else if (command.startsWith("search for ")) {
                    String searchQuery = command.substring("search for ".length()).trim(); // Trim the search query
                    searchWeb(searchQuery);
                }
                break;
        }
    }

    private void openBrowser() {
        try {
            String url = "https://www.google.com";

            openURL(url);
        } catch (Exception e) {
            LOGGER.error("An error occurred while opening the browser: ", e);
        }
    }

    private void openURL(String url) throws IOException, URISyntaxException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));

            stage.setWidth(800);
            stage.setHeight(600);

            stage.centerOnScreen();

            Platform.runLater(() -> stage.setAlwaysOnTop(true));

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> stage.setAlwaysOnTop(false));
            pause.play();
        }
    }

    private void searchWeb(String searchQuery) {
        try {
            String encodedSearchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            String url = "https://www.google.com/search?q=" + encodedSearchQuery;

            openURL(url);
        } catch (Exception e) {
            LOGGER.error("An error occurred while searching the web: ", e);
        }
    }

    private void sortDownloads() {
        try {
            // Create subdirectories if they don't exist
            for (String subDir : SUB_DIRS) {
                Files.createDirectories(Paths.get(DOWNLOAD_DIR, subDir));
            }

            // List all files in the download directory
            try (Stream<Path> paths = Files.list(Paths.get(DOWNLOAD_DIR))) {
                paths.forEach(path -> {
                    String fileName = path.getFileName().toString();
                    String fileExtension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";

                    // Check file extension and move file to corresponding subdirectory
                    for (int i = 0; i < EXTENSIONS.length; i++) {
                        if (Arrays.asList(EXTENSIONS[i]).contains(fileExtension)) {
                            try {
                                Files.move(path, Paths.get(DOWNLOAD_DIR, SUB_DIRS[i], fileName));
                            } catch (IOException e) {
                                LOGGER.error("An error occurred while sorting downloads: ", e);
                            }
                            break;
                        }
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while sorting downloads: ", e);
        }
    }
    private void listCommands() {
        StringBuilder commands = new StringBuilder("Here are some available commands:\n");
        for (String command : COMMAND_NAMES.keySet()) {
            String cmdName = COMMAND_NAMES.get(command);
            String formattedCmd = cmdName.substring(0, 1).toUpperCase() + cmdName.substring(1);
            if (formattedCmd.endsWith("for"))
                formattedCmd += " something";
            commands.append("- ").append(formattedCmd).append("\n");
        }
        mainController.getBotResponses().appendText(commands.toString());
    }
    public void searchYoutube(String query) {
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            GenericUrl url = new GenericUrl("https://www.googleapis.com/youtube/v3/search");
            url.put("key", YOUTUBE_DATA_API_KEY);
            url.put("q", query);
            url.put("type", "video");
            url.put("order", "relevance"); // Change this line
            url.put("maxResults", 1);
            url.put("part", "id");

            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();
            String responseString = response.parseAsString();

            JsonElement jsonElement = JsonParser.parseString(responseString);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String videoId = jsonObject.getAsJsonArray("items").get(0).getAsJsonObject().getAsJsonObject("id").get("videoId").getAsString();

            String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

            openURL(videoUrl);
        } catch (Exception e) {
            LOGGER.error("An error occurred while searching YouTube: ", e);
        }
    }
}