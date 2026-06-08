package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;

public class KosarajuAlgorithm<V> implements AlgorithmicInterface<V, KosarajuAlgorithmResult<V>> {
    private DirectedGraph<V> graph;

    public KosarajuAlgorithm() {}

    public KosarajuAlgorithm(DirectedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public String algorithmName() {
        return "Kosaraju Algorithm (Strongly Connected Components)";
    }

    @Override
    public KosarajuAlgorithmResult<V> start(Graph<V> graph) {

        KosarajuAlgorithmResult<V> result = new KosarajuAlgorithmResult<>();
        Stack<Vertex<V>> stack = new Stack<>();
        Set<Vertex<V>> visited = new HashSet<>();
        Map<Integer, List<Vertex<V>>> sccMap = result.getStronglyConnectedComponents();


        for (Vertex<V> vertex : graph.getVertices()) {
            if (!visited.contains(vertex)) {
                dfs1(graph, vertex, visited, stack, result, sccMap);
            }
        }


        Map<Vertex<V>, List<Edge<V>>> transposedAdj = buildTransposed(graph);

        visited.clear();
        int componentIdx = 0;

        while (!stack.isEmpty()) {
            Vertex<V> vertex = stack.pop();
            result.addStep(new KosarajuAlgorithmResult.KosarajuStep<>(
                    KosarajuAlgorithmResult.Phase.DFS2_POP, vertex, null, visited, stack, sccMap));

            if (!visited.contains(vertex)) {
                ArrayList<Vertex<V>> scc = new ArrayList<>();
                sccMap.put(componentIdx, scc);

                dfs2(transposedAdj, vertex, visited, scc, stack, result, sccMap);

                result.addStep(new KosarajuAlgorithmResult.KosarajuStep<>(
                        KosarajuAlgorithmResult.Phase.SCC_FOUND, vertex, null, visited, stack, sccMap));
                componentIdx++;
            }
        }
        return result;
    }

    private void dfs1(Graph<V> graph, Vertex<V> vertex, Set<Vertex<V>> visited, Stack<Vertex<V>> stack,
                      KosarajuAlgorithmResult<V> result, Map<Integer, List<Vertex<V>>> sccMap) {
        visited.add(vertex);
        result.addStep(new KosarajuAlgorithmResult.KosarajuStep<>(
                KosarajuAlgorithmResult.Phase.DFS1_VISIT, vertex, null, visited, stack, sccMap));

        for (Edge<V> edge : graph.getEdges()) {

            if (!edge.getSource().equals(vertex)) continue;

            Vertex<V> neighbor = edge.getTarget();
            result.addStep(new KosarajuAlgorithmResult.KosarajuStep<>(
                    KosarajuAlgorithmResult.Phase.DFS1_CHECK_EDGE, vertex, edge, visited, stack, sccMap));

            if (!visited.contains(neighbor)) {
                dfs1(graph, neighbor, visited, stack, result, sccMap);
            }
        }

        stack.push(vertex);
        result.addStep(new KosarajuAlgorithmResult.KosarajuStep<>(
                KosarajuAlgorithmResult.Phase.DFS1_PUSH, vertex, null, visited, stack, sccMap));
    }

    private void dfs2(Map<Vertex<V>, List<Edge<V>>> transposedAdjList, Vertex<V> vertex, Set<Vertex<V>> visited,
                      List<Vertex<V>> scc, Stack<Vertex<V>> stack, KosarajuAlgorithmResult<V> result, Map<Integer, List<Vertex<V>>> sccMap) {
        visited.add(vertex);
        scc.add(vertex);
        result.addStep(new KosarajuAlgorithmResult.KosarajuStep<>(
                KosarajuAlgorithmResult.Phase.DFS2_VISIT, vertex, null, visited, stack, sccMap));

        for (Edge<V> edge : transposedAdjList.getOrDefault(vertex, Collections.emptyList())) {
            Vertex<V> neighbor = edge.getTarget();
            result.addStep(new KosarajuAlgorithmResult.KosarajuStep<>(
                    KosarajuAlgorithmResult.Phase.DFS2_CHECK_EDGE, vertex, edge, visited, stack, sccMap));

            if (!visited.contains(neighbor)) {
                dfs2(transposedAdjList, neighbor, visited, scc, stack, result, sccMap);
            }
        }
    }

    private Map<Vertex<V>, List<Edge<V>>> buildTransposed(Graph<V> graph) {
        Map<Vertex<V>, List<Edge<V>>> transposed = new HashMap<>();

        for (Vertex<V> v : graph.getVertices()) {
            transposed.put(v, new ArrayList<>());
        }

        for (Edge<V> edge : graph.getEdges()) {
            Vertex<V> src = edge.getSource();
            Vertex<V> tgt = edge.getTarget();


            Edge<V> reversed = new DirectedEdge<>(tgt, src, edge.getId());
            transposed.computeIfAbsent(tgt, k -> new ArrayList<>()).add(reversed);
        }

        return transposed;
    }
}
