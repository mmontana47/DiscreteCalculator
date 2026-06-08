package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.algorithm.BFSResult;
import pl.edu.uj.discretecalculator.algorithm.DFSResult;
import pl.edu.uj.discretecalculator.algorithm.GreedyVCResult;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.algorithm.KosarajuAlgorithmResult;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.*;

public class TrackFactory {

    // Predefiniowana, statyczna paleta kolorów do algorytmów kolorowania
    private static final String[] COLOR_PALETTE = {
            "#E74C3C", // 1. Czerwony
            "#3498DB", // 2. Niebieski
            "#2ECC71", // 3. Zielony
            "#F1C40F", // 4. Żółty
            "#9B59B6", // 5. Fioletowy
            "#E67E22", // 6. Pomarańczowy
            "#1ABC9C", // 7. Morski
            "#E84393", // 8. Różowy
            "#00CECB", // 9. Turkusowy
            "#FF7675"  // 10. Łososiowy
    };

    // --- PUBLICZNE API DLA KONTROLERA ---

    public static AlgorithmTrack buildBfsTrack(BFSResult<String> result, Graph<String> graph) {
        return buildSearchTrackBase(result.getVisitOrder(), result.getParentMap(), result.getNonTreeEdges(), "BFS", graph);
    }

    public static AlgorithmTrack buildDfsTrack(DFSResult<String> result, Graph<String> graph) {
        return buildSearchTrackBase(result.getVisitOrder(), result.getParentMap(), result.getNonTreeEdges(), "DFS", graph);
    }


    // --- PRYWATNA LOGIKA WEWNĘTRZNA DLA BFS/DFS ---

    private static AlgorithmTrack buildSearchTrackBase(List<Vertex<String>> visitOrder,
                                                       Map<Vertex<String>, Vertex<String>> parentMap,
                                                       Set<Edge<String>> cycles,
                                                       String algName,
                                                       Graph<String> graph) {

        AlgorithmTrack track = new AlgorithmTrack();
        SearchAlgorithmFrame initialFrame = new SearchAlgorithmFrame(algName + " started");
        track.addFrame(initialFrame);

        Map<String, String> cumulativeVertexColors = new HashMap<>();
        Map<String, String> cumulativeEdgeColors = new HashMap<>();

        String visitColor = algName.equals("BFS") ? "#2ecc71" : "#e67e22";
        String treeEdgeColor = "#2c3e50";

        for (Vertex<String> currentNode : visitOrder) {
            String nodeId = String.valueOf(currentNode.getId());
            SearchAlgorithmFrame frame = new SearchAlgorithmFrame("Visiting vertex " + nodeId);

            cumulativeVertexColors.put(nodeId, visitColor);

            Vertex<String> parentNode = parentMap.get(currentNode);
            if (parentNode != null) {
                // Skoro mamy getEdges(), to wyszukiwanie jest teraz znacznie prostsze i bezpieczniejsze!
                for (Edge<String> edge : graph.getEdges()) {
                    if ((edge.getSource().getId() == currentNode.getId() && edge.getTarget().getId() == parentNode.getId()) ||
                            (edge.getSource().getId() == parentNode.getId() && edge.getTarget().getId() == currentNode.getId())) {
                        cumulativeEdgeColors.put(String.valueOf(edge.getId()), treeEdgeColor);
                        break;
                    }
                }
            }

            frame.getVertexColors().putAll(cumulativeVertexColors);
            frame.getEdgeColors().putAll(cumulativeEdgeColors);
            track.addFrame(frame);
        }

        if (!cycles.isEmpty()) {
            SearchAlgorithmFrame finalFrame = new SearchAlgorithmFrame("Marking back/cross edges");
            finalFrame.getVertexColors().putAll(cumulativeVertexColors);
            finalFrame.getEdgeColors().putAll(cumulativeEdgeColors);

            for (Edge<String> badEdge : cycles) {
                String edgeId = String.valueOf(badEdge.getId());
                finalFrame.getEdgeColors().put(edgeId, "#e74c3c");
            }
            track.addFrame(finalFrame);
        }

        return track;
    }

