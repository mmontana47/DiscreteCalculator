package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Edge;
import java.util.*;

public class BacktrackingAlgorithmForEdgesResult<V> {

    public enum Phase {
        TRY_COLOR,
        BACKTRACK  // slepy zaulek (na razie zmiana koloru)
    }

    public static class BacktrackStep<V> {
        public final Phase phase;
        public final Edge<V> activeEdge;
        public final int color;
        public final int maxColorsAllowed;
        public final Map<Edge<V>, Integer> colorsSnapshot;

        public BacktrackStep(Phase phase, Edge<V> activeEdge, int color, int maxColorsAllowed, Map<Edge<V>, Integer> colors) {
            this.phase = phase;
            this.activeEdge = activeEdge;
            this.color = color;
            this.maxColorsAllowed = maxColorsAllowed;
            this.colorsSnapshot = new HashMap<>(colors); // twardy snapshot
        }
    }

    private final List<BacktrackStep<V>> history = new ArrayList<>();
    private final Map<Edge<V>, Integer> finalColors = new HashMap<>();

    public void addStep(BacktrackStep<V> step) { history.add(step); }
    public List<BacktrackStep<V>> getHistory() { return history; }
    public void clearHistory() { history.clear(); } // być może gdybyśmy chceli jednak pomijać nieudane próby, to czysci historie
    public Map<Edge<V>, Integer> getFinalColors() { return finalColors; }
}