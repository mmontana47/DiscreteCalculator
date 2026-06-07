package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Edge;
import java.util.*;

public class GreedyEdgeColoringResult<V> {

    public enum Phase {
        EDGE_COLORED
    }

    public static class EdgeColoringStep<V> {
        public final Phase phase;
        public final Edge<V> activeEdge;
        public final int assignedColor;
        public final Map<Edge<V>, Integer> colorsSnapshot;

        //znow przerobilem na kroki - m. in. dla spojnosci architektury, i tak trzeba bylo pod backtracking
        public EdgeColoringStep(Phase phase, Edge<V> activeEdge, int assignedColor, Map<Edge<V>, Integer> colors) {
            this.phase = phase;
            this.activeEdge = activeEdge;
            this.assignedColor = assignedColor;
            //twarda kopia do historii
            this.colorsSnapshot = new HashMap<>(colors);
        }
    }

    private final List<EdgeColoringStep<V>> history = new ArrayList<>();
    private final Map<Edge<V>, Integer> finalColors = new HashMap<>();
    private int chromaticIndexUpperBound = 0;

    public void addStep(EdgeColoringStep<V> step) { history.add(step); }
    public List<EdgeColoringStep<V>> getHistory() { return history; }
    public Map<Edge<V>, Integer> getFinalColors() { return finalColors; }

    public int getChromaticIndexUpperBound() { return chromaticIndexUpperBound; }
    public void setChromaticIndexUpperBound(int bound) { this.chromaticIndexUpperBound = bound; }
}