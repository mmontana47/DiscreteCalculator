package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.algorithm.BFSResult;
import pl.edu.uj.discretecalculator.algorithm.DFSResult;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.algorithm.KosarajuAlgorithmResult;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.*;

public class TrackFactory {


    public static String getColorHexForIndex(int colorIndex) {
        if (colorIndex <= 0) return "#7F8C8D";

        double goldenRatioConjugate = 0.618033988749895;
        double hue = (0.9 + (colorIndex * goldenRatioConjugate)) % 1.0;

        double saturation = 0.65 + ((colorIndex % 3) * 0.10);
        double brightness = 0.85 + ((colorIndex % 2) * 0.10);

        javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.hsb(hue * 360, saturation, brightness);

        return String.format("#%02X%02X%02X",
                (int) (fxColor.getRed() * 255),
                (int) (fxColor.getGreen() * 255),
                (int) (fxColor.getBlue() * 255));
    }

    public static AlgorithmTrack buildBfsTrack(BFSResult<String> result, Graph<String> graph) {
        return buildSearchTrackBase(result.getVisitOrder(), result.getParentMap(), result.getNonTreeEdges(), result.getComponentMap(), "BFS", graph);
    }

    public static AlgorithmTrack buildDfsTrack(DFSResult<String> result, Graph<String> graph) {
        return buildSearchTrackBase(result.getVisitOrder(), result.getParentMap(), result.getNonTreeEdges(), result.getComponentMap(), "DFS", graph);
    }

    private static AlgorithmTrack buildSearchTrackBase(List<Vertex<String>> visitOrder,
                                                       Map<Vertex<String>, Vertex<String>> parentMap,
                                                       Set<Edge<String>> cycles,
                                                       Map<Vertex<String>, Integer> componentMap, // <--- DODANO
                                                       String algName,
                                                       Graph<String> graph) {

        AlgorithmTrack track = new AlgorithmTrack();
        SearchAlgorithmFrame initialFrame = new SearchAlgorithmFrame(algName + " started");
        track.addFrame(initialFrame);

        Map<String, String> cumulativeVertexColors = new HashMap<>();
        Map<String, String> cumulativeEdgeColors = new HashMap<>();
        Map<String, Boolean> cumulativeTreeEdges = new HashMap<>();
        Map<String, Boolean> cumulativeNonTreeEdges = new HashMap<>();


        String treeEdgeColor = "#333333";

        for (Vertex<String> currentNode : visitOrder) {
            String nodeId = String.valueOf(currentNode.getId());
            SearchAlgorithmFrame frame = new SearchAlgorithmFrame("Visiting vertex " + nodeId);

            int componentIndex = componentMap.get(currentNode);

            int colorOffset = algName.equals("BFS") ? 0 : 5;
            String visitColor = getColorHexForIndex(componentIndex + colorOffset);
            cumulativeVertexColors.put(nodeId, visitColor);

            Vertex<String> parentNode = parentMap.get(currentNode);
            if (parentNode != null) {
                for (Edge<String> edge : graph.getEdges()) {
                    if ((edge.getSource().getId() == currentNode.getId() && edge.getTarget().getId() == parentNode.getId()) ||
                            (edge.getSource().getId() == parentNode.getId() && edge.getTarget().getId() == currentNode.getId())) {

                        String edgeId = String.valueOf(edge.getId());

                        cumulativeEdgeColors.put(edgeId, treeEdgeColor);
                        cumulativeTreeEdges.put(edgeId, true);
                        break;
                    }
                }
            }

            frame.getVertexColors().putAll(cumulativeVertexColors);
            frame.getEdgeColors().putAll(cumulativeEdgeColors);
            frame.getTreeEdges().putAll(cumulativeTreeEdges);
            frame.getNonTreeEdges().putAll(cumulativeNonTreeEdges);

            track.addFrame(frame);
        }

        if (!cycles.isEmpty()) {
            SearchAlgorithmFrame finalFrame = new SearchAlgorithmFrame("Marking back/cross edges");
            finalFrame.getVertexColors().putAll(cumulativeVertexColors);
            finalFrame.getEdgeColors().putAll(cumulativeEdgeColors);

            for (Edge<String> badEdge : cycles) {
                String edgeId = String.valueOf(badEdge.getId());
                cumulativeNonTreeEdges.put(edgeId, true);
            }

            finalFrame.getTreeEdges().putAll(cumulativeTreeEdges);
            finalFrame.getNonTreeEdges().putAll(cumulativeNonTreeEdges);

            track.addFrame(finalFrame);
        }

        SearchAlgorithmFrame summaryFrame = new SearchAlgorithmFrame(algName + " finished. Highlighting search tree.");
        summaryFrame.getVertexColors().putAll(cumulativeVertexColors);
        summaryFrame.getEdgeColors().putAll(cumulativeEdgeColors);

        for (String treeEdgeId : cumulativeTreeEdges.keySet()) {
            summaryFrame.markAsTreeEdge(treeEdgeId);
        }

        for (Edge<String> e : graph.getEdges()) {
            String edgeId = String.valueOf(e.getId());
            if (!cumulativeTreeEdges.containsKey(edgeId)) {
                summaryFrame.markAsNonTreeEdge(edgeId);
            }
        }

        track.addFrame(summaryFrame);

        return track;
    }

