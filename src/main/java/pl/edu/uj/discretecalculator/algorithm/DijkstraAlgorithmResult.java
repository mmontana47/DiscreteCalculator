package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import java.util.*;

public class DijkstraAlgorithmResult<V> {

    // Fazy, które chcemy animować w interfejsie
    public enum Phase {
        VISIT_NODE,      // Pobranie wierzchołka z kolejki (np. kolorujemy go na żółto)
        CHECK_EDGE,      // Badanie krawędzi (np. podświetlamy krawędź)
        UPDATE_DISTANCE  // Aktualizacja dystansu (np. odświeżamy tabelę i kolor węzła)
    }

    public static class DijkstraStep<V> {
        public final Phase phase;
        public final Vertex<V> activeNode;
        public final Edge<V> activeEdge; // Może być null
        public final Map<Vertex<V>, Double> distancesSnapshot; // Zdjęcie tabeli dystansów w danym momencie
        public final Map<Vertex<V>, Vertex<V>> parentsSnapshot;

        public DijkstraStep(Phase phase, Vertex<V> activeNode, Edge<V> activeEdge,
                            Map<Vertex<V>, Double> dist, Map<Vertex<V>, Vertex<V>> par) {
            this.phase = phase;
            this.activeNode = activeNode;
            this.activeEdge = activeEdge;
            this.distancesSnapshot = new HashMap<>(dist); // Robimy twardą kopię (Snapshot)
            this.parentsSnapshot = new HashMap<>(par);
        }
    }

    private final List<DijkstraStep<V>> history = new ArrayList<>();
    private final Map<Vertex<V>, Double> finalDistances = new HashMap<>();
    private final Map<Vertex<V>, Vertex<V>> finalParents = new HashMap<>();

    public void addStep(DijkstraStep<V> step) {
        history.add(step);
    }

    public List<DijkstraStep<V>> getHistory() { return history; }
    public Map<Vertex<V>, Double> getFinalDistances() { return finalDistances; }
    public Map<Vertex<V>, Vertex<V>> getFinalParents() { return finalParents; }
}