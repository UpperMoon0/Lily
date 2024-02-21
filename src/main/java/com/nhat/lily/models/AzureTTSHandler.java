package com.nhat.lily.models;

import ch.qos.logback.classic.Logger;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class AzureTTSHandler {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(AzureTTSHandler.class);
    private static final String speechSubscriptionKey = System.getenv("AzureTTSKey");
    private static final String serviceRegion = "southeastasia";
    private static AzureTTSHandler instance = null;
    private AzureTTSHandler() {
    }
    public static AzureTTSHandler getInstance() {
        if (instance == null) {
            instance = new AzureTTSHandler();
        }
        return instance;
    }

    public void speak(String text, String language) {
        try {
            String voiceName = "en-US-AriaNeural";

            switch (language) {
                case "ja-JP":
                    voiceName = "ja-JP-MayuNeural";
                    break;
                default:
                    break;
            }

            String translatedText = text;

            if (!language.equals("en-US")) {
                translatedText = AzureTranslatorHandler.getInstance().translateText(text, language);
            }

            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
            config.setSpeechSynthesisLanguage(language);
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(config);

            String ssmlText = "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='" + language + "'>" +
                    "<voice name='" + voiceName + "'>" +
                    "<prosody pitch='+10%'>" + translatedText + "</prosody>" +
                    "</voice></speak>";

            synthesizer.SpeakSsmlAsync(ssmlText).get();

            synthesizer.close();
        } catch (ExecutionException e) {
            logger.error("ExecutionException: ", e);
        } catch (InterruptedException e) {
            logger.error("InterruptedException: ", e);
        }
    }
}