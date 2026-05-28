package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;


import pl.edu.uj.discretecalculator.model.graph.*;

public class BellmanFordResult<V>{
    private final ArrayList<Vertex<V>> path;
    private final Map<Vertex<V>,Vertex<V>> parents;
    private final Map<Vertex<V>,ArrayList<Double>> distance_change;
    private final Map<Vertex<V>,Double> distance;

    public BellmanFordResult()
    {
        path=new ArrayList<>();
        parents=new HashMap<>();
        distance_change=new HashMap<>();
        distance=new HashMap<>();
    }

    public ArrayList<Vertex<V>> getPath(){return path;}
    public Map<Vertex<V>,Vertex<V>> getParents(){return parents;}
    public Map<Vertex<V>,ArrayList<Double>> getDistanceChange(){return distance_change;}
    public Map<Vertex<V>, Double> getDistance(){return distance;}

}
