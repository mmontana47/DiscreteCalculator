package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.*;

public class BFS<V> implements AlgorithmicInterface<V, BFSResult<V>> {

    private final Vertex<V> first;

    public BFS(Vertex<V> first) {
        this.first = first;
    }
    @Override
    public  String algorithmName()
    {
        return "Breadth-First Search (BFS)";
    }
    @Override
    public BFSResult<V> start(Graph<V> graph)
    {
        List<Vertex<V>> visitOrder = new ArrayList<>();
        Set<Vertex<V>> visited=new HashSet<>();
        Queue<Vertex<V>>queue=new LinkedList<>();
        Set<Edge<V>> treeEdges = new HashSet<>();
        Set<Edge<V>> nonTreeEdges = new HashSet<>();

        Map<Vertex<V>, Vertex<V>> parentMap = new HashMap<>();

        visited.add(first);
        queue.add(first);
        parentMap.put(first, null);

        bfs(graph, first, visited, queue, parentMap, visitOrder, treeEdges, nonTreeEdges);

        for (Vertex<V> vertex : graph.getVertices()) {
            if (!visited.contains(vertex)) {
                parentMap.put(vertex, null);
                bfs(graph, vertex, visited, queue, parentMap, visitOrder, treeEdges, nonTreeEdges);
            }
        }

        return new BFSResult<>(first, parentMap, visitOrder, treeEdges, nonTreeEdges);
    }

    private void bfs(Graph<V> graph,
                          Vertex<V> startNode,
                          Set<Vertex<V>> visited,
                          Queue<Vertex<V>> queue,
                          Map<Vertex<V>, Vertex<V>> parentMap,
                          List<Vertex<V>> visitOrder,
                          Set<Edge<V>> treeEdges,
                          Set<Edge<V>> nonTreeEdges) {

        visited.add(startNode);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Vertex<V> current = queue.poll();
            visitOrder.add(current);

            for (Edge<V> edge : graph.getIncidentEdges(current)) {
                Vertex<V> neighbor = edge.getSource().equals(current) ? edge.getTarget() : edge.getSource();

                if (!visited.contains(neighbor))
                {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    treeEdges.add(edge);
                    queue.add(neighbor);
                }
                else if (!treeEdges.contains(edge))
                {
                    nonTreeEdges.add(edge);
                }
            }
        }
    }
}
