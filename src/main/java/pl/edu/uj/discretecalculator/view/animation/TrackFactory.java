package pl.edu.uj.discretecalculator.view.animation;

import pl.edu.uj.discretecalculator.algorithm.BFSResult;
import pl.edu.uj.discretecalculator.algorithm.DFSResult;
import pl.edu.uj.discretecalculator.algorithm.GreedyVCResult;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static AlgorithmTrack buildGreedyColoringTrack(GreedyVCResult<String> result, Graph<String> graph) {
        AlgorithmTrack track = new AlgorithmTrack();

        // 0. Klatka początkowa
        ColoringFrame initialFrame = new ColoringFrame("Uruchomiono Algorytm Zachłanny Kolorowania");
        track.addFrame(initialFrame);

        Map<String, String> cumulativeVertexColors = new HashMap<>();
        Map<String, String> cumulativeEdgeColors = new HashMap<>();

        // Magiczna sztuczka wizualna: wyszarzamy krawędzie przed startem!
        for (Edge<String> edge : graph.getEdges()) {
            cumulativeEdgeColors.put(String.valueOf(edge.getId()), "#BDC3C7");
        }
        initialFrame.getEdgeColors().putAll(cumulativeEdgeColors);

        // 1. Nakładamy kolory klatka po klatce
        List<Vertex<String>> order = result.getColoringOrder();
        Map<Vertex<String>, Integer> assignedColors = result.getAssignedColors();

        for (Vertex<String> vertex : order) {
            String nodeId = String.valueOf(vertex.getId());
            int colorInt = assignedColors.get(vertex);

            // Mapowanie na HEX z zabezpieczeniem (zawijanie dla więcej niż 10 kolorów)
            String hexColor = COLOR_PALETTE[(colorInt - 1) % COLOR_PALETTE.length];

            ColoringFrame frame = new ColoringFrame("Odwiedzam wierzchołek " + nodeId + " (Kolor: " + colorInt + ")");

            // Nadpisujemy stary stan nowym pokolorowanym wierzchołkiem
            cumulativeVertexColors.put(nodeId, hexColor);

            frame.getVertexColors().putAll(cumulativeVertexColors);
            frame.getEdgeColors().putAll(cumulativeEdgeColors);
            track.addFrame(frame);
        }

        // 2. Klatka końcowa (Podsumowanie)
        ColoringFrame finalFrame = new ColoringFrame("Zakończono. Górne oszacowanie liczby chromatycznej: " + result.getChromaticNumberUpperBound());
        finalFrame.getVertexColors().putAll(cumulativeVertexColors);
        finalFrame.getEdgeColors().putAll(cumulativeEdgeColors);
        track.addFrame(finalFrame);

        return track;
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
}