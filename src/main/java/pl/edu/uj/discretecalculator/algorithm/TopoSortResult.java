package pl.edu.uj.discretecalculator.algorithm;
import java.util.*;
import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.Vector;
public class TopoSortResult<V> {
    public final ArrayList<Vertex<V>> result;
    public TopoSortResult()
    {
        result=new ArrayList<>();
    }

    public ArrayList<Vertex<V>> getResult(){return result;}
}
