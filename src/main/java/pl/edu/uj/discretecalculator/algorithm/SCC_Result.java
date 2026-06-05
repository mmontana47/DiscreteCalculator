package pl.edu.uj.discretecalculator.algorithm;

import java.util.*;
import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.Vector;

public class SCC_Result<V> {
    private final ArrayList<Vertex<V>> dfs_order;
    private final HashMap<Integer,ArrayList<Vertex<V>>> StronglyConnectedComponentsMap;
    public SCC_Result()
    {
        dfs_order=new ArrayList<Vertex<V>>();
        StronglyConnectedComponentsMap=new HashMap<Integer,ArrayList<Vertex<V>>>();
    }

    public ArrayList<Vertex<V>> getDfs_order(){
        return dfs_order;
    }
    public HashMap<Integer,ArrayList<Vertex<V>>> getStronglyConnectedComponentsMap(){
        return StronglyConnectedComponentsMap;
    }
}
