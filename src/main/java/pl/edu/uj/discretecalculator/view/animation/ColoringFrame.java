package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.HashMap;
import java.util.Map;

public class ColoringFrame implements AlgorithmFrame {
    private final String description;

    // Przechowujemy HEXy dla wierzchołków (i opcjonalnie krawędzi)
    private final Map<String, String> vertexColors = new HashMap<>();
    private final Map<String, String> edgeColors = new HashMap<>();

    public ColoringFrame(String description) {
        this.description = description;
    }

    public void setVertexColor(String vertexId, String hexColor) {
        vertexColors.put(vertexId, hexColor);
    }

    public void setEdgeColor(String edgeId, String hexColor) {
        edgeColors.put(edgeId, hexColor);
    }

    // Gettery są niezbędne do przenoszenia stanu między klatkami
    public Map<String, String> getVertexColors() {
        return vertexColors;
    }

    public Map<String, String> getEdgeColors() {
        return edgeColors;
    }

    @Override
    public String getStepDescription() {
        return description;
    }

    @Override
    public void apply(CanvasManager canvas) {
        // Aplikowanie kolorów wierzchołków
        for (Map.Entry<String, String> entry : vertexColors.entrySet()) {
            VertexDrawn vd = canvas.getVertexById(entry.getKey());
            if (vd != null) {
                vd.setFillColor(entry.getValue());
            }
        }

        // Aplikowanie kolorów krawędzi (np. do ich wyszarzania)
        for (Map.Entry<String, String> entry : edgeColors.entrySet()) {
            EdgeDrawn ed = canvas.getEdgeById(entry.getKey());
            if (ed != null) {
                ed.setStrokeColor(entry.getValue());
            }
        }
    }
}