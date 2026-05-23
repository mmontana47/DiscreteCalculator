package pl.edu.uj.discretecalculator.model.graph;

import pl.edu.uj.discretecalculator.model.graph.Vertex;

public class Edge<V> {
    private final Vertex<V> source;
    private final Vertex<V> target;
    private final int id;
    int colour=0;

    public Edge(Vertex<V> source,Vertex<V> target,int id)
    {
        if(source==null||target==null)
            throw new IllegalArgumentException("Edge needs source and target.");
        this.source=source;
        this.target=target;
        this.id=id;
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
    public void setColour(int c){colour=c;}
}
