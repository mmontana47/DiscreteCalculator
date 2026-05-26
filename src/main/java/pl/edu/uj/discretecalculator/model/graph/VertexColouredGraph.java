package pl.edu.uj.discretecalculator.model.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VertexColouredGraph<V> extends Graph<V> {


    public VertexColouredGraph(String name)
    {
        super(name);
    }

    @Override
    public boolean addVertex(Vertex<V> vertex)
    {
        if(!(vertex instanceof ColouredVertex<?>))
            return false;
        return super.addVertex(vertex);
    }

    @Override
    public void addEdge(Edge<V> edge) {
        addVertex(edge.getSource());
        addVertex(edge.getTarget());

        this.getAdjacencyList().get(edge.getSource()).add(edge);


        this.getAdjacencyList().get(edge.getTarget()).add(edge);

    }



}
