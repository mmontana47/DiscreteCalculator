package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.Graph;

public interface AlgorithmicInterface<V,R> {
    String algorithmName();
    R start(Graph<V> graph);
}
