package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;

public class BacktrackingAlgorithmForVertices<V> implements AlgorithmicInterface<V, BacktrackingAlgorithmForVerticesResult<V>> {
    private final Vertex<V> startingVertex;

    public BacktrackingAlgorithmForVertices(Vertex<V> startingVertex) {
        this.startingVertex = startingVertex;
    }

    @Override
    public String algorithmName() { return "Algorytm Backtracking dla wierzchołków"; }

    @Override
    public BacktrackingAlgorithmForVerticesResult<V> start(Graph<V> graph) {
        BacktrackingAlgorithmForVerticesResult<V> result = new BacktrackingAlgorithmForVerticesResult<>();
        int graphSize = graph.getVertices().size();

        // Jeśli graf jest pusty
        if (graphSize == 0) return result;

        // slabe ograniczenie, ale i tak kolorowanie sie znajdzie
        for (int k = 1; k <= graphSize; k++) {

            //result.clearHistory(); // do czyszczenia nieudanych prob
            Map<Vertex<V>, Integer> colors = new HashMap<>();
            Deque<Vertex<V>> verticesDeque = new ArrayDeque<>();

            //zmieniłem żeby szło od wybranego wierzchołka
            if (startingVertex != null && graph.getVertices().contains(startingVertex)) {
                verticesDeque.addLast(startingVertex);
            }
            for (Vertex<V> v : graph.getVertices()) {
                if (!v.equals(startingVertex)) {
                    verticesDeque.addLast(v);
                }
            }

            // łamanie symetrii kolorów (parę razy mniej klatek)
            Vertex<V> firstVertex = verticesDeque.pollFirst();
            colors.put(firstVertex, 1);

            result.addStep(new BacktrackingAlgorithmForVerticesResult.BacktrackVertexStep<>(
                    BacktrackingAlgorithmForVerticesResult.Phase.TRY_COLOR, firstVertex, 1, k, colors));

            if (solve(verticesDeque, colors, result, k, graph)) {
                result.getFinalColors().putAll(colors);
                return result;
            }

            colors.remove(firstVertex);
            result.addStep(new BacktrackingAlgorithmForVerticesResult.BacktrackVertexStep<>(
                    BacktrackingAlgorithmForVerticesResult.Phase.BACKTRACK, firstVertex, 0, k, colors));
        }
        return result;
    }

    private boolean solve(Deque<Vertex<V>> vertices, Map<Vertex<V>, Integer> colors,
                          BacktrackingAlgorithmForVerticesResult<V> result, int maxColorsAllowed, Graph<V> graph) {

        if (vertices.isEmpty()) return true;

        Vertex<V> current = vertices.pollFirst();

        for (int c = 1; c <= maxColorsAllowed; c++) {
            if (isSafe(current, c, colors, graph)) {

                colors.put(current, c);
                result.addStep(new BacktrackingAlgorithmForVerticesResult.BacktrackVertexStep<>(
                        BacktrackingAlgorithmForVerticesResult.Phase.TRY_COLOR, current, c, maxColorsAllowed, colors));

                if (solve(vertices, colors, result, maxColorsAllowed, graph)) {
                    return true;
                }

                colors.remove(current);
                result.addStep(new BacktrackingAlgorithmForVerticesResult.BacktrackVertexStep<>(
                        BacktrackingAlgorithmForVerticesResult.Phase.BACKTRACK, current, 0, maxColorsAllowed, colors));
            }
        }

        //drobna pomylka - wczesniej dodawalismy current w petli powyzej, powinnismy go dodac z powrotem po potwierdzeniu
        //że nic pozniej nie działa
        vertices.addFirst(current);
        return false;
    }

    private boolean isSafe(Vertex<V> vertex, int color, Map<Vertex<V>, Integer> colors, Graph<V> graph) {
        for (Vertex<V> neighbor : graph.getNeighbors(vertex)) {
            if (colors.getOrDefault(neighbor, 0) == color) return false;
        }
        return true;
    }
}