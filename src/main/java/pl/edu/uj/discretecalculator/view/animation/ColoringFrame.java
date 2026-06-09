package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.HashMap;
import java.util.Map;

public class ColoringFrame implements AlgorithmFrame {
    private final String description;

    private final Map<String, String> vertexColors = new HashMap<>();
    private final Map<String, String> edgeColors = new HashMap<>();

    private String activeVertexId = null;
    private String activeEdgeId = null;

    public ColoringFrame(String description) {
        this.description = description;
    }

    public void setActiveVertexId(String activeVertexId) { this.activeVertexId = activeVertexId; }
    public void setActiveEdgeId(String activeEdgeId) { this.activeEdgeId = activeEdgeId; }

    public Map<String, String> getVertexColors() { return vertexColors; }
    public Map<String, String> getEdgeColors() { return edgeColors; }

    @Override
    public String getStepDescription() {
        return description;
    }

    @Override
    public void apply(CanvasManager canvas) {
        for (VertexDrawn vd : canvas.getVertices()) {
            String vId = vd.getVertexId();
            if (vertexColors.containsKey(vId)) {
                vd.setFillColor(vertexColors.get(vId));
            } else {
                vd.setFillColor("#BDC3C7"); // Szary dla nieodwiedzonych
            }
            if (vId.equals(activeVertexId)) vd.select();
        }

        for (EdgeDrawn ed : canvas.getEdges()) {
            String realEdgeId = ed.getEdgeId();

            if (edgeColors.containsKey(realEdgeId)) {
                ed.setStrokeColor(edgeColors.get(realEdgeId));
            } else {
                ed.setStrokeColor("#BDC3C7"); // Szary dla oczekujących
            }


            boolean isActiveEdge = (activeEdgeId != null && activeEdgeId.equals(realEdgeId));
            ed.setActive(isActiveEdge);
        }
    }
}