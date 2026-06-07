package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;
import java.util.Map;

public class ShortestPathFrame implements AlgorithmFrame {
    private final String description;
    private final String phaseName; // Zmienione na String, by obsługiwać różne algorytmy
    private final String activeVertexId;
    private final String activeEdgeId;
    private final Map<String, Double> distances;

    public ShortestPathFrame(String description, String phaseName, String activeVertexId, String activeEdgeId, Map<String, Double> distances) {
        this.description = description;
        this.phaseName = phaseName;
        this.activeVertexId = activeVertexId;
        this.activeEdgeId = activeEdgeId;
        this.distances = distances;
    }

    @Override
    public String getStepDescription() {
        return description;
    }

    @Override
    public void apply(CanvasManager canvas) {
        // 1. Nakładamy odległości i gradient na wszystkie wierzchołki
        for (Map.Entry<String, Double> entry : distances.entrySet()) {
            VertexDrawn vd = canvas.getVertexById(entry.getKey());
            if (vd != null) {
                if (entry.getValue() == Double.POSITIVE_INFINITY) {
                    vd.setFillColor("#CCCCCC");
                    vd.setBottomLabelText("∞");
                } else {
                    vd.setFillColor(calculateGradientColor(entry.getValue()));
                    vd.setBottomLabelText(String.format("%.1f", entry.getValue()));
                }
            }
        }

        // 2. Wizualizujemy akcje w zależności od fazy algorytmu
        if ("VISIT_NODE".equals(phaseName) && activeVertexId != null) {
            VertexDrawn vd = canvas.getVertexById(activeVertexId);
            if (vd != null) {
                vd.select(); // Podświetlamy badany wierzchołek
            }
        } else if ("CHECK_EDGE".equals(phaseName) || "UPDATE_DISTANCE".equals(phaseName) || "NEGATIVE_CYCLE_FOUND".equals(phaseName)) {
            EdgeDrawn ed = canvas.getEdgeById(activeEdgeId);
            if (ed != null) {
                if ("UPDATE_DISTANCE".equals(phaseName)) {
                    ed.setStrokeColor("#2ECC71"); // Sukces (Zielony)
                } else if ("NEGATIVE_CYCLE_FOUND".equals(phaseName)) {
                    ed.setStrokeColor("#8B0000"); // Krytyczny Błąd (Ciemnoczerwony)
                } else {
                    ed.setStrokeColor("#E74C3C"); // Sprawdzanie (Czerwony)
                }
            }
        }
    }

    private String calculateGradientColor(double distance) {
        if (distance == 0.0) return "#2ECC71"; // Start

        double maxExpectedDistance = 40.0;
        double ratio = Math.min(distance / maxExpectedDistance, 1.0);

        int r = (int) (50 + (180 * ratio));
        int g = (int) (200 - (150 * ratio));
        int b = (int) (100 - (50 * ratio));

        return String.format("#%02X%02X%02X", r, g, b);
    }
}