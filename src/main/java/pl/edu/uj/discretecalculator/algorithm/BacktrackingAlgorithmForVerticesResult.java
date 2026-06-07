package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Vertex;
import java.util.*;

public class BacktrackingAlgorithmForVerticesResult<V> {

    public enum Phase {
        TRY_COLOR,
        BACKTRACK
    }

    public static class BacktrackVertexStep<V> {
        public final Phase phase;
        public final Vertex<V> activeNode;
        public final int color;
        public final int maxColorsAllowed;
        public final Map<Vertex<V>, Integer> colorsSnapshot;

        public BacktrackVertexStep(Phase phase, Vertex<V> activeNode, int color, int maxColorsAllowed, Map<Vertex<V>, Integer> colors) {
            this.phase = phase;
            this.activeNode = activeNode;
            this.color = color;
            this.maxColorsAllowed = maxColorsAllowed;
            this.colorsSnapshot = new HashMap<>(colors);
        }
    }

    private final List<BacktrackVertexStep<V>> history = new ArrayList<>();
    private final Map<Vertex<V>, Integer> finalColors = new HashMap<>();

    public void addStep(BacktrackVertexStep<V> step) { history.add(step); }
    public List<BacktrackVertexStep<V>> getHistory() { return history; }
    public void clearHistory() { history.clear(); }
    public Map<Vertex<V>, Integer> getFinalColors() { return finalColors; }
}