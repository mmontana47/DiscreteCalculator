package pl.edu.uj.discretecalculator.model.graph;

import pl.edu.uj.discretecalculator.exception.WrongEdgeTypeException;

public class WeightedGraph<V> extends Graph<V> {
    public WeightedGraph(String name)
    {
        super(name);
    }

    @Override
    public void addEdge(Edge<V> edge)
    {
        if(!(edge instanceof WeightedEdge<?>))
            throw new WrongEdgeTypeException("Edges need to be of WeightedEdge type.");
        super.addEdge(edge);
    }
}
