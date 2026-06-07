package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;
import pl.edu.uj.discretecalculator.model.graph.*;

public class TopoSortResult<V> {

    public enum Phase {
        INIT,
        TAKE_NODE,
        UPDATE_DEGREES,
        CYCLE_DETECTED
    }

    public static class TopoStep<V> {
        public final Phase phase;
        public final Vertex<V> activeNode;
        public final Edge<V> activeEdge;
        public final Map<Vertex<V>, Integer> inDegreesSnapshot;
        public final List<Vertex<V>> sortedSoFar;

        public TopoStep(Phase phase, Vertex<V> activeNode, Edge<V> activeEdge,
                        Map<Vertex<V>, Integer> inDegrees, List<Vertex<V>> sorted) {
            this.phase = phase;
            this.activeNode = activeNode;
            this.activeEdge = activeEdge;
            this.inDegreesSnapshot = new HashMap<>(inDegrees);
            this.sortedSoFar = new ArrayList<>(sorted);
        }
    }

    private final List<TopoStep<V>> history = new ArrayList<>();
    private final List<Vertex<V>> sortedResult = new ArrayList<>();

    public void addStep(TopoStep<V> step) { history.add(step); }
    public List<TopoStep<V>> getHistory() { return history; }
    public List<Vertex<V>> getSortedResult() { return sortedResult; }
}