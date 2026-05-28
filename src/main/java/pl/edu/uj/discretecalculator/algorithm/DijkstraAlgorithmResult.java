package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;

import java.util.*;

public class DijkstraAlgorithmResult<V> {
    private final ArrayList<Vertex<V>> shortest_path;
    private final Map<Vertex<V>,ArrayList<Double>>distances;
    private final Map<Vertex<V>,Vertex<V>>parents;
    private final ArrayList<Vertex<V>>visited;
    private double shortest_distance;

    public DijkstraAlgorithmResult()
    {
        shortest_path=new ArrayList<>();
        distances=new HashMap<>();
        parents=new HashMap<>();
        visited=new ArrayList<>();
    }

    public ArrayList<Vertex<V>> getShortest_path(){return shortest_path;}
    public Map<Vertex<V>,ArrayList<Double>> getDistances(){return distances;}
    public Map<Vertex<V>,Vertex<V>> getParents(){return parents;}
    public ArrayList<Vertex<V>> getVisited(){return visited;}
    public void setShortest_distance(double dist){shortest_distance=dist;}
    public double getShortest_distance(){return shortest_distance;}
}
