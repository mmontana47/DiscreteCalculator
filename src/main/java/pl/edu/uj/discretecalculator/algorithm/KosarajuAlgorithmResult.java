package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;

public class KosarajuAlgorithmResult<V> {

    public enum Phase {
        DFS1_VISIT,
        DFS1_CHECK_EDGE,
        DFS1_PUSH,
        DFS2_POP,
        DFS2_VISIT,
        DFS2_CHECK_EDGE,
        SCC_FOUND
    }

    public static class KosarajuStep<V> {
        public final Phase phase;
        public final Vertex<V> activeNode;
        public final Edge<V> activeEdge; // może być null

        public final Set<Vertex<V>> visitedSnapshot;
        public final List<Vertex<V>> stackSnapshot;
        public final Map<Integer, List<Vertex<V>>> sccSnapshot;

        public KosarajuStep(Phase phase, Vertex<V> activeNode, Edge<V> activeEdge,
                            Set<Vertex<V>> visited, Collection<Vertex<V>> stack,
                            Map<Integer, List<Vertex<V>>> scc) {
            this.phase = phase;
            this.activeNode = activeNode;
            this.activeEdge = activeEdge;
            this.visitedSnapshot = new HashSet<>(visited);
            this.stackSnapshot = new ArrayList<>(stack);

            this.sccSnapshot = new HashMap<>();
            for (Map.Entry<Integer, List<Vertex<V>>> entry : scc.entrySet()) {
                this.sccSnapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
    }

    private final List<KosarajuStep<V>> history = new ArrayList<>();
    private final Map<Integer, List<Vertex<V>>> stronglyConnectedComponents = new HashMap<>();

    public void addStep(KosarajuStep<V> step) {
        history.add(step);
    }

    public List<KosarajuStep<V>> getHistory() {
        return history;
    }

    public Map<Integer, List<Vertex<V>>> getStronglyConnectedComponents() {
        return stronglyConnectedComponents;
    }
}