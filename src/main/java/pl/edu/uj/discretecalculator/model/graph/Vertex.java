package pl.edu.uj.discretecalculator.model.graph;

import java.util.Objects;

public class Vertex<V> {
    private  final int id;
    private V value;


    public Vertex(int id)
    {
        this.id=id;
    }

    public Vertex(int id,V value)
    {
        this.id=id;
        this.value=value;
    }

    public int getId(){return id;}
    public V getValue(){return value;}

    public void setValue(V v){value=v;}

    @Override
    public  boolean equals(Object o)
    {
        if(this==o)return true;
        if(o==null||getClass()!=o.getClass())return false;
        Vertex<?> vertex=(Vertex<?>)o;
        return id == vertex.id;
    }
    @Override
    public int hashCode()
    {
        return  Objects.hash(id);
    }
}
