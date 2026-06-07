package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;

import java.util.*;

public class KosarajuAlgorithmResult<V> {
    private final ArrayList<ArrayList<Vertex<V>>> CompoentsAfterDFS1;
    private final ArrayList<ArrayList<Vertex<V>>> StronglyConnectedComponents;

    public KosarajuAlgorithmResult()
    {
        CompoentsAfterDFS1=new ArrayList<>();
        StronglyConnectedComponents=new ArrayList<>();
    }

    public ArrayList<ArrayList<Vertex<V>>> getCompoentsAfterDFS1(){return CompoentsAfterDFS1;}
    public ArrayList<ArrayList<Vertex<V>>> getStronglyConnectedComponents(){return StronglyConnectedComponents;}
}
