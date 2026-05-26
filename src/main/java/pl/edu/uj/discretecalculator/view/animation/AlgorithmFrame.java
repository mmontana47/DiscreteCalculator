package pl.edu.uj.discretecalculator.view.animation;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmFrame {
    private final Map<String, String> vertexColors = new HashMap<>();
    private final Map<String, String> edgeColors = new HashMap<>();
    private final Map<String, String> vertexLabels = new HashMap<>();
    private final Map<String, String> edgeWeights = new HashMap<>();
    private final Map<String, Coordinate> vertexPositions = new HashMap<>();

    private final String stepDescription;

    public AlgorithmFrame(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public void setVertexColor(String vertexId, String hexColor) { vertexColors.put(vertexId, hexColor); }
    public void setEdgeColor(String edgeId, String hexColor) { edgeColors.put(edgeId, hexColor); }
    public void setVertexLabel(String vertexId, String label) { vertexLabels.put(vertexId, label); }
    public void setEdgeWeight(String edgeId, String weight) { edgeWeights.put(edgeId, weight); }
    public void setVertexPosition(String vertexId, double x, double y) {
        vertexPositions.put(vertexId, new Coordinate(x, y));
    }

    public Map<String, String> getVertexColors() { return vertexColors; }
    public Map<String, String> getEdgeColors() { return edgeColors; }
    public Map<String, String> getVertexLabels() { return vertexLabels; }
    public Map<String, String> getEdgeWeights() { return edgeWeights; }
    public Map<String, Coordinate> getVertexPositions() { return vertexPositions; }
    public String getStepDescription() { return stepDescription; }

    public record Coordinate(double x, double y) {}
}