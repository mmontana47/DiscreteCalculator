package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import pl.edu.uj.discretecalculator.model.graph.Edge;
public class Pair<V>{
    private Vertex<V> first;
    private Vertex<V> second;
    public Pair(Vertex<V> first,Vertex<V> second)
    {
        this.first=first;
        this.second=second;
    }
    public Vertex<V> getFirst()
    {
        return first;
    }
    public Vertex<V> getSecond()
    {
        return second;
    }

    public void setFirst(Vertex<V> first) {
        this.first = first;
    }
    public void setSecond(Vertex<V> second)
    {
        this.second=second;
    }
}
