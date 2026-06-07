package pl.edu.uj.discretecalculator.model.graph;

import java.util.Objects;

public class DirectedEdge<V> extends Edge<V>{
    public DirectedEdge(Vertex<V> source , Vertex<V> target,int id)
    {
        super(source,target,id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectedEdge<?> edge = (DirectedEdge<?>) o;
        return this.getId() == edge.getId()&&this.getSource().equals(edge.getSource())&&this.getTarget().equals(edge.getTarget());
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.getId(),this.getSource(),this.getTarget());
    }

    //opcjonalnie do testów
    @Override
    public String toString() {
        return "Edge{" +
                "id=" + this.getId() +
                ", source=" + this.getSource().getId() +
                " ----> target=" + this.getTarget().getId() +
                '}';
    }
}