    public static AlgorithmTrack buildDijkstraTrack(pl.edu.uj.discretecalculator.algorithm.DijkstraAlgorithmResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();

        // 1. Znajdujemy maksymalny osiągalny dystans dla gradientu
        double maxDist = 0.0;
        for (Double d : result.getFinalDistances().values()) {
            if (d != Double.POSITIVE_INFINITY && d > maxDist) {
                maxDist = d;
            }
        }

        // 2. Budujemy klatki, podając im wyliczone maksimum
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

        // 1. Znajdujemy maksymalny osiągalny dystans dla gradientu
        double maxDist = 0.0;
        for (Double d : result.getFinalDistances().values()) {
            if (d != Double.POSITIVE_INFINITY && d > maxDist) {
                maxDist = d;
            }
        }

        // 2. Budujemy klatki
        for (var step : result.getHistory()) {
            String desc = "";
            String vId = step.activeNode != null ? step.activeNode.getValue() : null;
            String eId = step.activeEdge != null ? String.valueOf(step.activeEdge.getId()) : null;

            Map<String, Double> guiDistances = new HashMap<>();
            for (var entry : step.distancesSnapshot.entrySet()) {
                guiDistances.put(entry.getKey().getValue(), entry.getValue());
            }

            switch (step.phase) {
                case START_ITERATION -> desc = "Starting iteration #" + step.iteration + " over all edges.";
                case CHECK_EDGE -> desc = "Checking edge toward node [" + vId + "] (iteration " + step.iteration + ").";
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

            // Przepakowanie danych dla GUI (Stringi zamiast obiektów Vertex)
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

            // Generowanie czytelnych opisów klatek
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

            // Używamy nowej klatki TopoFrame!
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
                String hex = COLOR_PALETTE[(entry.getValue() - 1) % COLOR_PALETTE.length];
                frame.getVertexColors().put(String.valueOf(entry.getKey().getId()), hex);
            }
            track.addFrame(frame);
        }

        ColoringFrame finalFrame = new ColoringFrame("Done! Chromatic number upper bound: " + result.getChromaticUpperBound());
        if (!track.getFrames().isEmpty()) {
            finalFrame.getVertexColors().putAll(((ColoringFrame) track.getFrames().get(track.getFrames().size() - 1)).getVertexColors());
        }
        track.addFrame(finalFrame);
        return track;
    }

    // W TrackFactory.java

