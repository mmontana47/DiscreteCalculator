package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.controller.CanvasManager;

public interface AlgorithmFrame {
    String getStepDescription();
    void apply(CanvasManager canvas);
}