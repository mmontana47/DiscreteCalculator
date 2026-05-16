package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import java.util.*;

public class DFS<V,E> implements AlgorithmicInterface<V,E>{
    private final Deque<Vertex<V>> order=new ArrayDeque<>();
    @Override
    public String algorithmName()
    {
        return "Depth-First Search (DFS)";
    }
    @Override
    public void start(Graph<V,E> graph,Vertex<V> first)
    {
        Set<Vertex<V>> visited=new HashSet<>();
        dfs(graph,first,visited,order);
    }

    private void dfs(Graph<V,E> graph,Vertex<V> node,Set<Vertex<V>> visited,Deque<Vertex<V>> order)
    {
        visited.add(node);
        order.addLast(node);
        for(Vertex<V> neighbor:graph.getNeighbors(node))
        {
            if(!visited.contains(neighbor))
            {
                dfs(graph,neighbor,visited,order);
            }
        }
    }
}
