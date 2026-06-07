package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import java.util.*;

public class BellmanFordResult<V> {

    public enum Phase {
        START_ITERATION,      // Rozpoczęcie nowego pełnego przejścia po krawędziach
        CHECK_EDGE,           // Badanie konkretnej krawędzi
        UPDATE_DISTANCE,      // Udana relaksacja krawędzi
        NEGATIVE_CYCLE_FOUND  // Wykryto cykl o ujemnej wadze!
    }

    public static class BellmanFordStep<V> {
        public final Phase phase;
        public final int iteration;
        public final Edge<V> activeEdge;
        public final Vertex<V> activeNode;
        public final Map<Vertex<V>, Double> distancesSnapshot;
        public final Map<Vertex<V>, Vertex<V>> parentsSnapshot;

        //znow przerobiłem na wzorzec krokow - tak jest wygodniej
        public BellmanFordStep(Phase phase, int iteration, Edge<V> activeEdge, Vertex<V> activeNode,
                               Map<Vertex<V>, Double> dist, Map<Vertex<V>, Vertex<V>> par) {
            this.phase = phase;
            this.iteration = iteration;
            this.activeEdge = activeEdge;
            this.activeNode = activeNode;
            this.distancesSnapshot = new HashMap<>(dist);
            this.parentsSnapshot = new HashMap<>(par);
        }
    }

    private final List<BellmanFordStep<V>> history = new ArrayList<>();
    private final Map<Vertex<V>, Double> finalDistances = new HashMap<>();
    private final Map<Vertex<V>, Vertex<V>> finalParents = new HashMap<>();

    public void addStep(BellmanFordStep<V> step) {
        history.add(step);
    }

    public List<BellmanFordStep<V>> getHistory() { return history; }
    public Map<Vertex<V>, Double> getFinalDistances() { return finalDistances; }
    public Map<Vertex<V>, Vertex<V>> getFinalParents() { return finalParents; }
}