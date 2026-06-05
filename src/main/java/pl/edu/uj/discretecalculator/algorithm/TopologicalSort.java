package pl.edu.uj.discretecalculator.algorithm;
import java.util.*;

import pl.edu.uj.discretecalculator.exception.TopologicalSortException;
import pl.edu.uj.discretecalculator.model.graph.*;

public class TopologicalSort<V> implements AlgorithmicInterface<V,List<Vertex<V>>>{
    TopoSortResult<V> toposort;
    DirectedGraph<V> modifiable_graph;
    @Override
    public String algorithmName(){
        return "Obgryzanie grafu(topologiczny sort)";
    }

    @Override
    public List<Vertex<V>> start(Graph<V> graph)
    {
        toposort=new TopoSortResult<>();
        modifiable_graph=new DirectedGraph<>("temporary");
        for(Vertex<V> v:graph.getVertices())
        {
            modifiable_graph.addVertex(v);
        }
        for(Map.Entry<Vertex<V>,List<Edge<V>>> entry:graph.getAdjacencyList().entrySet())
        {
            for (Edge<V> edge:entry.getValue())
            {
                DirectedEdge<V> edge1=new DirectedEdge<>(edge.getTarget(),edge.getSource(),edge.getId());
                modifiable_graph.addEdge(edge1);
            }
        }
        topoSort(modifiable_graph);
        if(toposort.getResult().size()!=graph.getVertices().size())
            throw new TopologicalSortException("Graph contains cycle");
        return toposort.getResult();
    }

    private void topoSort(DirectedGraph<V> modifiable_graph)
    {
        boolean contains_degree_zero=false;
        for(Vertex<V> v:modifiable_graph.getVertices())
        {
            if(modifiable_graph.getNeighbors(v).isEmpty())
            {
                modifiable_graph.deleteVertex(v);
                toposort.getResult().add(v);
                contains_degree_zero=true;
            }
        }
        if(contains_degree_zero)
            topoSort(modifiable_graph);
    }
}
