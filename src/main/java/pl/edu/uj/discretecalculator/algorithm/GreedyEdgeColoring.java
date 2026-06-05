package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;

public class GreedyEdgeColoring<V> implements AlgorithmicInterface<V,GreedyEdgeColoringResult<V>>{
    private final EdgeColouredGraph<V> graph;
    private final HashSet<Integer> usedColours;

    public GreedyEdgeColoring(EdgeColouredGraph<V> graph)
    {
        this.graph=graph;
        this.usedColours=new HashSet<>();
    }

    @Override
    public String algorithmName(){return "Greedy Edge Coloring Algorithm";}

    @Override
    public GreedyEdgeColoringResult<V> start(Graph<V> graph)
    {
        GreedyEdgeColoringResult<V> result=new GreedyEdgeColoringResult<>();
        for(Edge<V> e:this.graph.getEdges())
        {
            ColouredEdge<V> edge=(ColouredEdge<V>)e;
            List<Edge<V>> sourceIncidentEdges = graph.getIncidentEdges(edge.getSource());
            List<Edge<V>> targetIncidentEdges = graph.getIncidentEdges(edge.getTarget());

            for(Edge<V> e1:sourceIncidentEdges)
            {
                if (!e1.equals(edge)) {
                    int c = ((ColouredEdge<V>) e1).getColour();
                    if (c > 0) usedColours.add(c);
                }
            }
            for (Edge<V> e1 : targetIncidentEdges) {
                if (!e1.equals(edge)) {
                    int c = ((ColouredEdge<V>) e1).getColour();
                    if (c > 0) usedColours.add(c);
                }
            }
            int colorToAssign = 1;
            while (usedColours.contains(colorToAssign)) {
                colorToAssign++;
            }
            edge.setColour(colorToAssign);
            result.getColoringOrder().add(edge);
        }
        return  result;
    }
}
