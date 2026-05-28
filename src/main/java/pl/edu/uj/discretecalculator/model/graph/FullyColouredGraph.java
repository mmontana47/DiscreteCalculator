package pl.edu.uj.discretecalculator.model.graph;

import pl.edu.uj.discretecalculator.exception.WrongEdgeTypeException;

public class FullyColouredGraph<V> extends VertexColouredGraph<V> {
    public FullyColouredGraph(String name){super(name);}

    @Override
    public void addEdge(Edge<V> edge)
    {
        if(!(edge instanceof ColouredEdge<?>))
            throw new WrongEdgeTypeException("EDGE NEEDS TO BE OF ColouredEdge TYPE.");
        super.addEdge(edge);
    }
}
