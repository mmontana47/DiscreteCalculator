package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;
import pl.edu.uj.discretecalculator.exception.BellmanFordAlgorithmException;
import pl.edu.uj.discretecalculator.model.graph.*;

public class BellmanFordAlgorithm<V> implements AlgorithmicInterface<V, BellmanFordResult<V>> {
    private final Vertex<V> startNode;

    public BellmanFordAlgorithm(Vertex<V> startNode) {
        this.startNode = startNode;
    }

    @Override
    public String algorithmName() { return "Bellman Ford Algorithm"; }

    @Override
    public BellmanFordResult<V> start(Graph<V> g) {
        if (!(g instanceof WeightedGraph<?>)) {
            throw new BellmanFordAlgorithmException("Bellman-Ford requires for the graph to be weighted.");
        }

        WeightedGraph<V> graph = (WeightedGraph<V>) g;
        BellmanFordResult<V> result = new BellmanFordResult<>();

        Map<Vertex<V>, Double> distances = new HashMap<>();
        Map<Vertex<V>, Vertex<V>> parents = new HashMap<>();

        for (Vertex<V> vertex : graph.getVertices()) {
            distances.put(vertex, vertex.equals(startNode) ? 0.0 : Double.POSITIVE_INFINITY);
        }

        int verticesCount = graph.getSize();

        for (int i = 1; i < verticesCount; i++) {

            result.addStep(new BellmanFordResult.BellmanFordStep<>(
                    BellmanFordResult.Phase.START_ITERATION, i, null, null, distances, parents));

            for (Edge<V> e : graph.getEdges()) {
                WeightedEdge<V> edge = (WeightedEdge<V>) e;

                relaxEdge(edge, edge.getSource(), edge.getTarget(), i, distances, parents, result);

                if (!(edge instanceof WeightedDirectedEdge<?>)) {
                    relaxEdge(edge, edge.getTarget(), edge.getSource(), i, distances, parents, result);
                }
            }
        }

        // sprawdzenie cykli o ujemnej wadze (V-ta iteracja)
        for (Edge<V> e : graph.getEdges()) {
            WeightedEdge<V> edge = (WeightedEdge<V>) e;

            if (hasNegativeCycle(edge, edge.getSource(), edge.getTarget(), distances, parents, result)) {
                result.getFinalDistances().putAll(distances);
                result.getFinalParents().putAll(parents);
                return result;
            }
            if (!(edge instanceof WeightedDirectedEdge<?>)) {
                if (hasNegativeCycle(edge, edge.getTarget(), edge.getSource(), distances, parents, result)) {
                    result.getFinalDistances().putAll(distances);
                    result.getFinalParents().putAll(parents);
                    return result;
                }
            }
        }

        // ostateczny stan
        result.getFinalDistances().putAll(distances);
        result.getFinalParents().putAll(parents);

        return result;
    }

    private void relaxEdge(WeightedEdge<V> edge, Vertex<V> source, Vertex<V> target, int iteration,
                           Map<Vertex<V>, Double> distances, Map<Vertex<V>, Vertex<V>> parents, BellmanFordResult<V> result) {

        // badanie krawędzi
        result.addStep(new BellmanFordResult.BellmanFordStep<>(
                BellmanFordResult.Phase.CHECK_EDGE, iteration, edge, target, distances, parents));

        if (distances.get(source) != Double.POSITIVE_INFINITY) {
            double newDist = distances.get(source) + edge.getWeight();
            if (newDist < distances.get(target)) {
                distances.put(target, newDist);
                parents.put(target, source);

                // udana relaksacja
                result.addStep(new BellmanFordResult.BellmanFordStep<>(
                        BellmanFordResult.Phase.UPDATE_DISTANCE, iteration, edge, target, distances, parents));
            }
        }
    }

    private boolean hasNegativeCycle(WeightedEdge<V> edge, Vertex<V> source, Vertex<V> target,
                                     Map<Vertex<V>, Double> distances, Map<Vertex<V>, Vertex<V>> parents, BellmanFordResult<V> result) {
        if (distances.get(source) != Double.POSITIVE_INFINITY && distances.get(source) + edge.getWeight() < distances.get(target)) {
            result.addStep(new BellmanFordResult.BellmanFordStep<>(
                    BellmanFordResult.Phase.NEGATIVE_CYCLE_FOUND, -1, edge, target, distances, parents));
            return true;
        }
        return false;
    }
}