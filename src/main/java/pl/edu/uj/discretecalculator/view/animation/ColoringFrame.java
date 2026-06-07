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

    // wskazniki dla klatki, co robi algorytm
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
                vd.setFillColor("#BDC3C7");
            }
            if (vId.equals(activeVertexId)) vd.select();
        }

        java.util.List<EdgeDrawn> drawnEdges = canvas.getEdges();
        for (int i = 0; i < drawnEdges.size(); i++) {
            EdgeDrawn ed = drawnEdges.get(i);
            String numericId = String.valueOf(i);

            // wlasciwy kolor
            if (edgeColors.containsKey(numericId)) {
                ed.setStrokeColor(edgeColors.get(numericId));
            } else {
                ed.setStrokeColor("#BDC3C7"); //szare - oczekujace
            }

            //TODO: cos mi nie działa to pogrubianie - nie jest konieczne, ale może się tym zajmiemy
            //ed.setActive(numericId.equals(activeEdgeId));
        }
    }

}