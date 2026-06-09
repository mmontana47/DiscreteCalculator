package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.HashMap;
import java.util.Map;

public class SearchAlgorithmFrame implements AlgorithmFrame {
    private final String description;

    private final Map<String, String> vertexColors = new HashMap<>();
    private final Map<String, String> edgeColors = new HashMap<>();

    // NOWE: Przechowywanie typów krawędzi dla stylizacji (Tree vs Non-Tree)
    private final Map<String, Boolean> isTreeEdge = new HashMap<>();
    private final Map<String, Boolean> isNonTreeEdge = new HashMap<>();

    public SearchAlgorithmFrame(String description) {
        this.description = description;
    }

    public void setVertexColor(String vertexId, String hexColor) {
        vertexColors.put(vertexId, hexColor);
    }

    public void setEdgeColor(String edgeId, String hexColor) {
        edgeColors.put(edgeId, hexColor);
    }

    // NOWE: Metody ustawiające status krawędzi
    public void markAsTreeEdge(String edgeId) {
        isTreeEdge.put(edgeId, true);
    }

    public void markAsNonTreeEdge(String edgeId) {
        isNonTreeEdge.put(edgeId, true);
    }

    public Map<String, String> getVertexColors() { return vertexColors; }
    public Map<String, String> getEdgeColors() { return edgeColors; }

    // NOWE Gettery
    public Map<String, Boolean> getTreeEdges() { return isTreeEdge; }
    public Map<String, Boolean> getNonTreeEdges() { return isNonTreeEdge; }

    @Override
    public String getStepDescription() {
        return description;
    }

    @Override
    public void apply(CanvasManager canvas) {
        for (Map.Entry<String, String> entry : vertexColors.entrySet()) {
            VertexDrawn vd = canvas.getVertexById(entry.getKey());
            if (vd != null) vd.setFillColor(entry.getValue());
        }

        for (Map.Entry<String, String> entry : edgeColors.entrySet()) {
            EdgeDrawn ed = canvas.getEdgeById(entry.getKey());
            if (ed != null) ed.setStrokeColor(entry.getValue());
        }

        for (String edgeKey : isTreeEdge.keySet()) {
            EdgeDrawn ed = canvas.getEdgeById(edgeKey);
            if (ed != null) ed.highlightAsTreeEdge();
        }

        for (String edgeKey : isNonTreeEdge.keySet()) {
            EdgeDrawn ed = canvas.getEdgeById(edgeKey);
            if (ed != null) ed.highlightAsNonTreeEdge();
        }
    }
}