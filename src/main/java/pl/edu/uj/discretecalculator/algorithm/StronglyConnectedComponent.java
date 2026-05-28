package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.*;
import java.util.*;


//Algorytm Tarjana
public class StronglyConnectedComponent<V> implements AlgorithmicInterface<V, SCC_Result<V>> {

    private int index;
    private Map<Vertex<V>, Integer> indices;
    private Map<Vertex<V>, Integer> low;
    private Deque<Vertex<V>> stack;
    private Set<Vertex<V>> onStack;
    private List<List<Vertex<V>>> stronglyConnectedComponents;
    private Integer component_idx;
    SCC_Result<V> result;

    @Override
    public String algorithmName() {
        return "Algorytm Tarjana (Silnie Spójne Składowe)";
    }

    @Override
    public SCC_Result<V> start(Graph<V> graph) {
        component_idx=0;
        index = 0;
        indices = new HashMap<>();
        low = new HashMap<>();
        stack = new ArrayDeque<>();
        onStack = new HashSet<>();
        result=new SCC_Result<V>();
        for (Vertex<V> v : graph.getVertices()) {
            if (!indices.containsKey(v)) {
                strongConnect(v, graph);
            }
        }
        return result;
    }

    private void strongConnect(Vertex<V> v, Graph<V> graph) {
        indices.put(v, index);
        result.getDfs_order().add(v);
        low.put(v, index);
        index++;

        stack.push(v);
        onStack.add(v);

        for (Vertex<V> w : graph.getNeighbors(v)) {
            if (!indices.containsKey(w)) {
                strongConnect(w, graph);
                low.put(v, Math.min(low.get(v), low.get(w)));
            } else if (onStack.contains(w)) {

                low.put(v, Math.min(low.get(v), indices.get(w)));
            }
        }

        if (low.get(v).equals(indices.get(v))) {
            result.getStronglyConnectedComponentsMap().put(component_idx,new ArrayList<Vertex<V>>());

            Vertex<V> w;
            do {
                w = stack.pop();
                onStack.remove(w);
                result.getStronglyConnectedComponentsMap().get(component_idx).add(w);
            } while (!w.equals(v));
            component_idx++;

        }
    }
}
