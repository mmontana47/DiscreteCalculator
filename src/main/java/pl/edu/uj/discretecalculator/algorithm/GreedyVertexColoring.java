package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.ColouredVertex;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import pl.edu.uj.discretecalculator.model.graph.VertexColouredGraph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class GreedyVertexColoring<V> implements AlgorithmicInterface<V,GreedyVertexColoringResult<V>>{
    private final ColouredVertex<V> startingVertex;
    private final VertexColouredGraph<V> graph;
    private final Set<Integer> usedColours;

    public GreedyVertexColoring(VertexColouredGraph<V> graph,ColouredVertex<V> startingVertex)
    {
        this.startingVertex=startingVertex;
        this.graph=graph;
        this.usedColours=new HashSet<>();
        this.startingVertex.setColour(1);
        usedColours.add(1);
    }

    @Override
    public String algorithmName(){
        return "Greedy vertex coloring algorithm";
    }

    @Override
    public GreedyVertexColoringResult<V> start(Graph<V> graph)
    {
        GreedyVertexColoringResult<V> result =new GreedyVertexColoringResult<>();
        result.getColoringOrder().add(startingVertex);
        Set<ColouredVertex<V>> visited=new HashSet<>();
        Queue<ColouredVertex<V>> bfs=new LinkedList<>();

        visited.add(startingVertex);
        for(Vertex<V> vertex:this.graph.getNeighbors(startingVertex))
        {
            ColouredVertex<V> v=(ColouredVertex<V>)vertex;
            if(!visited.contains(v))
            {
                bfs.add(v);
            }
        }

        while(!bfs.isEmpty())
        {
            ColouredVertex<V> current = bfs.poll();
            if(visited.contains(current))
                continue;
            colorVertex(current);
            visited.add(current);
            result.getColoringOrder().add(current);
            for(Vertex<V> v:this.graph.getNeighbors(current))
            {
                ColouredVertex<V> vertex=(ColouredVertex<V>)v;
                if(!visited.contains(vertex))
                    bfs.add(vertex);
            }
        }

        // handling vertices in other connected components
        for(Vertex<V> v:this.graph.getVertices())
        {
            ColouredVertex<V> vertex=(ColouredVertex<V>)v;
            if(!visited.contains(vertex))
            {
                colorVertex(vertex);
                visited.add(vertex);
                result.getColoringOrder().add(vertex);
            }
        }



        return result;
    }

    private void colorVertex(ColouredVertex<V> vertex)
    {
        Set<Integer> neighborColours=new HashSet<>();
        for(Vertex<V> v:this.graph.getNeighbors(vertex))
        {
            ColouredVertex<V> neigbor=(ColouredVertex<V>)v;
            int colour=neigbor.getColour();
            if(colour>0)
                neighborColours.add(colour);
        }
        int myColour=1;
        while(neighborColours.contains(myColour))
            myColour++;
        vertex.setColour(myColour);
        this.usedColours.add(myColour);
    }
}

