package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;

public class KosarajuAlgorithm<V> implements AlgorithmicInterface<V,KosarajuAlgorithmResult<V>>{
    private final DirectedGraph<V> graph;
    private final DirectedGraph<V> EdgeReverseGraph;

    public KosarajuAlgorithm(DirectedGraph<V> graph)
    {
        this.graph=graph;
        this.EdgeReverseGraph=Reverse(graph);
    }


    @Override
    public String algorithmName() {
        return "Kosaraju Algorithm (Strongly Connected Components)";
    }

    @Override
    public KosarajuAlgorithmResult<V> start(Graph<V> graph) {
        KosarajuAlgorithmResult<V> result=new KosarajuAlgorithmResult<>();
        if (!(graph instanceof DirectedGraph)) {
            throw new IllegalArgumentException("Kosaraju algorithm requires DirectedGraph.");
        }

        Stack<Vertex<V>> stack = new Stack<>();
        Set<Vertex<V>> visited = new HashSet<>();
        int index=0;

        for (Vertex<V> vertex : graph.getVertices()) {
            if (!visited.contains(vertex)) {
                result.getCompoentsAfterDFS1().add(index,new ArrayList<>());
                dfs1(this.graph, vertex, visited, stack,result,index);
                index++;
            }
        }
        visited.clear();
        Map<Vertex<V>, List<Edge<V>>> transposedAdjList=this.EdgeReverseGraph.getAdjacencyList();
        while (!stack.isEmpty()) {
            Vertex<V> vertex = stack.pop();

            if (!visited.contains(vertex)) {
                ArrayList<Vertex<V>> scc = new ArrayList<>();
                dfs2(transposedAdjList,vertex, visited, scc);
                result.getStronglyConnectedComponents().add(scc);
            }
        }
        return result;
    }

    private void dfs1(DirectedGraph<V> graph, Vertex<V> vertex, Set<Vertex<V>> visited, Stack<Vertex<V>> stack,KosarajuAlgorithmResult<V> result,int index) {
        visited.add(vertex);
        result.getCompoentsAfterDFS1().get(index).add(vertex);
        for (Edge<V> edge : graph.getIncidentEdges(vertex)) {
            Vertex<V> neighbor = edge.getTarget();
            if (!visited.contains(neighbor)) {
                dfs1(graph, neighbor, visited, stack,result,index);
            }
        }
        stack.push(vertex);
    }

    private void dfs2(Map<Vertex<V>, List<Edge<V>>> transposedAdjList, Vertex<V> vertex, Set<Vertex<V>> visited, List<Vertex<V>> scc) {

        visited.add(vertex);
        scc.add(vertex);

        for (Edge<V> neighbor : transposedAdjList.getOrDefault(vertex, Collections.emptyList())) {
            Vertex<V> neighbor1=neighbor.getTarget();
            if (!visited.contains(neighbor1)) {
                dfs2(transposedAdjList,neighbor1, visited, scc);
            }
        }
    }

    private DirectedGraph<V> Reverse(DirectedGraph<V> graph) {
        DirectedGraph<V> reverse_graph=new DirectedGraph<>("reversed graph");
        for(Edge<V> e:graph.getEdges())
        {
            DirectedEdge<V> edge=(DirectedEdge<V>)e;
            DirectedEdge<V> newedge=new DirectedEdge<>(edge.getTarget(),edge.getSource(),edge.getId());
            reverse_graph.addEdge(newedge);
        }
        return reverse_graph;
    }
}
