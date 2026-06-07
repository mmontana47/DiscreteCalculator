package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.List;
import java.util.Map;

public class TopoFrame implements AlgorithmFrame {
    private final String description;
    private final String phaseName;
    private final String activeVertexId;
    private final String activeEdgeId;
    private final Map<String, Integer> inDegrees;
    private final List<String> sortedNodes;

    public TopoFrame(String description, String phaseName, String activeVertexId, String activeEdgeId,
                     Map<String, Integer> inDegrees, List<String> sortedNodes) {
        this.description = description;
        this.phaseName = phaseName;
        this.activeVertexId = activeVertexId;
        this.activeEdgeId = activeEdgeId;
        this.inDegrees = inDegrees;
        this.sortedNodes = sortedNodes;
    }

    @Override
    public String getStepDescription() {
        return description;
    }

    @Override
    public void apply(CanvasManager canvas) {
        for (VertexDrawn vd : canvas.getVertices()) {
            String vId = vd.getVertexId();

            if (sortedNodes.contains(vId)) {
                int rank = sortedNodes.indexOf(vId) + 1;
                vd.setFillColor("#2ECC71"); // zielony - posortowano
                vd.setBottomLabelText("#" + rank);
            } else {
                vd.setFillColor("#E74C3C"); // czerwony - węzeł czeka

                // zabezpieczenie przed NullPointerException
                if (inDegrees.containsKey(vId)) {
                    if (inDegrees.get(vId) == 0) {
                        vd.setFillColor("#F1C40F"); // żółty - gotowy do pobrania
                    }
                    vd.setBottomLabelText("in-deg: " + inDegrees.get(vId));
                }
            }
        }

        for (EdgeDrawn ed : canvas.getEdges()) {
            String sourceId = ed.getSource().getVertexId();
            if (sortedNodes.contains(sourceId)) {
                ed.setStrokeColor("#ECF0F1"); // szary - wirtualne usunięcie
            } else {
                ed.setStrokeColor("#34495E"); // domyślny stan
            }
        }

        if ("TAKE_NODE".equals(phaseName) && activeVertexId != null) {
            VertexDrawn vd = canvas.getVertexById(activeVertexId);
            if (vd != null) vd.select(); // highlight
        } else if ("UPDATE_DEGREES".equals(phaseName) && activeEdgeId != null) {
            EdgeDrawn ed = canvas.getEdgeById(activeEdgeId);
            if (ed != null) ed.setStrokeColor("#E67E22"); //pomarańcz - usunięcie krawędzi
        }
    }
}