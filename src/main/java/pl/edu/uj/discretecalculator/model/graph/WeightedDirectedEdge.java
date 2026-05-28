package pl.edu.uj.discretecalculator.model.graph;

import java.util.Objects;

public class WeightedDirectedEdge<V> extends WeightedEdge<V> {
    private double weight = 1.0;

    public WeightedDirectedEdge(Vertex<V> source, Vertex<V> target, int id) {
        super(source, target, id);
    }

    public WeightedDirectedEdge(Vertex<V> source, Vertex<V> target, int id, double weight) {
        super(source, target, id);
        this.weight = weight;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightedDirectedEdge<?> edge = (WeightedDirectedEdge<?>) o;
        return this.getId() == edge.getId()
                && Double.compare(edge.weight, weight) == 0
                && this.getSource().equals(edge.getSource())
                && this.getTarget().equals(edge.getTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), weight, this.getSource(), this.getTarget());
    }

    @Override
    public String toString() {
        return "Edge{" +
                "id=" + this.getId() +
                ", weight=" + weight +
                ", source=" + this.getSource().getId() +
                " ----> target=" + this.getTarget().getId() +
                '}';
    }
}