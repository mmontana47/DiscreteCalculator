package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.List;
import java.util.Map;

public class TarjanFrame implements AlgorithmFrame {
    private final String description;
    private final String phaseName;
    private final String activeVertexId;
    private final String activeEdgeId;
    private final Map<String, Integer> indices;
    private final Map<String, Integer> lowLinks;
    private final List<String> currentStack;
    private final Map<Integer, List<String>> sccMap;

    public TarjanFrame(String description, String phaseName, String activeVertexId, String activeEdgeId,
                       Map<String, Integer> indices, Map<String, Integer> lowLinks,
                       List<String> currentStack, Map<Integer, List<String>> sccMap) {
        this.description = description;
        this.phaseName = phaseName;
        this.activeVertexId = activeVertexId;
        this.activeEdgeId = activeEdgeId;
        this.indices = indices;
        this.lowLinks = lowLinks;
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

            if (!isFinalScc && currentStack.contains(vId)) {
                vd.setFillColor("#3498DB"); // kolor odłożenia na stos
            }
        }

        if (activeVertexId != null && ("VISIT_NODE".equals(phaseName) || "COMPONENT_FOUND".equals(phaseName))) {
            VertexDrawn vd = canvas.getVertexById(activeVertexId);
            if (vd != null) vd.select(); // highlight
        }

        if (activeEdgeId != null && ("CHECK_EDGE".equals(phaseName) || "UPDATE_LOW_LINK".equals(phaseName))) {
            EdgeDrawn ed = canvas.getEdgeById(activeEdgeId);
            if (ed != null) {
                if ("UPDATE_LOW_LINK".equals(phaseName)) {
                    ed.setStrokeColor("#F39C12"); // update low linka - pomarańczowy
                } else {
                    ed.setStrokeColor("#E74C3C"); // zwykłe badanie krawędzi - czerwony
                }
            }
        }
    }
}