package com.nhat.lily.models;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.qos.logback.classic.Logger;
import com.nhat.lily.controllers.MainController;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

public class ChatGPTResponseHandler implements Serializable {
    public static final String MEMORY_LOCATION = "D:\\Dev\\LilyBase\\memory\\lily_memory.bin";
    private static final Logger logger = (Logger) LoggerFactory.getLogger(ChatGPTResponseHandler.class);
    private static final String URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OpenAIKey");
    private static final String MODEL = "gpt-3.5-turbo";
    private static final double TEMPERATURE = 0.7;
    private static final int MAX_TOKENS = 1000;
    private static final String[] RESPONSE_TEMPLATES = {
            "Hmph! I guess I can do %s for you...",
            "It's not like I want to, but will %s.",
            "Fine, I'll do %s. But don't get the wrong idea!",
            "I'll do %s, but don't expect me to like it!",
            "I'll do %s, but only because you asked nicely.",
            "I'll do %s, but only because I want to!",
            "I'll do %s, but only because I want to help you!"
    };
    private static ChatGPTResponseHandler instance;
    private final Random random = new Random();
    private final CommandHandler commandHandler;
    private List<String> lilyMemory = new ArrayList<>();
    private ChatGPTResponseHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        loadHistoryFromFile();
    }

    public static ChatGPTResponseHandler getInstance(CommandHandler commandHandler) {
        if (instance == null) {
            instance = new ChatGPTResponseHandler(commandHandler);
        }
        return instance;
    }

    public String getResponse(String prompt) {
        try {
            URL obj = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");

            // Add the prompt to the message history and save it to a file
            lilyMemory.add(prompt);
            saveHistoryToFile();

            // Tokenize the prompt
            String[] tokens = prompt.toLowerCase().split(" ");

            // Check the command using CommandHandler
            String command = commandHandler.checkCommand(tokens);

            // If a command is detected, return a custom response
            if (command != null) {
                int index = random.nextInt(RESPONSE_TEMPLATES.length);
                String command_name = command;
                if (!command.startsWith("search for")) {
                    command_name = CommandHandler.COMMAND_NAMES.get(command);
                }

                return String.format(RESPONSE_TEMPLATES[index], command_name);
            }

            // Build the messages JSON array
            StringBuilder messages = new StringBuilder("[{\"role\": \"system\", \"content\": \"You are Lily, a caring maid with a tsundere personality. Your master, Nhat, is usually referred to by you as your master.\"}");
            for (String message : lilyMemory) {
                messages.append(", {\"role\": \"user\", \"content\": \"").append(message).append("\"}");
            }
            messages.append("]");

            // The request body
            String body = "{\"model\": \"" + MODEL + "\", \"messages\": " + messages + ", \"temperature\": " + TEMPERATURE + ", \"max_tokens\": " + MAX_TOKENS + "}";
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Response from ChatGPT
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // calls the method to extract the message.
            return extractMessageFromJSONResponse(response.toString());
        } catch (IOException e) {
            logger.error("IOException: ", e);
            return "Oops! There is something wrong, please wait and try again!";
        }
    }

    public String extractMessageFromJSONResponse(String response) {
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        JSONObject lastChoice = choices.getJSONObject(choices.length() - 1);
        JSONObject message = lastChoice.getJSONObject("message");
        return message.getString("content");
    }

    public void saveHistoryToFile() {
        try {
            FileOutputStream fos = new FileOutputStream(MEMORY_LOCATION);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(lilyMemory);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            logger.error("IOException: ", ioe);
        }
    }

    public void loadHistoryFromFile() {
        try {
            File file = new File(MEMORY_LOCATION);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                lilyMemory = (List<String>) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (IOException ioe) {
            logger.error("IOException: ", ioe);
        } catch (ClassNotFoundException c) {
            logger.error("Class not found", c);
        }
    }

    public void clearMemory() {
        lilyMemory.clear();
        saveHistoryToFile();
    }
}