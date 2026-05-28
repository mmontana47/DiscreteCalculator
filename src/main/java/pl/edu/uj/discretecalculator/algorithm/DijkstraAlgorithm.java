package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;

import pl.edu.uj.discretecalculator.exception.DijkstraNoPathException;
import pl.edu.uj.discretecalculator.model.graph.*;

public class DijkstraAlgorithm<V> implements AlgorithmicInterface<V,DijkstraAlgorithmResult<V>>{
    private final Vertex<V> path_beg;
    private final Vertex<V> path_end;

    public DijkstraAlgorithm(Vertex<V> beg,Vertex<V> end)
    {
        path_beg=beg;
        path_end=end;
    }

    @Override
    public String algorithmName(){return "Dijkstra Algorithm";}

    @Override
    public DijkstraAlgorithmResult<V> start(Graph<V> g)
    {
        if (!(g instanceof WeightedGraph)) {
            throw new IllegalArgumentException("Algorytm Dijkstry wymaga grafu ważonego (WeightedGraph).");
        }
        DijkstraAlgorithmResult<V> result=new DijkstraAlgorithmResult<>();
        WeightedGraph<V> graph=(WeightedGraph<V>)g;
        Map<Vertex<V>,Double> distance_map=new HashMap<>();
        PriorityQueue<VertexDistance<V>> queue=new PriorityQueue<>(Comparator.comparingDouble(vertex_distance->vertex_distance.distance));

        for(Vertex<V> vertex:graph.getVertices())
        {
            if(vertex.equals(path_beg)) {
                distance_map.put(vertex, Double.POSITIVE_INFINITY);
                result.getDistances().put(vertex, new ArrayList<>());
                result.getDistances().get(vertex).add(Double.POSITIVE_INFINITY);
            }else
            {
                distance_map.put(vertex,0.0);
                result.getDistances().put(vertex, new ArrayList<>());
                result.getDistances().get(vertex).add(0.0);
            }
        }
        queue.add(new VertexDistance<>(path_beg,0.0));
        while (!queue.isEmpty())
        {
            VertexDistance<V> current=queue.poll();
            Vertex<V> current_vertex=current.vertex;
            if(current_vertex.equals(path_end))
                break;
            if(result.getVisited().contains(current_vertex))
                continue;
            result.getVisited().add(current_vertex);
            for(Edge<V> e:graph.getIncidentEdges(current_vertex))
            {
                WeightedEdge<V> edge=(WeightedEdge<V>) e;
                Vertex<V> next_vertex;
                if(edge.getTarget().equals(current_vertex))
                {
                    next_vertex=edge.getSource();
                }else
                {
                    next_vertex=edge.getTarget();
                }
                if(result.getVisited().contains(next_vertex))
                    continue;
                double dist=edge.getWeight();
                double new_dist=distance_map.get(current_vertex)+dist;

                if(new_dist<distance_map.get(next_vertex))
                {
                    distance_map.put(next_vertex,new_dist);
                    result.getDistances().get(next_vertex).add(new_dist);
                    result.getParents().put(next_vertex,current_vertex);
                    queue.add(new VertexDistance<>(next_vertex,new_dist));
                }
            }
        }
        result.setShortest_distance(distance_map.get(path_end));
        if(result.getShortest_distance()!=Double.POSITIVE_INFINITY)
        {
            for(Vertex<V> v=path_end;v!=null;v=result.getParents().get(v))
                result.getShortest_path().add(v);
            Collections.reverse(result.getShortest_path());
        }
        else
            throw new DijkstraNoPathException("No path exists connecting "+path_beg.getValue()+" and "+path_end.getValue());
        return result;
    }

    private static class VertexDistance<V>
    {
        final Vertex<V> vertex;
        final double distance;
        VertexDistance(Vertex<V> vertex,double distance)
        {
            this.vertex=vertex;
            this.distance=distance;
        }
    }
}