    public static AlgorithmTrack buildDijkstraTrack(pl.edu.uj.discretecalculator.algorithm.DijkstraAlgorithmResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();

        double maxDist = 0.0;
        for (Double d : result.getFinalDistances().values()) {
            if (d != Double.POSITIVE_INFINITY && d > maxDist) {
                maxDist = d;
            }
        }

        for (var step : result.getHistory()) {
            String desc = "";
            String vId = step.activeNode != null ? step.activeNode.getValue() : null;
            String eId = step.activeEdge != null ? String.valueOf(step.activeEdge.getId()) : null;

            Map<String, Double> guiDistances = new HashMap<>();
            for (var entry : step.distancesSnapshot.entrySet()) {
                guiDistances.put(entry.getKey().getValue(), entry.getValue());
            }

            switch (step.phase) {
                case VISIT_NODE -> desc = "Processing node [" + vId + "] — closest in queue.";
                case CHECK_EDGE -> desc = "Checking edge toward node [" + vId + "].";
                case UPDATE_DISTANCE -> desc = "Distance to node [" + vId + "] relaxed.";
            }

            track.addFrame(new ShortestPathFrame(desc, step.phase.name(), vId, eId, guiDistances, maxDist));
        }
        return track;
    }

    public static AlgorithmTrack buildBellmanFordTrack(pl.edu.uj.discretecalculator.algorithm.BellmanFordResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();

        double maxDist = 0.0;
        for (Double d : result.getFinalDistances().values()) {
            if (d != Double.POSITIVE_INFINITY && d > maxDist) {
                maxDist = d;
            }
        }

        var history = result.getHistory();

        for (int i = 0; i < history.size(); i++) {
            var step = history.get(i);

            if (step.phase == pl.edu.uj.discretecalculator.algorithm.BellmanFordResult.Phase.CHECK_EDGE) {
                boolean willUpdate = false;
                if (i + 1 < history.size() && history.get(i + 1).phase == pl.edu.uj.discretecalculator.algorithm.BellmanFordResult.Phase.UPDATE_DISTANCE) {
                    willUpdate = true;
                }

                if (!willUpdate) {
                    continue;
                }
            }

            String desc = "";
            String vId = step.activeNode != null ? step.activeNode.getValue() : null;
            String eId = step.activeEdge != null ? String.valueOf(step.activeEdge.getId()) : null;

            Map<String, Double> guiDistances = new HashMap<>();
            for (var entry : step.distancesSnapshot.entrySet()) {
                guiDistances.put(entry.getKey().getValue(), entry.getValue());
            }

            switch (step.phase) {
                case START_ITERATION -> desc = "Starting iteration #" + step.iteration + " over all edges.";
                case CHECK_EDGE -> desc = "Found shorter path via edge toward node [" + vId + "]. Relaxing...";
                case UPDATE_DISTANCE -> desc = "Iteration " + step.iteration + ": distance to [" + vId + "] relaxed.";
                case NEGATIVE_CYCLE_FOUND -> desc = "WARNING: Negative-weight cycle detected! Algorithm aborted.";
            }

            track.addFrame(new ShortestPathFrame(desc, step.phase.name(), vId, eId, guiDistances, maxDist));
        }
        return track;
    }

