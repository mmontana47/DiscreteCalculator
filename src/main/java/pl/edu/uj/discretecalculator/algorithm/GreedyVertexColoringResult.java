package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Vertex;
import java.util.*;

public class GreedyVertexColoringResult<V> {

    public enum Phase {
        NODE_COLORED
    }

    //znow przerobilem na kroki - m. in. dla spojnosci architektury, i tak trzeba bylo pod backtracking
    public static class ColoringStep<V> {
        public final Phase phase;
        public final Vertex<V> activeNode;
        public final int assignedColor;
        public final Map<Vertex<V>, Integer> colorsSnapshot;

        public ColoringStep(Phase phase, Vertex<V> activeNode, int assignedColor, Map<Vertex<V>, Integer> colors) {
            this.phase = phase;
            this.activeNode = activeNode;
            this.assignedColor = assignedColor;
            // Robimy twardą kopię mapy (Snapshot), aby w trybie "Krok w tył" kolory nie znikały
            this.colorsSnapshot = new HashMap<>(colors);
        }
    }

    private final List<ColoringStep<V>> history = new ArrayList<>();
    private final Map<Vertex<V>, Integer> finalColors = new HashMap<>();
    private int chromaticUpperBound = 0;

    public void addStep(ColoringStep<V> step) { history.add(step); }
    public List<ColoringStep<V>> getHistory() { return history; }
    public Map<Vertex<V>, Integer> getFinalColors() { return finalColors; }

    public int getChromaticUpperBound() { return chromaticUpperBound; }
    public void setChromaticUpperBound(int bound) { this.chromaticUpperBound = bound; }
}