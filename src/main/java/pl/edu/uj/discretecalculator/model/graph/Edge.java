package pl.edu.uj.discretecalculator.model.graph;

import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.Objects;

public class Edge<V> {
    private final Vertex<V> source;
    private final Vertex<V> target;
    private final int id;


    public Edge(Vertex<V> source,Vertex<V> target,int id)
    {
        if(source==null||target==null)
            throw new IllegalArgumentException("Edge needs source and target.");
        this.source=source;
        this.target=target;
        this.id=id;
    }

    public Vertex<V> getSource(){return source;}
    public Vertex<V> getTarget(){return target;}
    public int getId(){return id;}

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
                '}';
    }
}
