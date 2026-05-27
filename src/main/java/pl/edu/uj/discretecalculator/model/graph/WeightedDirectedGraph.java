package pl.edu.uj.discretecalculator.model.graph;

import pl.edu.uj.discretecalculator.exception.WrongEdgeTypeException;

public class WeightedDirectedGraph<V> extends Graph<V> {

    public WeightedDirectedGraph(String name) {
        super(name);
    }

    @Override
    public void addEdge(Edge<V> edge) {
        if (!(edge instanceof WeightedDirectedEdge<?>)) {
            throw new WrongEdgeTypeException("Edges need to be of WeightedDirectedEdge type.");
        }

        addVertex(edge.getSource());
        addVertex(edge.getTarget());

        this.getAdjacencyList().get(edge.getSource()).add(edge);
    }
}