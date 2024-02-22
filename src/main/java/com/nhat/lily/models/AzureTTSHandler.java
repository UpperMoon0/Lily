package com.nhat.lily.models;

import ch.qos.logback.classic.Logger;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.nhat.lily.views.AudioLineVisualizer;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AzureTTSHandler {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AzureTTSHandler.class);
    private static final String SPEECH_SUBSCRIPTION_KEY = System.getenv("AzureTTSKey");
    private static final String SERVICE_REGION = System.getenv("AzureRegion");
    private static AzureTTSHandler instance = null;
    private AzureTTSHandler() {
    }
    public static AzureTTSHandler getInstance() {
        if (instance == null) {
            instance = new AzureTTSHandler();
        }
        return instance;
    }

    public void speak(String text, String language, AudioLineVisualizer visualizer) {
        new Thread(() -> {
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

            SpeechConfig config = SpeechConfig.fromSubscription(SPEECH_SUBSCRIPTION_KEY, SERVICE_REGION);
            config.setSpeechSynthesisLanguage(language);
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(config);

            String ssmlText =
                    "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='" + language + "'>" +
                    "<voice name='" + voiceName + "'>" +
                    "<prosody pitch='+7%' contour='(0%,+10Hz) (50%,+30Hz) (100%,+15Hz)' range='+60%' rate='0.95' volume='loud'>" + translatedText + "</prosody>" +
                    "</voice></speak>";

            Future<SpeechSynthesisResult> result = synthesizer.SpeakSsmlAsync(ssmlText);

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                if (result.isDone()) {
                    visualizer.resetRectangleHeights();
                    synthesizer.close();
                    executor.shutdown();
                } else {
                    Random random = new Random();
                    float[] magnitudes = new float[AudioLineVisualizer.NUM_RECTANGLES];
                    for (int i = 0; i < magnitudes.length; i++) {
                        magnitudes[i] = (random.nextFloat() + .5f) / 2;
                    }
                    visualizer.updateRectangleHeights(magnitudes);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
        }).start();
    }
}