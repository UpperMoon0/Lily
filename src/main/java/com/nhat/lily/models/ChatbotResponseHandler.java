package com.nhat.lily.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatbotResponseHandler {
    private static final String URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OpenAIKey");
    private static final String MODEL = "gpt-3.5-turbo";
    private static final double TEMPERATURE = 0.7;
    private static final int MAX_TOKENS = 1000;
    private static ChatbotResponseHandler instance;
    private List<String> messageHistory = new ArrayList<>();
    private ChatbotResponseHandler() {}

    public static ChatbotResponseHandler getInstance() {
        if (instance == null) {
            instance = new ChatbotResponseHandler();
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

            // Add the prompt to the message history
            messageHistory.add(prompt);

            // Build the messages JSON array
            StringBuilder messages = new StringBuilder("[{\"role\": \"system\", \"content\": \"You are Lily, a caring maid with a tsundere personality. Your master, Nhat, is usually referred to by you as 'Master'.\"}");
            for (String message : messageHistory) {
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
}