package pl.edu.uj.discretecalculator.model.graph;

import pl.edu.uj.discretecalculator.exception.WrongEdgeTypeException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EdgeColouredGraph<V> extends Graph<V> {
    public EdgeColouredGraph(String name)
    {
        super(name);
    }

    @Override
    public void addEdge(Edge<V> edge) {
        if(!(edge instanceof ColouredEdge<?>))
            throw new WrongEdgeTypeException("EDGE NEEDS TO BE OF ColouredEdge TYPE.");
        addVertex(edge.getSource());
        addVertex(edge.getTarget());

        this.getAdjacencyList().get(edge.getSource()).add(edge);


        this.getAdjacencyList().get(edge.getTarget()).add(edge);

    }


}
