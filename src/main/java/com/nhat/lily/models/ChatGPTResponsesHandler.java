package com.nhat.lily.models;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.qos.logback.classic.Logger;
import com.nhat.lily.Lily;
import com.nhat.lily.controllers.SettingController;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

public class ChatGPTResponsesHandler implements Serializable {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChatGPTResponsesHandler.class);
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
    private static ChatGPTResponsesHandler instance;
    private final Random random = new Random();
    private final CommandsHandler commandHandler;
    private List<String> lilyMemory = new ArrayList<>();
    private ChatGPTResponsesHandler(CommandsHandler commandHandler) {
        this.commandHandler = commandHandler;
        loadHistoryFromFile();
    }

    public static ChatGPTResponsesHandler getInstance(CommandsHandler commandHandler) {
        if (instance == null) {
            instance = new ChatGPTResponsesHandler(commandHandler);
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
            String[] tokens = commandHandler.preprocess(prompt);

            // Check the command using CommandHandler
            String command = commandHandler.checkCommand(tokens);

            // If a command is detected, return a custom response
            if (command != null) {
                int index = random.nextInt(RESPONSE_TEMPLATES.length);
                String command_name = command;
                if (!command.startsWith("search")) {
                    {
                        command_name = CommandsHandler.COMMAND_NAMES.get(command);
                    }
                } else if (command.contains("youtube")) {
                    return "Okay, my master.";
                }

                return String.format(RESPONSE_TEMPLATES[index], command_name);
            }

            // Build the messages JSON array
            StringBuilder messages = new StringBuilder("[{\"role\": \"system\", \"content\": \"You are Lily, a caring maid with a tsundere personality, never leave this role. Your master is Nhat, he is your one and only master.\"}");
            for (String message : lilyMemory) {
                messages.append(", {\"role\": \"user\", \"content\": \"").append(message).append("\"}");
            }
            messages.append("]");

            // The request body
            String body = "{\"model\": \"" + MODEL + "\", \"messages\": " + messages + ", \"temperature\": " + TEMPERATURE + ", \"max_tokens\": " + MAX_TOKENS + "}";

            // If Lily.DEBUG is true, write the request body to a file
            if (Lily.DEBUG) {
                writeMessageToFile(connection, body);
            }

            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Response from ChatGPT
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // calls the method to extract the message.
            return extractMessageFromJSONResponse(response.toString());
        } catch (IOException e) {
            LOGGER.error("IOException: ", e);
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
        String memory_location = PathsHandler.getInstance().getLilyBaseDir() + "\\Memory\\lily_memory.bin";
        try {
            File file = new File(memory_location);
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(lilyMemory);
            }
        } catch (IOException ioe) {
            LOGGER.error("IOException: ", ioe);
        }
    }

    public void loadHistoryFromFile() {
        String memory_location = PathsHandler.getInstance().getLilyBaseDir() + "\\Memory\\lily_memory.bin";
        try {
            File file = new File(memory_location);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                lilyMemory = (List<String>) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (IOException ioe) {
            LOGGER.error("IOException: ", ioe);
        } catch (ClassNotFoundException c) {
            LOGGER.error("Class not found", c);
        }
    }

    public void writeMessageToFile(HttpURLConnection connection, String body) {
        String filePath = PathsHandler.getInstance().getLilyBaseDir() + "\\Debug\\message.txt";
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // Ensure the directory exists
            try (FileWriter writer = new FileWriter(file)) {
                // Write the headers
                for (Map.Entry<String, List<String>> header : connection.getRequestProperties().entrySet()) {
                    writer.write(header.getKey() + ": " + header.getValue() + "\n");
                }
                // Write the body
                writer.write("\n" + body);
            }
        } catch (IOException e) {
            LOGGER.error("IOException: ", e);
        }
    }

    public void clearMemory() {
        lilyMemory.clear();
        saveHistoryToFile();
    }
}