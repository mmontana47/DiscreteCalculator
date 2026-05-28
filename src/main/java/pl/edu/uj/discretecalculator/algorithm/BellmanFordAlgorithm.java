package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;


import pl.edu.uj.discretecalculator.exception.BellmanFordAlgorithmException;
import pl.edu.uj.discretecalculator.model.graph.*;

public class BellmanFordAlgorithm<V> implements AlgorithmicInterface<V,BellmanFordResult<V>>{
    private BellmanFordResult<V> result;
    private final Vertex<V> start;
    private final Vertex<V> end;
    @Override
    public String algorithmName(){return "Bellman Ford Algorithm";}

    public BellmanFordAlgorithm(Vertex<V> start,Vertex<V> end)
    {
        this.start=start;
        this.end=end;
        result=new BellmanFordResult<>();
    }

    @Override
    public BellmanFordResult<V> start(Graph<V> g)
    {
        if(!(g instanceof WeightedGraph))
            throw new BellmanFordAlgorithmException("The graph must be directed.");
        WeightedGraph<V> graph=(WeightedGraph<V>) g;
        for(Vertex<V> vertex:graph.getVertices())
        {
            if(!vertex.equals(start)) {
                result.getDistance().put(vertex, Double.POSITIVE_INFINITY);
                result.getDistanceChange().put(vertex, new ArrayList<>());
                result.getDistanceChange().get(vertex).add(Double.POSITIVE_INFINITY);
            }else
            {
                result.getDistance().put(vertex, 0.0);
                result.getDistanceChange().put(vertex, new ArrayList<>());
                result.getDistanceChange().get(vertex).add(0.0);
            }
        }
        for(int i=1;i<graph.getSize();i++)
        {
            for(Edge<V> e:graph.getEdges())
            {
                WeightedEdge<V> edge=(WeightedEdge<V>) e;
                Vertex<V> v=edge.getSource();
                Vertex<V> u=edge.getTarget();
                double dist=edge.getWeight();
                if(result.getDistance().get(v)!=Double.POSITIVE_INFINITY)
                {
                    double newDist=result.getDistance().get(v)+dist;
                    if(newDist<result.getDistance().get(u))
                    {
                        result.getDistance().put(u,newDist);
                        result.getDistanceChange().get(u).add(newDist);
                        result.getParents().put(u,v);
                    }
                }
            }
        }
        //sprawdzenie cykli o ujemnej wadze
        for(Edge<V> e:graph.getEdges())
        {
            WeightedEdge<V> edge=(WeightedEdge<V>) e;
            Vertex<V> v=edge.getSource();
            Vertex<V> u=edge.getTarget();
            double weight=edge.getWeight();
            if(result.getDistance().get(v)!=Double.POSITIVE_INFINITY&&result.getDistance().get(v)+weight<result.getDistance().get(u))
            {
                throw new BellmanFordAlgorithmException("Graph contains negative-weight cycle attainable from start.");
            }
        }
        if(result.getDistance().get(end)!=Double.POSITIVE_INFINITY)
        {
            Vertex<V>current=end;
            while (current!=null)
            {
                result.getPath().add(current);
                current=result.getParents().get(current);
            }
            Collections.reverse(result.getPath());
        }
        else
            throw new BellmanFordAlgorithmException("There is no path from "+start.getValue()+" to "+end.getValue());
        return result;
    }
}
