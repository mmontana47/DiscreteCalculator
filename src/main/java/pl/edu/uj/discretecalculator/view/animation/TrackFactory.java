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
        SearchAlgorithmFrame initialFrame = new SearchAlgorithmFrame("Uruchomiono algorytm " + algName);
        track.addFrame(initialFrame);

        Map<String, String> cumulativeVertexColors = new HashMap<>();
        Map<String, String> cumulativeEdgeColors = new HashMap<>();

        String visitColor = algName.equals("BFS") ? "#2ecc71" : "#e67e22";
        String treeEdgeColor = "#2c3e50";

        for (Vertex<String> currentNode : visitOrder) {
            String nodeId = String.valueOf(currentNode.getId());
            SearchAlgorithmFrame frame = new SearchAlgorithmFrame("Odwiedzam wierzchołek " + nodeId);

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
            SearchAlgorithmFrame finalFrame = new SearchAlgorithmFrame("Zaznaczanie krawędzi wstecznych/krzyżowych");
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
                case VISIT_NODE -> desc = "Pobrano węzeł [" + vId + "] z kolejki jako najbliższy.";
                case CHECK_EDGE -> desc = "Badanie krawędzi prowadzącej do węzła [" + vId + "].";
                case UPDATE_DISTANCE -> desc = "Udany update! Skrócono dystans do węzła [" + vId + "].";
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
                case START_ITERATION -> desc = "Rozpoczęto pełną iterację nr " + step.iteration + " po wszystkich krawędziach.";
                case CHECK_EDGE -> desc = "Badanie krawędzi w kierunku węzła [" + vId + "] (Iteracja " + step.iteration + ").";
                case UPDATE_DISTANCE -> desc = "Relaksacja w iteracji " + step.iteration + " zaktualizowała dystans do [" + vId + "].";
                case NEGATIVE_CYCLE_FOUND -> desc = "UWAGA: Znaleziono cykl o ujemnej wadze! Algorytm przerwany.";
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
                case VISIT_NODE -> desc = "Odwiedzanie węzła [" + vId + "]. Przypisano index i odłożono go na stos.";
                case CHECK_EDGE -> desc = "Analiza sąsiada z węzła [" + vId + "].";
                case UPDATE_LOW_LINK -> desc = "Znaleziono powiązanie! Zaktualizowano wartość Low-Link dla węzła [" + vId + "].";
                case COMPONENT_FOUND -> desc = "Sukces! Węzeł [" + vId + "] jest korzeniem nowej Silnie Spójnej Składowej. Zdejmowanie ze stosu.";
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
            case DFS1_VISIT       -> "Faza 1 – odwiedzam wierzchołek " + vLabel;
            case DFS1_CHECK_EDGE  -> "Faza 1 – sprawdzam krawędź z " + vLabel;
            case DFS1_PUSH        -> "Faza 1 – odkładam " + vLabel + " na stos";
            case DFS2_POP         -> "Faza 2 – zdejmuję " + vLabel + " ze stosu";
            case DFS2_VISIT       -> "Faza 2 – odwiedzam " + vLabel + " (graf transponowany)";
            case DFS2_CHECK_EDGE  -> "Faza 2 – sprawdzam krawędź z " + vLabel + " (transponowana)";
            case SCC_FOUND        -> "Znaleziono SCC zawierające " + vLabel;
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
                case INIT -> desc = "Wyliczono początkowe stopnie wejściowe (in-degree) dla wszystkich wierzchołków.";
                case TAKE_NODE -> desc = "Pobrano węzeł [" + vId + "] (in-degree = 0) i dodano do wyniku.";
                case UPDATE_DEGREES -> desc = "Obgryzanie grafu: usunięto krawędź. Stopień wejściowy węzła [" + vId + "] maleje.";
                case CYCLE_DETECTED -> desc = "BŁĄD: Graf zablokował się (brak węzłów z in-degree 0). Wykryto cykl!";
            }

            // Używamy nowej klatki TopoFrame!
            track.addFrame(new TopoFrame(desc, step.phase.name(), vId, eId, guiInDegrees, guiSorted));
        }
        return track;
    }

    public static AlgorithmTrack buildGreedyColoringTrack(pl.edu.uj.discretecalculator.algorithm.GreedyVertexColoringResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();
        track.addFrame(new ColoringFrame("Uruchomiono Zachłanne Kolorowanie Wierzchołków (BFS)"));

        for (var step : result.getHistory()) {
            String desc = "Węzeł [" + step.activeNode.getId() + "] otrzymuje najmniejszy wolny kolor: " + step.assignedColor;
            ColoringFrame frame = new ColoringFrame(desc);
            frame.setActiveVertexId(String.valueOf(step.activeNode.getId()));

            for (var entry : step.colorsSnapshot.entrySet()) {
                String hex = COLOR_PALETTE[(entry.getValue() - 1) % COLOR_PALETTE.length];
                frame.getVertexColors().put(String.valueOf(entry.getKey().getId()), hex);
            }
            track.addFrame(frame);
        }

        ColoringFrame finalFrame = new ColoringFrame("Zakończono! Górne oszacowanie liczby chromatycznej: " + result.getChromaticUpperBound());
        if (!track.getFrames().isEmpty()) {
            finalFrame.getVertexColors().putAll(((ColoringFrame) track.getFrames().get(track.getFrames().size() - 1)).getVertexColors());
        }
        track.addFrame(finalFrame);
        return track;
    }

    // W TrackFactory.java

    public static AlgorithmTrack buildGreedyEdgeColoringTrack(pl.edu.uj.discretecalculator.algorithm.GreedyEdgeColoringResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();
        track.addFrame(new ColoringFrame("Uruchomiono Zachłanne Kolorowanie Krawędzi"));

        // Trzymamy "bieżący" stan kolorów poza snapshotami algorytmu
        Map<String, String> cumulativeColors = new HashMap<>();

        for (var step : result.getHistory()) {
            String numericEdgeId = String.valueOf(step.activeEdge.getId());
            String hexColor = COLOR_PALETTE[(step.assignedColor - 1) % COLOR_PALETTE.length];

            String desc = "Krawędź [" + numericEdgeId + "] otrzymuje kolor: " + step.assignedColor;
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

        ColoringFrame finalFrame = new ColoringFrame("Zakończono! Górne oszacowanie indeksu chromatycznego: " + result.getChromaticIndexUpperBound());
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
                track.addFrame(new ColoringFrame(">>> ZWIĘKSZAM LIMIT KOLORÓW DO: " + currentK + " <<<"));
            }

            if (step.phase == pl.edu.uj.discretecalculator.algorithm.BacktrackingAlgorithmForVerticesResult.Phase.TRY_COLOR) {
                // --- FAZA 1: PRÓBA KOLORU ---
                String hexColor = COLOR_PALETTE[(step.color - 1) % COLOR_PALETTE.length];
                ColoringFrame frame = new ColoringFrame("Próba koloru " + step.color + " dla węzła [" + vId + "] (Limit k=" + step.maxColorsAllowed + ")");
                frame.setActiveVertexId(vId); // Pogrubiamy

                cumulativeColors.put(vId, hexColor);
                frame.getVertexColors().putAll(cumulativeColors);
                track.addFrame(frame);

            } else {
                // --- FAZA 2: ŚLEPY ZAUŁEK I COFNIĘCIE ---
                // Tworzymy klatkę "ALARMOWĄ", żeby uwydatnić porażkę
                ColoringFrame failFrame = new ColoringFrame("Ślepy zaułek! Brakuje dopuszczalnych kolorów. Wycofuję węzeł [" + vId + "].");
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

        ColoringFrame finalFrame = new ColoringFrame("Sukces! Znaleziono optymalne pokolorowanie metodą Backtrackingu.");
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
                track.addFrame(new ColoringFrame(">>> ZWIĘKSZAM LIMIT KOLORÓW KRAWĘDZI DO: " + currentK + " <<<"));
            }

            if (step.phase == pl.edu.uj.discretecalculator.algorithm.BacktrackingAlgorithmForEdgesResult.Phase.TRY_COLOR) {
                // --- FAZA 1: PRÓBA KOLORU ---
                String hexColor = COLOR_PALETTE[(step.color - 1) % COLOR_PALETTE.length];
                ColoringFrame frame = new ColoringFrame("Próba koloru " + step.color + " dla krawędzi [" + numericEdgeId + "] (Limit k=" + step.maxColorsAllowed + ")");
                frame.setActiveEdgeId(numericEdgeId); // Pogrubiamy

                cumulativeColors.put(numericEdgeId, hexColor);
                frame.getEdgeColors().putAll(cumulativeColors);
                track.addFrame(frame);

            } else {
                // --- FAZA 2: ŚLEPY ZAUŁEK I COFNIĘCIE ---
                ColoringFrame failFrame = new ColoringFrame("Ślepy zaułek! Wycofuję kolor z krawędzi [" + numericEdgeId + "].");
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

        ColoringFrame finalFrame = new ColoringFrame("Sukces! Znaleziono optymalne pokolorowanie krawędzi metodą Backtrackingu.");
        finalFrame.getEdgeColors().putAll(cumulativeColors);
        track.addFrame(finalFrame);

        return track;
    }
}