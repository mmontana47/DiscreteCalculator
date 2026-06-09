package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import java.util.*;

public class GreedyEdgeColoring<V> implements AlgorithmicInterface<V, GreedyEdgeColoringResult<V>> {

    @Override
    public String algorithmName() {
        return "Zachłanne Kolorowanie Krawędzi";
    }

    @Override
    public GreedyEdgeColoringResult<V> start(Graph<V> graph) {
        GreedyEdgeColoringResult<V> result = new GreedyEdgeColoringResult<>();
        Map<Edge<V>, Integer> edgeColors = new HashMap<>();
        int maxColorUsed = 0;

        for (Edge<V> edge : graph.getEdges()) {

            // dodałem lokalne palety użytych kolorów
            Set<Integer> neighborColors = new HashSet<>();

            // logika sie zmieni dla grafow skierowanych (!).
            for (Edge<V> e1 : graph.getEdges()) {
                if (!e1.equals(edge) && edgeColors.containsKey(e1)) {
                    if (e1.getSource().equals(edge.getSource()) ||
                            e1.getSource().equals(edge.getTarget()) ||
                            e1.getTarget().equals(edge.getSource()) ||
                            e1.getTarget().equals(edge.getTarget())) {
                        neighborColors.add(edgeColors.get(e1));
                    }
                }
            }

            int colorToAssign = 1;
            while (neighborColors.contains(colorToAssign)) {
                colorToAssign++;
            }

            // apliikowanie i update
            edgeColors.put(edge, colorToAssign);
            maxColorUsed = Math.max(maxColorUsed, colorToAssign);

            result.addStep(new GreedyEdgeColoringResult.EdgeColoringStep<>(
                    GreedyEdgeColoringResult.Phase.EDGE_COLORED, edge, colorToAssign, edgeColors));
        }

        result.getFinalColors().putAll(edgeColors);
        result.setChromaticIndexUpperBound(maxColorUsed);

        return result;
    }
}