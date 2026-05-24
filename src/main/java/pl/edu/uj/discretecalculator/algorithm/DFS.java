package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.*;

public class DFS<V> implements AlgorithmicInterface<V, DFSResult<V>>{
    private final Vertex<V> first;

    public DFS(Vertex<V> first) {
        this.first = first;
    }

    @Override
    public String algorithmName()
    {
        return "Depth-First Search (DFS)";
    }
    @Override
    public DFSResult<V> start(Graph<V> graph)
    {
        List<Vertex<V>> visitOrder = new ArrayList<>();
        Map<Vertex<V>, Vertex<V>> parentMap = new HashMap<>();
        Set<Edge<V>> treeEdges = new HashSet<>();
        Set<Edge<V>> nonTreeEdges = new HashSet<>();
        Set<Vertex<V>> visited = new HashSet<>();

        parentMap.put(first, null);
        dfs(graph, first, visited, parentMap, visitOrder, treeEdges, nonTreeEdges);

        for (Vertex<V> vertex : graph.getVertices()) {
            if (!visited.contains(vertex)) {
                //nowa składowa
                parentMap.put(vertex, null);

                // Odpalamy na niej zwykłą logikę
                dfs(graph, vertex, visited, parentMap, visitOrder, treeEdges, nonTreeEdges);
            }
        }

        return new DFSResult<>(first, parentMap, visitOrder, treeEdges, nonTreeEdges);
    }

    private void dfs(Graph<V> graph,Vertex<V> node,
                     Set<Vertex<V>> visited, Map<Vertex<V>,
                    Vertex<V>> parentMap,
                     List<Vertex<V>> visitOrder,
                     Set<Edge<V>> treeEdges,
                     Set<Edge<V>> nonTreeEdges)
    {
        visited.add(node);
        visitOrder.addLast(node);
        for(Edge<V> edge:graph.getIncidentEdges(node))
        {
            Vertex<V> neighbor = edge.getSource().equals(node) ? edge.getTarget() : edge.getSource();
            if(!visited.contains(neighbor))
            {
                parentMap.put(neighbor, node);
                treeEdges.add(edge);
                dfs(graph,neighbor,visited, parentMap, visitOrder, treeEdges, nonTreeEdges);
            }
            else if (!treeEdges.contains(edge))
            {
                nonTreeEdges.add(edge);
            }
        }
    }
}
