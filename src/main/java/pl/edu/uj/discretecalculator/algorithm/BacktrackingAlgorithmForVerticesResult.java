package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.*;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.*;


public class BacktrackingAlgorithmForVerticesResult<V> {
    //every time vertex changes colour we put it into array
    private final ArrayList<ColouredVertex<V>> coloringOrder;
    private final List<ColouredVertex<V>> result;

    public BacktrackingAlgorithmForVerticesResult()
    {
        coloringOrder=new ArrayList<>();
        result=new ArrayList<>();
    }

    public ArrayList<ColouredVertex<V>> getColoringOrder(){
        return coloringOrder;
    }

    public List<ColouredVertex<V>> getResult(){return result;}
}