    public static AlgorithmTrack buildGreedyEdgeColoringTrack(pl.edu.uj.discretecalculator.algorithm.GreedyEdgeColoringResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();
        track.addFrame(new ColoringFrame("Greedy Edge Coloring started"));

        // Trzymamy "bieżący" stan kolorów poza snapshotami algorytmu
        Map<String, String> cumulativeColors = new HashMap<>();

        for (var step : result.getHistory()) {
            String numericEdgeId = String.valueOf(step.activeEdge.getId());
            String hexColor = COLOR_PALETTE[(step.assignedColor - 1) % COLOR_PALETTE.length];

            String desc = "Edge [" + numericEdgeId + "] assigned color: " + step.assignedColor;
            ColoringFrame frame = new ColoringFrame(desc);

            // 1. Zaznaczamy krawędź jako aktywną (będzie pogrubiona)
            frame.setActiveEdgeId(numericEdgeId);

            // 2. Wgrywamy WSZYSTKIE poprzednie kolory do klatki
            frame.getEdgeColors().putAll(cumulativeColors);

            // 3. JAWNIE dodajemy NOWY kolor do TEJ klatki (nawet jeśli snapshot tego nie zrobił synchronicznie)
            frame.getEdgeColors().put(numericEdgeId, hexColor);

            // Aktualizujemy nasz skumulowany stan dla następnych klatek
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
        int currentK = 0; // Do śledzenia, czy zwiększyliśmy limit kolorów

        for (var step : result.getHistory()) {
            String vId = String.valueOf(step.activeNode.getId());

            // Opcjonalna klatka informacyjna, gdy algorytm zwiększa 'k'
            if (step.maxColorsAllowed > currentK) {
                currentK = step.maxColorsAllowed;
                track.addFrame(new ColoringFrame(">>> INCREASING COLOR LIMIT TO: " + currentK + " <<<"));
            }

            if (step.phase == pl.edu.uj.discretecalculator.algorithm.BacktrackingAlgorithmForVerticesResult.Phase.TRY_COLOR) {
                // --- FAZA 1: PRÓBA KOLORU ---
                String hexColor = COLOR_PALETTE[(step.color - 1) % COLOR_PALETTE.length];
                ColoringFrame frame = new ColoringFrame("Trying color " + step.color + " for node [" + vId + "] (limit k=" + step.maxColorsAllowed + ")");
                frame.setActiveVertexId(vId); // Pogrubiamy

                cumulativeColors.put(vId, hexColor);
                frame.getVertexColors().putAll(cumulativeColors);
                track.addFrame(frame);

            } else {
                // --- FAZA 2: ŚLEPY ZAUŁEK I COFNIĘCIE ---
                // Tworzymy klatkę "ALARMOWĄ", żeby uwydatnić porażkę
                ColoringFrame failFrame = new ColoringFrame("Dead end! No valid color available. Backtracking from node [" + vId + "].");
                failFrame.setActiveVertexId(vId); // Utrzymujemy pogrubienie

                // Zmieniamy kolor na bordowy/alarmowy, żeby pokazać konflikt!
                Map<String, String> alertColors = new HashMap<>(cumulativeColors);
                alertColors.put(vId, "#8B0000"); // Ciemna czerwień oznaczająca błąd
                failFrame.getVertexColors().putAll(alertColors);
                track.addFrame(failFrame);

                // Fizycznie usuwamy kolor z naszej trwałej pamięci
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

            // Opcjonalna klatka informacyjna przy zwiększeniu limitu
            if (step.maxColorsAllowed > currentK) {
                currentK = step.maxColorsAllowed;
                track.addFrame(new ColoringFrame(">>> INCREASING EDGE COLOR LIMIT TO: " + currentK + " <<<"));
            }

            if (step.phase == pl.edu.uj.discretecalculator.algorithm.BacktrackingAlgorithmForEdgesResult.Phase.TRY_COLOR) {
                // --- FAZA 1: PRÓBA KOLORU ---
                String hexColor = COLOR_PALETTE[(step.color - 1) % COLOR_PALETTE.length];
                ColoringFrame frame = new ColoringFrame("Trying color " + step.color + " for edge [" + numericEdgeId + "] (limit k=" + step.maxColorsAllowed + ")");
                frame.setActiveEdgeId(numericEdgeId); // Pogrubiamy

                cumulativeColors.put(numericEdgeId, hexColor);
                frame.getEdgeColors().putAll(cumulativeColors);
                track.addFrame(frame);

            } else {
                // --- FAZA 2: ŚLEPY ZAUŁEK I COFNIĘCIE ---
                ColoringFrame failFrame = new ColoringFrame("Dead end! Backtracking from edge [" + numericEdgeId + "].");
                failFrame.setActiveEdgeId(numericEdgeId); // Utrzymujemy pogrubienie

                // Zmieniamy kolor na bordowy/alarmowy
                Map<String, String> alertColors = new HashMap<>(cumulativeColors);
                alertColors.put(numericEdgeId, "#8B0000");
                failFrame.getEdgeColors().putAll(alertColors);
                track.addFrame(failFrame);

                // Usuwamy kolor z naszej trwałej pamięci
                cumulativeColors.remove(numericEdgeId);
            }
        }

        ColoringFrame finalFrame = new ColoringFrame("Done! Optimal edge coloring found via backtracking.");
        finalFrame.getEdgeColors().putAll(cumulativeColors);
        track.addFrame(finalFrame);

        return track;
    }
}