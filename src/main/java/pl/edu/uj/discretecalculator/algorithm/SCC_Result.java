package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;
import pl.edu.uj.discretecalculator.model.graph.*;

public class SCC_Result<V> {

    public enum Phase {
        VISIT_NODE,          // dołożenie na stos
        CHECK_EDGE,          // badanie krawedzi
        UPDATE_LOW_LINK,     // aktualizacja low
        COMPONENT_FOUND      // zdejmowanie ze stosu
    }

    public static class TarjanStep<V> {
        public final Phase phase;
        public final Vertex<V> activeNode;
        public final Edge<V> activeEdge; // może być null

        public final Map<Vertex<V>, Integer> indicesSnapshot;
        public final Map<Vertex<V>, Integer> lowLinkSnapshot;
        public final List<Vertex<V>> currentStackSnapshot; // stos w danym kroku
        public final Map<Integer, List<Vertex<V>>> sccSnapshot; //znalezione składowe

        //znow pomocniczo step
        public TarjanStep(Phase phase, Vertex<V> activeNode, Edge<V> activeEdge,
                          Map<Vertex<V>, Integer> indices, Map<Vertex<V>, Integer> lowLink,
                          Deque<Vertex<V>> stack, Map<Integer, List<Vertex<V>>> scc) {
            this.phase = phase;
            this.activeNode = activeNode;
            this.activeEdge = activeEdge;
            this.indicesSnapshot = new HashMap<>(indices);
            this.lowLinkSnapshot = new HashMap<>(lowLink);
            this.currentStackSnapshot = new ArrayList<>(stack);

            this.sccSnapshot = new HashMap<>();
            for (Map.Entry<Integer, List<Vertex<V>>> entry : scc.entrySet()) {
                this.sccSnapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
    }

    private final List<TarjanStep<V>> history = new ArrayList<>();
    private final Map<Integer, List<Vertex<V>>> finalComponents = new HashMap<>();

    public void addStep(TarjanStep<V> step) {
        history.add(step);
    }

    public List<TarjanStep<V>> getHistory() { return history; }
    public Map<Integer, List<Vertex<V>>> getFinalComponents() { return finalComponents; }
}