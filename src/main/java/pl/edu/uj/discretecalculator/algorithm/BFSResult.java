package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Vertex;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import java.util.*;

public class BFSResult<V> {
    private final Vertex<V> first;
    private final Map<Vertex<V>, Vertex<V>> parentMap;
    private final List<Vertex<V>> visitOrder;
    private final Set<Edge<V>> treeEdges;
    private final Set<Edge<V>> nonTreeEdges;
    private final Map<Vertex<V>, Integer> componentMap;

    public BFSResult(Vertex<V> first, Map<Vertex<V>, Vertex<V>> parentMap, List<Vertex<V>> visitOrder, Set<Edge<V>> treeEdges, Set<Edge<V>> nonTreeEdges, Map<Vertex<V>, Integer> componentMap) {
        this.first = first;
        this.parentMap = parentMap;
        this.visitOrder = visitOrder;
        this.treeEdges = treeEdges;
        this.nonTreeEdges = nonTreeEdges;
        this.componentMap = componentMap;
    }

    public List<Vertex<V>> getPathTo(Vertex<V> target) {
        if (!parentMap.containsKey(target) && !target.equals(first)) {
            return Collections.emptyList();
        }

        List<Vertex<V>> path = new ArrayList<>();
        Vertex<V> current = target;

        while (current != null) {
            path.add(current);
            current = parentMap.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    public List<Vertex<V>> getVisitOrder() {
        return visitOrder;
    }

    public Map<Vertex<V>, Vertex<V>> getParentMap() {
        return parentMap;
    }
    public Set<Edge<V>> getTreeEdges() {
        return treeEdges;
    }

    public Set<Edge<V>> getNonTreeEdges() {
        return nonTreeEdges;
    }

    public Map<Vertex<V>, Integer> getComponentMap() {
        return componentMap;
    }
}