    public static AlgorithmTrack buildSccTrack(pl.edu.uj.discretecalculator.algorithm.SCC_Result<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();

        for (var step : result.getHistory()) {
            String desc = "";
            String vId = step.activeNode != null ? step.activeNode.getValue() : null;
            String eId = step.activeEdge != null ? String.valueOf(step.activeEdge.getId()) : null;

            Map<String, Integer> guiIndices = new HashMap<>();
            for (var entry : step.indicesSnapshot.entrySet()) guiIndices.put(entry.getKey().getValue(), entry.getValue());

            Map<String, Integer> guiLowLinks = new HashMap<>();
            for (var entry : step.lowLinkSnapshot.entrySet()) guiLowLinks.put(entry.getKey().getValue(), entry.getValue());

            List<String> guiStack = new java.util.ArrayList<>();
            for (var v : step.currentStackSnapshot) guiStack.add(v.getValue());

            Map<Integer, List<String>> guiScc = new HashMap<>();
            for (var entry : step.sccSnapshot.entrySet()) {
                List<String> compList = new java.util.ArrayList<>();
                for (var v : entry.getValue()) compList.add(v.getValue());
                guiScc.put(entry.getKey(), compList);
            }

            switch (step.phase) {
                case VISIT_NODE -> desc = "Visiting node [" + vId + "]. Index assigned and pushed onto stack.";
                case CHECK_EDGE -> desc = "Examining neighbor from node [" + vId + "].";
                case UPDATE_LOW_LINK -> desc = "Back link found! Low-link of node [" + vId + "] updated.";
                case COMPONENT_FOUND -> desc = "Node [" + vId + "] is root of a new SCC. Popping from stack.";
            }

            track.addFrame(new TarjanFrame(desc, step.phase.name(), vId, eId, guiIndices, guiLowLinks, guiStack, guiScc));
        }
        return track;
    }

    public static AlgorithmTrack buildKosarajuTrack(KosarajuAlgorithmResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();
        for (KosarajuAlgorithmResult.KosarajuStep<String> step : result.getHistory()) {

            String activeVertexId = step.activeNode != null ? step.activeNode.getValue() : null;
            String activeEdgeId   = step.activeEdge != null ? String.valueOf(step.activeEdge.getId()) : null;

            Set<String> visitedIds = new HashSet<>();
            for (Vertex<String> v : step.visitedSnapshot) visitedIds.add(v.getValue());

            List<String> stackIds = new ArrayList<>();
            for (Vertex<String> v : step.stackSnapshot) stackIds.add(v.getValue());

            Map<Integer, List<String>> sccIds = new HashMap<>();
            for (Map.Entry<Integer, List<Vertex<String>>> entry : step.sccSnapshot.entrySet()) {
                List<String> ids = new ArrayList<>();
                for (Vertex<String> v : entry.getValue()) ids.add(v.getValue());
                sccIds.put(entry.getKey(), ids);
            }

            track.addFrame(new KosarajuFrame(
                    buildKosarajuDescription(step),
                    step.phase.name(),
                    activeVertexId,
                    activeEdgeId,
                    visitedIds,
                    stackIds,
                    sccIds
            ));
        }
        return track;
    }

    private static String buildKosarajuDescription(KosarajuAlgorithmResult.KosarajuStep<String> step) {
        String vLabel = step.activeNode != null ? step.activeNode.getValue() : "?";
        return switch (step.phase) {
            case DFS1_VISIT       -> "Phase 1 – visiting vertex " + vLabel;
            case DFS1_CHECK_EDGE  -> "Phase 1 – checking edge from " + vLabel;
            case DFS1_PUSH        -> "Phase 1 – pushing " + vLabel + " onto stack";
            case DFS2_POP         -> "Phase 2 – popping " + vLabel + " from stack";
            case DFS2_VISIT       -> "Phase 2 – visiting " + vLabel + " (transposed graph)";
            case DFS2_CHECK_EDGE  -> "Phase 2 – checking edge from " + vLabel + " (transposed)";
            case SCC_FOUND        -> "SCC found containing " + vLabel;
        };
    }

