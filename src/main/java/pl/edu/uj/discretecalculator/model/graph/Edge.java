package pl.edu.uj.discretecalculator.model.graph;

import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.Objects;

public class Edge<V> {
    private final Vertex<V> source;
    private final Vertex<V> target;
    private final int id;
    private int colour=0;
    private double weight = 1.0;
    private boolean isDirected = false;

    public Edge(Vertex<V> source,Vertex<V> target,int id)
    {
        if(source==null||target==null)
            throw new IllegalArgumentException("Edge needs source and target.");
        this.source=source;
        this.target=target;
        this.id=id;
    }

    public Edge(Vertex<V> source, Vertex<V> target, int id, double weight) {
        this.source = source;
        this.target = target;
        this.id = id;
        this.weight = weight;
    }

    public Edge(Vertex<V> source,Vertex<V> target,int id,int colour)
    {
        if(source==null||target==null)
            throw new IllegalArgumentException("Edge needs source and target.");
        this.source=source;
        this.target=target;
        this.id=id;
        this.colour=colour;
    }

    public Vertex<V> getSource(){return source;}
    public Vertex<V> getTarget(){return target;}
    public int getId(){return id;}
    public int getColour(){return colour;}
    public double getWeight() {return weight;}
    public boolean getIsDirected() {return isDirected;}
    public void setColour(int c){colour=c;}
    public void setWeight(double weight) {this.weight = weight;}
    public void setIsDirected(boolean isDirected) {this.isDirected = isDirected;};

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge<?> edge = (Edge<?>) o;
        return id == edge.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //opcjonalnie do testów
    @Override
    public String toString() {
        return "Edge{" +
                "id=" + id +
                ", source=" + source.getId() +
                ", target=" + target.getId() +
                ", weight=" + weight +
                '}';
    }
}
