package pl.edu.uj.discretecalculator.model.graph;

import java.util.Objects;
import java.util.Optional;

public class Vertex<V> {
    private  final int id;
    private V value;
    private int colour=0;

    public Vertex(int id)
    {
        this.id=id;
    }

    public Vertex(int id,V value)
    {
        this.id=id;
        this.value=value;
    }

    public Vertex(int id,V value,int colour)
    {
        this.id=id;
        this.value=value;
        this.colour=colour;
    }

    public Vertex(int id,int colour)
    {
        this.id=id;
        this.colour=colour;
    }

    public int getId(){return id;}
    public V getValue(){ return value; }
    public int getColour(){return colour;}
    public void setValue(V v){value=v;}
    public void setColour(int c){colour=c;}

    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(o==null||getClass()!=o.getClass()) return false;
        Vertex<?> vertex=(Vertex<?>)o;
        return id == vertex.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    //opcjonalnie do testów
    @Override
    public String toString() {
        return "Vertex{" + "id=" + id + ", colour=" + colour + '}';
    }
}
