package pl.edu.uj.discretecalculator.model.graph;

import java.util.*;

public class Graph<V> {

    private final Map<Vertex<V>, List<Edge<V>>> adjacencyList = new LinkedHashMap<>();

    private final String name;
    private boolean isDirected = false;
    private boolean colouredVertices = false;
    private boolean colouredEdges = false;

    public Graph(String name) {
        this.name = name;
    }

    public Graph(String name, boolean isDirected, boolean colouredVertices, boolean colouredEdges) {
        this.name = name;
        this.isDirected = isDirected;
        this.colouredVertices = colouredVertices;
        this.colouredEdges = colouredEdges;
    }

    public boolean addVertex(Vertex<V> vertex) {
        if (adjacencyList.containsKey(vertex)) {
            return false;
        }
        adjacencyList.put(vertex, new ArrayList<>());
        return true;
    }

    public void addEdge(Edge<V> edge) {
        addVertex(edge.getSource());
        addVertex(edge.getTarget());

        adjacencyList.get(edge.getSource()).add(edge);

        if (!isDirected) {
            adjacencyList.get(edge.getTarget()).add(edge);
        }
    }

    public boolean deleteVertex(Vertex<V> vertex) {
        if (!adjacencyList.containsKey(vertex)) return false;

        adjacencyList.remove(vertex);

        for (List<Edge<V>> edges : adjacencyList.values()) {
            edges.removeIf(edge -> edge.getTarget().equals(vertex) || edge.getSource().equals(vertex));
        }
        return true;
    }

    public boolean deleteEdge(Edge<V> edge) {
        boolean removed = false;
        if (adjacencyList.containsKey(edge.getSource())) {
            removed = adjacencyList.get(edge.getSource()).remove(edge);
        }
        if (!isDirected && adjacencyList.containsKey(edge.getTarget())) {
            removed = adjacencyList.get(edge.getTarget()).remove(edge) || removed;
        }
        return removed;
    }

    public List<Vertex<V>> getVertices() {
        return new ArrayList<>(adjacencyList.keySet());
    }

    public List<Edge<V>> getIncidentEdges(Vertex<V> vertex) {
        return adjacencyList.getOrDefault(vertex, new ArrayList<>());
    }

    public List<Vertex<V>> getNeighbors(Vertex<V> vertex) {
        List<Vertex<V>> neighbors = new ArrayList<>();
        for (Edge<V> edge : getIncidentEdges(vertex)) {
            Vertex<V> neighbor = edge.getSource().equals(vertex) ? edge.getTarget() : edge.getSource();
            neighbors.add(neighbor);
        }
        return neighbors;
    }

    public int getSize() {
        return adjacencyList.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Graph<?> graph = (Graph<?>) o;
        if (this.getSize() != graph.getSize() ||
                isDirected != graph.isDirected ||
                colouredVertices != graph.colouredVertices ||
                colouredEdges != graph.colouredEdges ||
                !Objects.equals(name, graph.name)) {
            return false;
        }
        if (!adjacencyList.keySet().equals(graph.adjacencyList.keySet())) {
            return false;
        }
        for (Map.Entry<Vertex<V>, List<Edge<V>>> entry : adjacencyList.entrySet()) {
            Vertex<V> currentVertex = entry.getKey();
            List<Edge<V>> thisEdges = entry.getValue();
            List<?> otherEdges = graph.adjacencyList.get(currentVertex);
            if (otherEdges == null || thisEdges.size() != otherEdges.size()) {
                return false;
            }
            if (!new HashSet<>(thisEdges).equals(new HashSet<>(otherEdges))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, this.getSize(), isDirected, colouredVertices, colouredEdges);
        int edgesHash = 0;
        for (Map.Entry<Vertex<V>, List<Edge<V>>> entry : adjacencyList.entrySet()) {
            int keyHash = entry.getKey() != null ? entry.getKey().hashCode() : 0;
            int localEdgesHash = 0;
            for (Edge<V> edge : entry.getValue()) {
                localEdgesHash += (edge != null ? edge.hashCode() : 0);
            }
            edgesHash += (keyHash ^ localEdgesHash);
        }
        return 31 * result + edgesHash;
    }
}