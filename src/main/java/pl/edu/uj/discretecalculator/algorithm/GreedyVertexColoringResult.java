package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.*;

import java.util.*;

public class GreedyVertexColoringResult<V> {
    private final ArrayList<ColouredVertex<V>> ColoringOrder;

    public GreedyVertexColoringResult()
    {
        ColoringOrder=new ArrayList<>();
    }

    public ArrayList<ColouredVertex<V>> getColoringOrder(){
        return ColoringOrder;
    }
}
