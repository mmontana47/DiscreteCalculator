package pl.edu.uj.discretecalculator.view.animation;
import java.util.Locale;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;
import java.util.Map;

public class ShortestPathFrame implements AlgorithmFrame {
    private final String description;
    private final String phaseName;
    private final String activeVertexId;
    private final String activeEdgeId;
    private final Map<String, Double> distances;
    private final double maxGraphDistance; // Maksymalny dystans dla skalowania gradientu

    public ShortestPathFrame(String description, String phaseName, String activeVertexId,
                             String activeEdgeId, Map<String, Double> distances, double maxGraphDistance) {
        this.description = description;
        this.phaseName = phaseName;
        this.activeVertexId = activeVertexId;
        this.activeEdgeId = activeEdgeId;
        this.distances = distances;
        this.maxGraphDistance = maxGraphDistance;
    }

    @Override
    public String getStepDescription() {
        return description;
    }

    @Override
    public void apply(CanvasManager canvas) {
        for (Map.Entry<String, Double> entry : distances.entrySet()) {
            VertexDrawn vd = canvas.getVertexById(entry.getKey());
            if (vd != null) {
                if (entry.getValue() == Double.POSITIVE_INFINITY) {
                    vd.setFillColor("#CCCCCC");
                    vd.setBottomLabelText("∞");
                } else {
                    vd.setFillColor(calculateGradientColor(entry.getValue()));
                    vd.setBottomLabelText(String.format(Locale.US, "%.1f", entry.getValue()));
                }
            }
        }

        if ("VISIT_NODE".equals(phaseName) && activeVertexId != null) {
            VertexDrawn vd = canvas.getVertexById(activeVertexId);
            if (vd != null) vd.select();
        } else if ("CHECK_EDGE".equals(phaseName) || "UPDATE_DISTANCE".equals(phaseName) || "NEGATIVE_CYCLE_FOUND".equals(phaseName)) {
            EdgeDrawn ed = canvas.getEdgeById(activeEdgeId);
            if (ed != null) {
                // >>> NOWOŚĆ: Pogrubiamy krawędź, żeby była wyraźnie widoczna w trakcie animacji <<<
                ed.setActive(true);

                if ("UPDATE_DISTANCE".equals(phaseName)) {
                    ed.setStrokeColor("#2ECC71");
                } else if ("NEGATIVE_CYCLE_FOUND".equals(phaseName)) {
                    ed.setStrokeColor("#8B0000");

                    // >>> NOWOŚĆ: Wyświetlamy nieblokujący komunikat o cyklu <<<
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Bellman-Ford Algorithm");
                        alert.setHeaderText("Negative Cycle Detected!");
                        alert.setContentText("The algorithm found a negative-weight cycle. Shortest paths cannot be calculated.");
                        alert.show(); // Ważne: show() a nie showAndWait()!
                    });
                } else {
                    ed.setStrokeColor("#E74C3C");
                }
            }
        }
    }

    //TODO: dać użytkownikowi wybór gradientu
    private String calculateGradientColor(double distance) {
        if (distance == 0.0) return "#2ECC71";

        // Zabezpieczenie przed dzieleniem przez zero
        double safeMax = (maxGraphDistance > 0) ? maxGraphDistance : 1.0;

        // Skalowanie proporcjonalne (ratio zawsze od 0.0 do 1.0)
        double ratio = Math.min(distance / safeMax, 1.0);

        //cos jak rownanie odcinka
        int r = (int) (50 + (180 * ratio));
        int g = (int) (200 - (150 * ratio));
        int b = (int) (100 - (50 * ratio));

        return String.format("#%02X%02X%02X", r, g, b);
    }
}