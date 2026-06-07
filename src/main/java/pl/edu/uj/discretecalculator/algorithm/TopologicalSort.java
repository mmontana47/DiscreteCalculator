package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;
import pl.edu.uj.discretecalculator.exception.TopologicalSortException;
import pl.edu.uj.discretecalculator.model.graph.*;

public class TopologicalSort<V> implements AlgorithmicInterface<V, TopoSortResult<V>> {

    @Override
    public String algorithmName() { return "Algorytm Kahna (Sortowanie Topologiczne)"; }

    @Override
    public TopoSortResult<V> start(Graph<V> graph) {

        // rozwiazanie problemu z dziedziczeniem
        boolean isDirected = (graph instanceof DirectedGraph<?>) ||
                graph.getClass().getSimpleName().contains("Directed");

        if (!isDirected) {
            throw new TopologicalSortException("Sortowanie topologiczne wymaga grafu skierowanego.");
        }

        TopoSortResult<V> result = new TopoSortResult<>();
        Map<Vertex<V>, Integer> inDegrees = new HashMap<>();

        for (Vertex<V> v : graph.getVertices()) {
            inDegrees.put(v, 0);
        }

        for (Edge<V> e : graph.getEdges()) {
            Vertex<V> target = e.getTarget();
            inDegrees.put(target, inDegrees.get(target) + 1);
        }

        Queue<Vertex<V>> queue = new LinkedList<>();
        for (Map.Entry<Vertex<V>, Integer> entry : inDegrees.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        result.addStep(new TopoSortResult.TopoStep<>(TopoSortResult.Phase.INIT, null, null, inDegrees, result.getSortedResult()));

        int processedCount = 0;

        while (!queue.isEmpty()) {
            Vertex<V> current = queue.poll();
            result.getSortedResult().add(current);
            processedCount++;

            result.addStep(new TopoSortResult.TopoStep<>(TopoSortResult.Phase.TAKE_NODE, current, null, inDegrees, result.getSortedResult()));

            for (Edge<V> e : graph.getIncidentEdges(current)) {
                if (e.getSource().equals(current)) {
                    Vertex<V> neighbor = e.getTarget();

                    inDegrees.put(neighbor, inDegrees.get(neighbor) - 1);

                    result.addStep(new TopoSortResult.TopoStep<>(TopoSortResult.Phase.UPDATE_DEGREES, neighbor, e, inDegrees, result.getSortedResult()));

                    if (inDegrees.get(neighbor) == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        // detekcja cyklu
        if (processedCount != graph.getVertices().size()) {
            result.addStep(new TopoSortResult.TopoStep<>(TopoSortResult.Phase.CYCLE_DETECTED, null, null, inDegrees, result.getSortedResult()));
            throw new TopologicalSortException("Graf zawiera cykl");
        }

        return result;
    }
}