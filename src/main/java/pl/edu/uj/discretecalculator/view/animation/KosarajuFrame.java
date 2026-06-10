package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class KosarajuFrame implements AlgorithmFrame {
    private final String description;
    private final String phaseName;
    private final String activeVertexId;
    private final String activeEdgeId;
    private final Set<String> visitedVertices;
    private final List<String> currentStack;
    private final Map<Integer, List<String>> sccMap;

    public KosarajuFrame(String description, String phaseName, String activeVertexId, String activeEdgeId,
                         Set<String> visitedVertices, List<String> currentStack, Map<Integer, List<String>> sccMap) {
        this.description = description;
        this.phaseName = phaseName;
        this.activeVertexId = activeVertexId;
        this.activeEdgeId = activeEdgeId;
        this.visitedVertices = visitedVertices;
        this.currentStack = currentStack;
        this.sccMap = sccMap;
    }

    @Override
    public String getStepDescription() {
        return description;
    }

    @Override
    public void apply(CanvasManager canvas) {

        for (VertexDrawn vd : canvas.getVertices()) {
            String vId = vd.getVertexId();

            boolean isFinalScc = false;

            for (Map.Entry<Integer, List<String>> entry : sccMap.entrySet()) {
                if (entry.getValue().contains(vId)) {
                    // Wykorzystanie dynamicznego generatora z TrackFactory
                    String color = TrackFactory.getColorHexForIndex(entry.getKey() + 1);
                    vd.setFillColor(color);
                    isFinalScc = true;
                    break;
                }
            }

            if (!isFinalScc) {
                if (currentStack.contains(vId)) {
                    vd.setFillColor("#3498DB");
                } else if (visitedVertices.contains(vId)) {
                    vd.setFillColor("#BDC3C7");
                } else {
                    vd.setFillColor("#FFFFFF");
                }
            }
        }

        if (activeVertexId != null &&
                ("DFS1_VISIT".equals(phaseName) || "DFS1_PUSH".equals(phaseName) ||
                        "DFS2_POP".equals(phaseName) || "DFS2_VISIT".equals(phaseName))) {
            VertexDrawn vd = canvas.getVertexById(activeVertexId);
            if (vd != null) vd.select();
        }

        if (activeEdgeId != null && ("DFS1_CHECK_EDGE".equals(phaseName) || "DFS2_CHECK_EDGE".equals(phaseName))) {
            EdgeDrawn ed = canvas.getEdgeById(activeEdgeId);
            if (ed != null) {
                if ("DFS2_CHECK_EDGE".equals(phaseName)) {
                    ed.setStrokeColor("#F39C12");
                } else {
                    ed.setStrokeColor("#E74C3C");
                }
            }
        }
    }
}