package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;
import pl.edu.uj.discretecalculator.model.graph.*;

public class DijkstraAlgorithm<V> implements AlgorithmicInterface<V, DijkstraAlgorithmResult<V>> {
    private final Vertex<V> startNode;

    public DijkstraAlgorithm(Vertex<V> startNode) {
        this.startNode = startNode;
    }

    @Override
    public String algorithmName() { return "Dijkstra Algorithm"; }

    @Override
    public DijkstraAlgorithmResult<V> start(Graph<V> g) {
        //nie musimy rozwazac WeightedDirected - tutaj dziedziczenie jest ok
        if (!(g instanceof WeightedGraph<?>)) {
            throw new IllegalArgumentException("Dijkstra requires for the graph to be weighted.");
        }

        WeightedGraph<V> graph = (WeightedGraph<V>) g;
        DijkstraAlgorithmResult<V> result = new DijkstraAlgorithmResult<>();

        Map<Vertex<V>, Double> distances = new HashMap<>();
        Map<Vertex<V>, Vertex<V>> parents = new HashMap<>();
        Set<Vertex<V>> visited = new HashSet<>();
        PriorityQueue<VertexDistance<V>> queue = new PriorityQueue<>(Comparator.comparingDouble(vd -> vd.distance));

        for (Vertex<V> vertex : graph.getVertices()) {
            distances.put(vertex, vertex.equals(startNode) ? 0.0 : Double.POSITIVE_INFINITY);
        }

        queue.add(new VertexDistance<>(startNode, 0.0));

        while (!queue.isEmpty()) {
            Vertex<V> current = queue.poll().vertex;

            if (visited.contains(current)) continue;
            visited.add(current);

            result.addStep(new DijkstraAlgorithmResult.DijkstraStep<>(
                    DijkstraAlgorithmResult.Phase.VISIT_NODE, current, null, distances, parents));

            for (Edge<V> e : graph.getIncidentEdges(current)) {
                WeightedEdge<V> edge = (WeightedEdge<V>) e;
                Vertex<V> neighbor = edge.getTarget().equals(current) ? edge.getSource() : edge.getTarget();

                // skip reversed direction for directed edges
                if (edge instanceof WeightedDirectedEdge<?> && edge.getTarget().equals(current)) continue;

                if (visited.contains(neighbor)) continue;

                result.addStep(new DijkstraAlgorithmResult.DijkstraStep<>(
                        DijkstraAlgorithmResult.Phase.CHECK_EDGE, neighbor, edge, distances, parents));

                double newDist = distances.get(current) + edge.getWeight();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    parents.put(neighbor, current);
                    queue.add(new VertexDistance<>(neighbor, newDist));

                    result.addStep(new DijkstraAlgorithmResult.DijkstraStep<>(
                            DijkstraAlgorithmResult.Phase.UPDATE_DISTANCE, neighbor, edge, distances, parents));
                }
            }
        }

        result.getFinalDistances().putAll(distances);
        result.getFinalParents().putAll(parents);

        return result;
    }

    private static class VertexDistance<V> {
        final Vertex<V> vertex;
        final double distance;
        VertexDistance(Vertex<V> vertex, double distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }
}