    public static AlgorithmTrack buildTopoSortTrack(pl.edu.uj.discretecalculator.algorithm.TopoSortResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();

        for (var step : result.getHistory()) {
            String desc = "";
            String vId = step.activeNode != null ? step.activeNode.getValue() : null;
            String eId = step.activeEdge != null ? String.valueOf(step.activeEdge.getId()) : null;

            Map<String, Integer> guiInDegrees = new HashMap<>();
            for (var entry : step.inDegreesSnapshot.entrySet()) {
                guiInDegrees.put(entry.getKey().getValue(), entry.getValue());
            }

            List<String> guiSorted = new java.util.ArrayList<>();
            for (var v : step.sortedSoFar) {
                guiSorted.add(v.getValue());
            }

            switch (step.phase) {
                case INIT -> desc = "Computed initial in-degrees for all vertices.";
                case TAKE_NODE -> desc = "Took node [" + vId + "] (in-degree = 0) and added to result.";
                case UPDATE_DEGREES -> desc = "Removed edge; in-degree of node [" + vId + "] decremented.";
                case CYCLE_DETECTED -> desc = "ERROR: No node with in-degree 0 remaining. Cycle detected!";
            }

            track.addFrame(new TopoFrame(desc, step.phase.name(), vId, eId, guiInDegrees, guiSorted));
        }
        return track;
    }

    public static AlgorithmTrack buildGreedyColoringTrack(pl.edu.uj.discretecalculator.algorithm.GreedyVertexColoringResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();
        track.addFrame(new ColoringFrame("Greedy Vertex Coloring started"));

        for (var step : result.getHistory()) {
            String desc = "Node [" + step.activeNode.getId() + "] assigned smallest available color: " + step.assignedColor;
            ColoringFrame frame = new ColoringFrame(desc);
            frame.setActiveVertexId(String.valueOf(step.activeNode.getId()));

            for (var entry : step.colorsSnapshot.entrySet()) {
                String hexColor = getColorHexForIndex(entry.getValue());
                frame.getVertexColors().put(String.valueOf(entry.getKey().getId()), hexColor);
            }
            track.addFrame(frame);
        }

        ColoringFrame finalFrame = new ColoringFrame("Chromatic number upper bound: " + result.getChromaticUpperBound());
        if (!track.getFrames().isEmpty()) {
            finalFrame.getVertexColors().putAll(((ColoringFrame) track.getFrames().get(track.getFrames().size() - 1)).getVertexColors());
        }
        track.addFrame(finalFrame);
        return track;
    }

    public static AlgorithmTrack buildGreedyEdgeColoringTrack(pl.edu.uj.discretecalculator.algorithm.GreedyEdgeColoringResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();
        track.addFrame(new ColoringFrame("Greedy Edge Coloring started"));

        Map<String, String> cumulativeColors = new HashMap<>();

        for (var step : result.getHistory()) {
            String numericEdgeId = String.valueOf(step.activeEdge.getId());
            String hexColor = getColorHexForIndex(step.assignedColor);

            String desc = "Edge [" + numericEdgeId + "] assigned color: " + step.assignedColor;
            ColoringFrame frame = new ColoringFrame(desc);

            frame.setActiveEdgeId(numericEdgeId);
            frame.getEdgeColors().putAll(cumulativeColors);
            frame.getEdgeColors().put(numericEdgeId, hexColor);

            cumulativeColors.put(numericEdgeId, hexColor);

            track.addFrame(frame);
        }

        ColoringFrame finalFrame = new ColoringFrame("Done! Chromatic index upper bound: " + result.getChromaticIndexUpperBound());
        if (!track.getFrames().isEmpty()) {
            finalFrame.getEdgeColors().putAll(((ColoringFrame) track.getFrames().get(track.getFrames().size() - 1)).getEdgeColors());
        }
        track.addFrame(finalFrame);
        return track;
    }

