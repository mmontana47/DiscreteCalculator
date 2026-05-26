package pl.edu.uj.discretecalculator.model.graph;

import java.util.Objects;

public class ColouredEdge<V> extends Edge<V> {
    private int colour=0;

    public ColouredEdge(Vertex<V> source, Vertex<V> target, int id)
    {
        super(source,target,id);
    }

    public ColouredEdge(Vertex<V> source,Vertex<V> target,int id,int colour)
    {
        super(source,target,id);
        this.colour=colour;
    }

    public int getColour(){return colour;}
    public void setColour(int colour){this.colour=colour;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColouredEdge<?> edge = (ColouredEdge<?>) o;
        return this.getId() == edge.getId()&&colour==edge.getColour();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(),colour);
    }

    //opcjonalnie do testów
    @Override
    public String toString() {
        return "Edge{" +
                "id=" + this.getId() +
                ", colour="+colour+
                ", source=" + this.getSource().getId() +
                ", target=" + this.getTarget().getId() +
                '}';
    }
}
