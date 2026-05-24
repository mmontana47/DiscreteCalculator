package pl.edu.uj.discretecalculator.view;

import javafx.scene.Scene;

import java.util.Objects;

public enum Theme {
    LIGHT("/css/light.css"),
    DARK ("/css/dark.css");

    private final String stylesheet;

    Theme(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    public String stylesheetUrl() {
        return Objects.requireNonNull(
                getClass().getResource(stylesheet),
                "Missing stylesheet: " + stylesheet
        ).toExternalForm();
    }

    public void applyTo(Scene scene) {
        scene.getStylesheets().setAll(stylesheetUrl());
    }
}
