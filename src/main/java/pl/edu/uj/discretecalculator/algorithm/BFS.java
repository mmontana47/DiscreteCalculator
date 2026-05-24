package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.exception.*;

import java.util.*;
//BFS implementation
public class BFS<V,E> implements AlgorithmicInterface<V,E> {

    @Override
    public  String algorithmName()
    {
        return "Breadth-First Search (BFS)";
    }
    @Override
    public void start(Graph<V,E> graph,Vertex<V> first)
    {
        Set<Vertex<V>> visited=new HashSet<>();
        Queue<Pair<V>>queue=new LinkedList<>();
        Pair<V> start=new Pair<>(first,first);
        visited.add(first);
        queue.add(start);

        while (!queue.isEmpty())
        {
            Vertex<V>current=queue.poll().getSecond();

            for(Vertex<V> neighbor:graph.getNeighbors(current))
            {
                if(!visited.contains(neighbor))
                {
                    Pair<V> next=new Pair<>(current,neighbor);
                    visited.add(neighbor);
                    queue.add(next);
                }
            }
        }
        if(visited.size()!=graph.getVertices().size())
            throw new GraphNotConnectedException("Graph "+graph.getName()+" is not connected.");
    }
}
