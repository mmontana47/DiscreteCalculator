package pl.edu.uj.discretecalculator.model.graph;

import java.util.Objects;

public class WeightedEdge<V> extends Edge<V>{
    private double weight = 1.0;

    public WeightedEdge(Vertex<V> source,Vertex<V> target,int id)
    {
        super(source,target,id);
    }

    public WeightedEdge(Vertex<V> source,Vertex<V> target,int id,double weight)
    {
        super(source, target, id);
        this.weight=weight;
    }

    public double getWeight(){return weight;}
    public void setWeight(double weight){this.weight=weight;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightedEdge<?> edge = (WeightedEdge<?>) o;
        return this.getId() == edge.getId()&&weight== edge.getWeight();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(),weight);
    }

    //opcjonalnie do testów
    @Override
    public String toString() {
        return "Edge{" +
                "id=" + this.getId() +
                ", weight="+ weight+
                ", source=" + this.getSource().getId() +
                ", target=" + this.getTarget().getId() +
                '}';
    }
}
