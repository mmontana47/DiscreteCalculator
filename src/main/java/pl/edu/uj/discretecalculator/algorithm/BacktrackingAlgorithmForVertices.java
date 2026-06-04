package pl.edu.uj.discretecalculator.algorithm;
import java.util.*;
import pl.edu.uj.discretecalculator.model.graph.*;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import pl.edu.uj.discretecalculator.model.graph.VertexColouredGraph;

import javax.swing.text.AttributeSet;

public class BacktrackingAlgorithmForVertices<V> implements AlgorithmicInterface<V,BacktrackingAlgorithmForVerticesResult<V>>{
    private final VertexColouredGraph<V> graph;
    private final ColouredVertex<V> startingVertex;

    public BacktrackingAlgorithmForVertices(VertexColouredGraph<V> graph,ColouredVertex<V> startingVertex)
    {
        this.graph=graph;
        this.startingVertex=startingVertex;
    }

    @Override
    public String algorithmName(){return "Backtracking algorithm for vertices";}

    //IMPORTANT: in this algorithm I assume the IDs of vertices are 0,1,2,3,...,graph_size-1
    @Override
    public BacktrackingAlgorithmForVerticesResult<V> start(Graph<V> graph)
    {
        BacktrackingAlgorithmForVerticesResult<V> result=new BacktrackingAlgorithmForVerticesResult<>();
        Deque<ColouredVertex<V>>vertices=new ArrayDeque<>();
        vertices.addLast(startingVertex);
        for(Vertex<V> v:this.graph.getVertices())
        {

            ColouredVertex<V> vertex=(ColouredVertex<V>) v;
            if(!vertex.equals(startingVertex))
            {
                vertices.addLast(vertex);
            }
        }
        for(int i=1;i<=this.graph.getSize();i++)
        {
            if(solve(vertices,i,result)) {
                this.graph.getVertices().forEach(v->result.getResult().add((ColouredVertex<V>)v));
                return result;
            }
        }
        return result;
    }

    private boolean solve(Deque<ColouredVertex<V>> vertices,int colurs,BacktrackingAlgorithmForVerticesResult<V> res) {
        if (vertices.isEmpty()) return true;
        ColouredVertex<V> current=vertices.pollFirst();
        for(int c=1;c<colurs;c++)
        {
            if(isSafe(current,c))
            {
                current.setColour(c);
                res.getColoringOrder().add(current);
                if(solve(vertices,colurs,res))return true;
                current.setColour(0);
                res.getColoringOrder().add(current);
                vertices.addFirst(current);
            }
        }
        return false;
    }

    private boolean isSafe(ColouredVertex<V> vertex,int colour)
    {
        for(Vertex<V> v:this.graph.getNeighbors(vertex))
        {
            ColouredVertex<V> neighbor=(ColouredVertex<V>) v;
            if(colour==neighbor.getColour())
                return false;
        }
        return true;
    }

}
