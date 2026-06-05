package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.ColouredEdge;

import java.util.*;

public class BacktrackingAlgorithmForEdgesResult<V> {
    private final ArrayList<Pair<Integer,Integer>> coloringOrder;
    private final ArrayList<ColouredEdge<V>> finalColoring;

    public BacktrackingAlgorithmForEdgesResult()
    {
        coloringOrder=new ArrayList<>();
        finalColoring=new ArrayList<>();
    }

    public ArrayList<Pair<Integer,Integer>> getColoringOrder(){return coloringOrder;}
    public ArrayList<ColouredEdge<V>> getFinalColoring(){return finalColoring;}
}
