package pl.edu.uj.discretecalculator.model.graph;

import pl.edu.uj.discretecalculator.exception.WrongEdgeTypeException;

public class DirectedGraph<V> extends Graph<V>{
    public DirectedGraph(String name)
    {
        super(name);
    }

    @Override
    public void addEdge(Edge<V> edge)
    {
        if(!(edge instanceof DirectedEdge<?>))
            throw new WrongEdgeTypeException("Edges need to be directed.");
        super.addVertex(edge.getSource());
        super.addVertex(edge.getTarget());
        this.getAdjacencyList().get(edge.getSource()).add(edge);
    }
}
