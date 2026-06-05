package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;

public class BacktrackingAlgorithmForEdges<V> implements AlgorithmicInterface<V,BacktrackingAlgorithmForEdgesResult<V>>{
    private final EdgeColouredGraph<V> graph;

    public BacktrackingAlgorithmForEdges(EdgeColouredGraph<V> graph)
    {
        this.graph=graph;
    }

    @Override
    public String algorithmName(){return "Backtracking algorithm for edges.";}

    @Override
    public BacktrackingAlgorithmForEdgesResult<V> start(Graph<V> graph)
    {
        BacktrackingAlgorithmForEdgesResult<V> result=new BacktrackingAlgorithmForEdgesResult<>();
        Deque<ColouredEdge<V>> edges=new ArrayDeque<>();
        for(Edge<V> e:this.graph.getEdges())
        {
            ColouredEdge<V> edge=(ColouredEdge<V>) e;
            edges.addLast(edge);
        }
        int k=this.graph.getEdges().size();
        for(int i=1;i<=k;i++)
        {
            if(solve(edges,result,i))
            {
                this.graph.getEdges().forEach(e->result.getFinalColoring().add((ColouredEdge<V>) e));
                return result;
            }
        }
        return result;
    }

    private boolean solve(Deque<ColouredEdge<V>> edges,BacktrackingAlgorithmForEdgesResult<V> result,int colour)
    {
        if(edges.isEmpty())return true;

        ColouredEdge<V> current=edges.pollFirst();
        for(int c=1;c<=colour;c++)
        {
            if(isSafe(current,c))
            {
                current.setColour(c);
                result.getColoringOrder().add(current);
                if(solve(edges,result,colour))return true;
                current.setColour(0);
                result.getColoringOrder().add(current);
                edges.addFirst(current);
            }
        }
        return false;
    }

    private boolean isSafe(ColouredEdge<V> edge,int colour)
    {
        for(Edge<V> incident:this.graph.getIncidentEdges(edge.getSource()))
        {
            if(!incident.equals(edge)&&((ColouredEdge<V>)incident).getColour()==colour)
            {
                return false;
            }
        }
        for(Edge<V> incident:this.graph.getIncidentEdges(edge.getTarget()))
        {
            if(!incident.equals(edge)&&((ColouredEdge<V>)incident).getColour()==colour)
            {
                return false;
            }
        }
        return true;
    }
}
