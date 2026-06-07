package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import java.util.*;


//WAŻNE: porzuciłem użycie klasy ColouredVertex.
//imo trzeba byłoby wtedy bardziej uważać z kodem w dalszej części apki, bo nadpisujemy w takiej sytuacji inherentną własność wierzchołka
//analogicznie zmieniłem dla wszystkich innych algorytmow
//zmienianie takich właściwości raczej powinno się ograniczać do świadomych operacji użytkownika w panelu Edit Graph
// jak zmienianie wag krawędzi lub skierowania grafu
public class GreedyVertexColoring<V> implements AlgorithmicInterface<V, GreedyVertexColoringResult<V>> {
    private final Vertex<V> startingVertex;

    public GreedyVertexColoring(Vertex<V> startingVertex) {
        this.startingVertex = startingVertex;
    }

    @Override
    public String algorithmName() {
        return "Zachłanne Kolorowanie Wierzchołków";
    }

    @Override
    public GreedyVertexColoringResult<V> start(Graph<V> graph) {
        GreedyVertexColoringResult<V> result = new GreedyVertexColoringResult<>();
        Map<Vertex<V>, Integer> colors = new HashMap<>();
        Set<Vertex<V>> visited = new HashSet<>();
        Queue<Vertex<V>> queue = new LinkedList<>();

        int maxColorUsed = 0;

        if (startingVertex != null && graph.getVertices().contains(startingVertex)) {
            queue.add(startingVertex);
            visited.add(startingVertex);
        }

        while (!queue.isEmpty()) {
            Vertex<V> current = queue.poll();
            int color = colorVertex(current, graph, colors);
            maxColorUsed = Math.max(maxColorUsed, color);
            result.addStep(new GreedyVertexColoringResult.ColoringStep<>(
                    GreedyVertexColoringResult.Phase.NODE_COLORED, current, color, colors));

            for (Vertex<V> neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // obsluga innych skladowych
        for (Vertex<V> v : graph.getVertices()) {
            if (!visited.contains(v)) {
                visited.add(v);
                int color = colorVertex(v, graph, colors);
                maxColorUsed = Math.max(maxColorUsed, color);

                result.addStep(new GreedyVertexColoringResult.ColoringStep<>(
                        GreedyVertexColoringResult.Phase.NODE_COLORED, v, color, colors));
            }
        }

        // koncowa klatka
        result.getFinalColors().putAll(colors);
        result.setChromaticUpperBound(maxColorUsed);

        return result;
    }

    private int colorVertex(Vertex<V> vertex, Graph<V> graph, Map<Vertex<V>, Integer> colors) {
        Set<Integer> neighborColors = new HashSet<>();

        for (Vertex<V> neighbor : graph.getNeighbors(vertex)) {
            if (colors.containsKey(neighbor)) {
                neighborColors.add(colors.get(neighbor));
            }
        }

        int colorToAssign = 1;
        while (neighborColors.contains(colorToAssign)) {
            colorToAssign++;
        }

        colors.put(vertex, colorToAssign);
        return colorToAssign;
    }
}