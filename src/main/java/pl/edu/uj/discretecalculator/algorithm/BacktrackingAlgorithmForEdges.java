package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;

public class BacktrackingAlgorithmForEdges<V> implements AlgorithmicInterface<V, BacktrackingAlgorithmForEdgesResult<V>> {

    @Override
    public String algorithmName() { return "Algorytm Backtracking dla krawędzi"; }

    @Override
    public BacktrackingAlgorithmForEdgesResult<V> start(Graph<V> graph) {
        BacktrackingAlgorithmForEdgesResult<V> result = new BacktrackingAlgorithmForEdgesResult<>();
        List<Edge<V>> edgeList = new ArrayList<>(graph.getEdges());
        int maxEdges = edgeList.size();

        for (int k = 1; k <= maxEdges; k++) {
            Map<Edge<V>, Integer> colors = new HashMap<>();
            Deque<Edge<V>> deque = new ArrayDeque<>(edgeList);

            // symmetry breaking: fix first edge to color 1
            Edge<V> firstEdge = deque.pollFirst();
            colors.put(firstEdge, 1);

            result.addStep(new BacktrackingAlgorithmForEdgesResult.BacktrackStep<>(
                    BacktrackingAlgorithmForEdgesResult.Phase.TRY_COLOR, firstEdge, 1, k, colors));

            if (solve(deque, colors, result, k, graph)) {
                result.getFinalColors().putAll(colors);
                return result;
            }

            colors.remove(firstEdge);
            result.addStep(new BacktrackingAlgorithmForEdgesResult.BacktrackStep<>(
                    BacktrackingAlgorithmForEdgesResult.Phase.BACKTRACK, firstEdge, 0, k, colors));
        }
        return result;
    }

    private boolean solve(Deque<Edge<V>> edges, Map<Edge<V>, Integer> colors,
                          BacktrackingAlgorithmForEdgesResult<V> result, int maxColorsAllowed, Graph<V> graph) {

        if (edges.isEmpty()) return true;

        Edge<V> current = edges.pollFirst();

        for (int c = 1; c <= maxColorsAllowed; c++) {
            if (isSafe(current, c, colors, graph)) {

                colors.put(current, c);
                result.addStep(new BacktrackingAlgorithmForEdgesResult.BacktrackStep<>(
                        BacktrackingAlgorithmForEdgesResult.Phase.TRY_COLOR, current, c, maxColorsAllowed, colors));

                if (solve(edges, colors, result, maxColorsAllowed, graph)) {
                    return true;
                }

                colors.remove(current);
                result.addStep(new BacktrackingAlgorithmForEdgesResult.BacktrackStep<>(
                        BacktrackingAlgorithmForEdgesResult.Phase.BACKTRACK, current, 0, maxColorsAllowed, colors));

            }
        }

        //analogiczna poprawka jak w backtrackingVC
        edges.addFirst(current);
        return false;
    }

    private boolean isSafe(Edge<V> edge, int color, Map<Edge<V>, Integer> colors, Graph<V> graph) {
        for (Edge<V> incident : graph.getIncidentEdges(edge.getSource())) {
            if (!incident.equals(edge) && colors.getOrDefault(incident, 0) == color) return false;
        }
        for (Edge<V> incident : graph.getIncidentEdges(edge.getTarget())) {
            if (!incident.equals(edge) && colors.getOrDefault(incident, 0) == color) return false;
        }
        return true;
    }
}