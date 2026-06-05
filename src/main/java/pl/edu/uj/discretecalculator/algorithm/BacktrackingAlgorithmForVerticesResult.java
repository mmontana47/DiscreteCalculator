package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.*;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.*;


public class BacktrackingAlgorithmForVerticesResult<V> {
    //every time vertex changes colour we put it into array
    private final ArrayList<Pair<Integer,Integer>> coloringOrder;
    private final List<ColouredVertex<V>> result;

    public BacktrackingAlgorithmForVerticesResult()
    {
        coloringOrder=new ArrayList<>();
        result=new ArrayList<>();
    }

    public ArrayList<Pair<Integer,Integer>> getColoringOrder(){
        return coloringOrder;
    }

    public List<ColouredVertex<V>> getResult(){return result;}
}
