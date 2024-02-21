package com.nhat.lily.models;

import ch.qos.logback.classic.Logger;
import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.Translation;
import com.azure.core.credential.AzureKeyCredential;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class AzureTranslatorHandler {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(AzureTranslatorHandler.class);
    private static final String API_KEY = System.getenv("AzureTranslatorKey");
    private static final String REGION = System.getenv("AzureRegion");
    private static AzureTranslatorHandler instance = null;
    private TextTranslationClient client;

    private AzureTranslatorHandler() {
        AzureKeyCredential credential = new AzureKeyCredential(API_KEY);

        this.client = new TextTranslationClientBuilder()
                .credential(credential)
                .region(REGION)
                .buildClient();
    }

    public static AzureTranslatorHandler getInstance() {
        if (instance == null) {
            instance = new AzureTranslatorHandler();
        }
        return instance;
    }

    public String translateText(String text, String targetLanguage) {
        StringBuilder translatedText = new StringBuilder();
        try {
            List<String> targetLanguages = Collections.singletonList(targetLanguage);
            List<InputTextItem> inputTextItems = Collections.singletonList(new InputTextItem(text));
            TranslatedTextItem result = client.translate(targetLanguages, inputTextItems).get(0);
            for (Translation translation : result.getTranslations()) {
                translatedText.append(translation.getText());
            }
        }
        catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return translatedText.toString();
    }
}