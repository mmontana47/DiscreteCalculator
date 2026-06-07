package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import java.util.*;

public class DijkstraAlgorithmResult<V> {

    public enum Phase {
        VISIT_NODE,
        CHECK_EDGE,      // (podświetlamy krawędź)
        UPDATE_DISTANCE
    }

    public static class DijkstraStep<V> {
        public final Phase phase;
        public final Vertex<V> activeNode;
        public final Edge<V> activeEdge; // Może być null
        public final Map<Vertex<V>, Double> distancesSnapshot;
        public final Map<Vertex<V>, Vertex<V>> parentsSnapshot;

        //WAZNE: zmienilem na mechanizm snapshotow - latwiej mi idzie wtedy logika w trackfactory. analogicznie dla wszystkich innych algorytmow
        //Wczesniej trzymalismy mapę wierzchołków odwiedzonych i list dystansów - trudniej było z tego odtworzyć chronologię
        public DijkstraStep(Phase phase, Vertex<V> activeNode, Edge<V> activeEdge,
                            Map<Vertex<V>, Double> dist, Map<Vertex<V>, Vertex<V>> par) {
            this.phase = phase;
            this.activeNode = activeNode;
            this.activeEdge = activeEdge;
            this.distancesSnapshot = new HashMap<>(dist);
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