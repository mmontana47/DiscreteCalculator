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
            throw new BellmanFordAlgorithmException("Algorytm Bellmana-Forda wymaga grafu ważonego (WeightedGraph).");
        }

        WeightedGraph<V> graph = (WeightedGraph<V>) g;
        BellmanFordResult<V> result = new BellmanFordResult<>();

        Map<Vertex<V>, Double> distances = new HashMap<>();
        Map<Vertex<V>, Vertex<V>> parents = new HashMap<>();

        // Inicjalizacja dystansów
        for (Vertex<V> vertex : graph.getVertices()) {
            distances.put(vertex, vertex.equals(startNode) ? 0.0 : Double.POSITIVE_INFINITY);
        }

        int verticesCount = graph.getSize();

        // Główna pętla relaksacji (V - 1 razy)
        for (int i = 1; i < verticesCount; i++) {

            result.addStep(new BellmanFordResult.BellmanFordStep<>(
                    BellmanFordResult.Phase.START_ITERATION, i, null, null, distances, parents));

            for (Edge<V> e : graph.getEdges()) {
                WeightedEdge<V> edge = (WeightedEdge<V>) e;

                // Relaksacja zgodnie z kierunkiem (lub w obie strony dla nieskierowanych)
                relaxEdge(edge, edge.getSource(), edge.getTarget(), i, distances, parents, result);

                if (!(edge instanceof WeightedDirectedEdge<?>)) {
                    relaxEdge(edge, edge.getTarget(), edge.getSource(), i, distances, parents, result);
                }
            }
        }

        // Sprawdzenie cykli o ujemnej wadze (V-ta iteracja)
        for (Edge<V> e : graph.getEdges()) {
            WeightedEdge<V> edge = (WeightedEdge<V>) e;

            if (hasNegativeCycle(edge, edge.getSource(), edge.getTarget(), distances, parents, result)) {
                throw new BellmanFordAlgorithmException("Wykryto ujemny cykl osiągalny ze źródła!");
            }
            if (!(edge instanceof WeightedDirectedEdge<?>)) {
                if (hasNegativeCycle(edge, edge.getTarget(), edge.getSource(), distances, parents, result)) {
                    throw new BellmanFordAlgorithmException("Wykryto ujemny cykl osiągalny ze źródła!");
                }
            }
        }

        // Zapisanie wyników ostatecznych
        result.getFinalDistances().putAll(distances);
        result.getFinalParents().putAll(parents);

        return result;
    }

    private void relaxEdge(WeightedEdge<V> edge, Vertex<V> source, Vertex<V> target, int iteration,
                           Map<Vertex<V>, Double> distances, Map<Vertex<V>, Vertex<V>> parents, BellmanFordResult<V> result) {

        // Krok: Badanie krawędzi
        result.addStep(new BellmanFordResult.BellmanFordStep<>(
                BellmanFordResult.Phase.CHECK_EDGE, iteration, edge, target, distances, parents));

        if (distances.get(source) != Double.POSITIVE_INFINITY) {
            double newDist = distances.get(source) + edge.getWeight();
            if (newDist < distances.get(target)) {
                distances.put(target, newDist);
                parents.put(target, source);

                // Krok: Udana relaksacja
                result.addStep(new BellmanFordResult.BellmanFordStep<>(
                        BellmanFordResult.Phase.UPDATE_DISTANCE, iteration, edge, target, distances, parents));
            }
        }
    }

    private boolean hasNegativeCycle(WeightedEdge<V> edge, Vertex<V> source, Vertex<V> target,
                                     Map<Vertex<V>, Double> distances, Map<Vertex<V>, Vertex<V>> parents, BellmanFordResult<V> result) {
        if (distances.get(source) != Double.POSITIVE_INFINITY && distances.get(source) + edge.getWeight() < distances.get(target)) {
            // Zapisujemy krok przed rzuceniem wyjątku, by można było to pokazać w animacji!
            result.addStep(new BellmanFordResult.BellmanFordStep<>(
                    BellmanFordResult.Phase.NEGATIVE_CYCLE_FOUND, -1, edge, target, distances, parents));
            return true;
        }
        return false;
    }
}
