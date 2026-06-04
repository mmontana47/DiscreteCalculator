package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.*;

import java.util.*;

public class GreedyEdgeColoringResult<V> {
    private final ArrayList<ColouredEdge<V>> coloringOrder;

    public GreedyEdgeColoringResult(){
        coloringOrder=new ArrayList<>();
    }

    public ArrayList<ColouredEdge<V>> getColoringOrder(){return coloringOrder;}
}
