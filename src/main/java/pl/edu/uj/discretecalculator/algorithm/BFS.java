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
    }
}
