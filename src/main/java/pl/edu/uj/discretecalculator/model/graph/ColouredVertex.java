package pl.edu.uj.discretecalculator.model.graph;

import java.util.Objects;

public class ColouredVertex<V> extends Vertex<V>{
    private int colour=0;

    public ColouredVertex(int id,int colour)
    {
        super(id);
        this.colour=colour;
    }

    public ColouredVertex(int id)
    {
        super(id);
    }

    public ColouredVertex(int id,int colour,V value)
    {
        super(id,value);
        this.colour=colour;
    }

    public int getColour(){return colour;}

    public void setColour(int colour){
        this.colour=colour;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this==o)return true;
        if(o==null||getClass()!=o.getClass())return false;
        ColouredVertex<?> vertex=(ColouredVertex<?>) o;
        return this.getId()== vertex.getId()&&this.colour==vertex.getColour();
    }

    @Override
    public int hashCode()
    {
        return  Objects.hash(this.getId(),colour);
    }
}