    public static AlgorithmTrack buildBacktrackingVertexTrack(pl.edu.uj.discretecalculator.algorithm.BacktrackingAlgorithmForVerticesResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();

        Map<String, String> cumulativeColors = new HashMap<>();
        int currentK = 0;

        for (var step : result.getHistory()) {
            String vId = String.valueOf(step.activeNode.getId());

            if (step.maxColorsAllowed > currentK) {
                currentK = step.maxColorsAllowed;
                track.addFrame(new ColoringFrame(">>> INCREASING COLOR LIMIT TO: " + currentK + " <<<"));
            }

            if (step.phase == pl.edu.uj.discretecalculator.algorithm.BacktrackingAlgorithmForVerticesResult.Phase.TRY_COLOR) {
                String hexColor = getColorHexForIndex(step.color);
                ColoringFrame frame = new ColoringFrame("Trying color " + step.color + " for node [" + vId + "] (limit k=" + step.maxColorsAllowed + ")");
                frame.setActiveVertexId(vId);

                cumulativeColors.put(vId, hexColor);
                frame.getVertexColors().putAll(cumulativeColors);
                track.addFrame(frame);

            } else {
                ColoringFrame failFrame = new ColoringFrame("Dead end! No valid color available. Backtracking from node [" + vId + "].");
                failFrame.setActiveVertexId(vId);

                Map<String, String> alertColors = new HashMap<>(cumulativeColors);
                alertColors.put(vId, "#8B0000");
                failFrame.getVertexColors().putAll(alertColors);
                track.addFrame(failFrame);

                cumulativeColors.remove(vId);
            }
        }

        ColoringFrame finalFrame = new ColoringFrame("Done! Optimal vertex coloring found via backtracking.");
        finalFrame.getVertexColors().putAll(cumulativeColors);
        track.addFrame(finalFrame);

        return track;
    }

    public static AlgorithmTrack buildBacktrackingEdgeTrack(pl.edu.uj.discretecalculator.algorithm.BacktrackingAlgorithmForEdgesResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();

        Map<String, String> cumulativeColors = new HashMap<>();
        int currentK = 0;

        for (var step : result.getHistory()) {
            String numericEdgeId = String.valueOf(step.activeEdge.getId());

            if (step.maxColorsAllowed > currentK) {
                currentK = step.maxColorsAllowed;
                track.addFrame(new ColoringFrame(">>> INCREASING EDGE COLOR LIMIT TO: " + currentK + " <<<"));
            }

            if (step.phase == pl.edu.uj.discretecalculator.algorithm.BacktrackingAlgorithmForEdgesResult.Phase.TRY_COLOR) {
                String hexColor = getColorHexForIndex(step.color);
                ColoringFrame frame = new ColoringFrame("Trying color " + step.color + " for edge [" + numericEdgeId + "] (limit k=" + step.maxColorsAllowed + ")");
                frame.setActiveEdgeId(numericEdgeId);

                cumulativeColors.put(numericEdgeId, hexColor);
                frame.getEdgeColors().putAll(cumulativeColors);
                track.addFrame(frame);

            } else {
                ColoringFrame failFrame = new ColoringFrame("Dead end! Backtracking from edge [" + numericEdgeId + "].");
                failFrame.setActiveEdgeId(numericEdgeId);

                Map<String, String> alertColors = new HashMap<>(cumulativeColors);
                alertColors.put(numericEdgeId, "#8B0000");
                failFrame.getEdgeColors().putAll(alertColors);
                track.addFrame(failFrame);

                cumulativeColors.remove(numericEdgeId);
            }
        }

        ColoringFrame finalFrame = new ColoringFrame("Done! Optimal edge coloring found via backtracking.");
        finalFrame.getEdgeColors().putAll(cumulativeColors);
        track.addFrame(finalFrame);

        return track;
    }
}