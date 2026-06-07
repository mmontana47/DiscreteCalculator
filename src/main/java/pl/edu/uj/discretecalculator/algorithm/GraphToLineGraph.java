package pl.edu.uj.discretecalculator.algorithm;


import pl.edu.uj.discretecalculator.model.graph.*;

import java.util.*;

public class GraphToLineGraph<V> {
    public static <V> Graph<Edge<V>> Convert(Graph<V> graph)
    {
        Graph<Edge<V>> lineGraph = new Graph<>(graph.getName() + " - Line Graph");
        Map<Edge<V>, Vertex<Edge<V>>> edgeToVertexMap = new HashMap<>();

        for (Edge<V> originalEdge : graph.getEdges()) {
            Vertex<Edge<V>> newVertex = new Vertex<>(originalEdge.getId(), originalEdge);
            lineGraph.addVertex(newVertex);
            edgeToVertexMap.put(originalEdge, newVertex);
        }

        int newEdgeIdCounter = 0;

        for (Vertex<V> originalVertex : graph.getVertices()) {
            List<Edge<V>> incidentEdges = graph.getIncidentEdges(originalVertex);

            for (int i = 0; i < incidentEdges.size(); i++) {
                for (int j = i + 1; j < incidentEdges.size(); j++) {
                    Edge<V> e1 = incidentEdges.get(i);
                    Edge<V> e2 = incidentEdges.get(j);
                    Vertex<Edge<V>> v1 = edgeToVertexMap.get(e1);
                    Vertex<Edge<V>> v2 = edgeToVertexMap.get(e2);
                    if (!lineGraph.getNeighbors(v1).contains(v2)) {
                        Edge<Edge<V>> newLineEdge = new Edge<>(v1, v2, newEdgeIdCounter++);
                        lineGraph.addEdge(newLineEdge);
                    }
                }
            }
        }

        return lineGraph;

    }
}
