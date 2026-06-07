package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;

public class StronglyConnectedComponent<V> implements AlgorithmicInterface<V, SCC_Result<V>> {

    private int index;
    private Map<Vertex<V>, Integer> indices;
    private Map<Vertex<V>, Integer> low;
    private Deque<Vertex<V>> stack;
    private Set<Vertex<V>> onStack;
    private Integer component_idx;
    private SCC_Result<V> result;
    private Map<Integer, List<Vertex<V>>> stronglyConnectedComponentsMap;
    private boolean isDirected;

    @Override
    public String algorithmName() {
        return "Tarjan's SCC Algorithm";
    }

    @Override
    public SCC_Result<V> start(Graph<V> graph) {
        // czyscimy stan, żeby nie był nadpisany przez wczesniejsze uruchomienia
        component_idx = 0;
        index = 0;
        indices = new HashMap<>();
        low = new HashMap<>();
        stack = new ArrayDeque<>();
        onStack = new HashSet<>();
        stronglyConnectedComponentsMap = new HashMap<>();
        result = new SCC_Result<>();

        //rozwiazanie klopotu z dziedziczeniem (lub jego brakiem)
        this.isDirected = (graph instanceof DirectedGraph<?>) ||
                graph.getClass().getSimpleName().contains("Directed");

        for (Vertex<V> v : graph.getVertices()) {
            if (!indices.containsKey(v)) {
                strongConnect(v, graph);
            }
        }

        result.getFinalComponents().putAll(stronglyConnectedComponentsMap);
        return result;
    }

    private void strongConnect(Vertex<V> v, Graph<V> graph) {
        indices.put(v, index);
        low.put(v, index);
        index++;

        stack.push(v);
        onStack.add(v);

        result.addStep(new SCC_Result.TarjanStep<>(SCC_Result.Phase.VISIT_NODE, v, null, indices, low, stack, stronglyConnectedComponentsMap));

        for (Edge<V> edge : graph.getIncidentEdges(v)) {
            if (isDirected && !edge.getSource().equals(v)) continue;

            Vertex<V> w = edge.getTarget().equals(v) ? edge.getSource() : edge.getTarget();

            result.addStep(new SCC_Result.TarjanStep<>(SCC_Result.Phase.CHECK_EDGE, w, edge, indices, low, stack, stronglyConnectedComponentsMap));

            if (!indices.containsKey(w)) {
                strongConnect(w, graph);

                int oldLow = low.get(v);
                int newLow = Math.min(low.get(v), low.get(w));
                low.put(v, newLow);

                if (oldLow != newLow) {
                    //update low
                    result.addStep(new SCC_Result.TarjanStep<>(SCC_Result.Phase.UPDATE_LOW_LINK, v, edge, indices, low, stack, stronglyConnectedComponentsMap));
                }

            } else if (onStack.contains(w)) {
                int oldLow = low.get(v);
                int newLow = Math.min(low.get(v), indices.get(w));
                low.put(v, newLow);

                if (oldLow != newLow) {
                    // update low
                    result.addStep(new SCC_Result.TarjanStep<>(SCC_Result.Phase.UPDATE_LOW_LINK, v, edge, indices, low, stack, stronglyConnectedComponentsMap));
                }
            }
        }

        if (low.get(v).equals(indices.get(v))) {
            stronglyConnectedComponentsMap.put(component_idx, new ArrayList<>());

            Vertex<V> w;
            do {
                w = stack.pop();
                onStack.remove(w);
                stronglyConnectedComponentsMap.get(component_idx).add(w);
            } while (!w.equals(v));

            // znaleziono skladową
            result.addStep(new SCC_Result.TarjanStep<>(SCC_Result.Phase.COMPONENT_FOUND, v, null, indices, low, stack, stronglyConnectedComponentsMap));

            component_idx++;
        }
    }
}