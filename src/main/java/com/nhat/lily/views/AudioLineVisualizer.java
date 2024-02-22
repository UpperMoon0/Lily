package com.nhat.lily.views;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.util.Random;

public class AudioLineVisualizer extends Pane {
    public static final int NUM_RECTANGLES = 30;
    private final Rectangle[] rectangles = new Rectangle[NUM_RECTANGLES];
    private final Color startColor = Color.rgb(0, 255, 144);
    private final Color endColor = Color.rgb(119, 255, 234);
    public AudioLineVisualizer() {
        for (int i = 0; i < NUM_RECTANGLES; i++) {
            rectangles[i] = new Rectangle();
            rectangles[i].widthProperty().bind(Bindings.divide(heightProperty(), NUM_RECTANGLES / 2));
            rectangles[i].heightProperty().bind(Bindings.divide(widthProperty(), NUM_RECTANGLES / 2));

            // Calculate the color for each rectangle based on its position
            double ratio = (double) i / (NUM_RECTANGLES - 1);
            Color color = startColor.interpolate(endColor, ratio);

            // Set the fill of each rectangle to the calculated color
            rectangles[i].setFill(color);

            getChildren().add(rectangles[i]);
        }


        widthProperty().addListener((observable, oldValue, newValue) -> updateRectanglePositions());
        heightProperty().addListener((observable, oldValue, newValue) -> updateRectanglePositions());
    }

    public void resetRectangleHeights() {
        for (Rectangle rectangle : rectangles) {
            rectangle.widthProperty().unbind();
            rectangle.heightProperty().unbind();
            rectangle.widthProperty().bind(Bindings.divide(heightProperty(), NUM_RECTANGLES / 2));
            rectangle.heightProperty().bind(Bindings.divide(widthProperty(), NUM_RECTANGLES / 2));
        }
        updateRectanglePositions();
        resetRectangleColors();
    }

    public void updateRectangleHeights(float[] magnitudes) {
        // Generate a random offset for the gradient
        Random random = new Random();
        double offset = random.nextDouble();

        for (int i = 0; i < rectangles.length; i++) {
            rectangles[i].heightProperty().bind(Bindings.divide(widthProperty(), NUM_RECTANGLES / 2));
            switch (i) {
                case 0, NUM_RECTANGLES - 1:
                    magnitudes[i] = magnitudes[i] * 0.25f;
                    break;
                case 1, NUM_RECTANGLES - 2:
                    magnitudes[i] = magnitudes[i] * 0.5f;
                    break;
                default:
                    break;
            }
            double newHeight = calculateHeight(getHeight(), magnitudes[i]);
            rectangles[i].heightProperty().unbind();
            rectangles[i].setHeight(newHeight);

            // Calculate the color for each rectangle based on its position and the random offset
            double ratio = ((double) i / (NUM_RECTANGLES - 1) + offset) % 1;
            Color color = startColor.interpolate(endColor, ratio);

            // Set the fill of each rectangle to the calculated color
            rectangles[i].setFill(color);
        }
        updateRectanglePositions();
    }

    private void resetRectangleColors() {
        for (int i = 0; i < NUM_RECTANGLES; i++) {
            // Calculate the color for each rectangle based on its position
            double ratio = (double) i / (NUM_RECTANGLES - 1);
            Color color = startColor.interpolate(endColor, ratio);

            // Set the fill of each rectangle to the calculated color
            rectangles[i].setFill(color);
        }
    }

    private void updateRectanglePositions() {
        double width = getWidth() * 0.5;
        double gap = width / NUM_RECTANGLES;
        double startX = (getWidth() - width) / 2;
        double imageCenterY = getHeight() / 2;
        for (int i = 0; i < getChildren().size(); i++) {
            Rectangle rectangle = (Rectangle) getChildren().get(i);
            rectangle.setLayoutX(startX + i * gap);
            rectangle.setLayoutY(imageCenterY - rectangle.getHeight() / 2);
        }
    }

    private double calculateHeight(double currentHeight, float magnitude) {
        return magnitude * currentHeight;
    }
}