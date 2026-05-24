//interface
package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.Graph;

public interface AlgorithmicInterface<V,R> {
    //returns name of the algorithm
    String algorithmName();
    //starts the algorithm
    R start(Graph<V> graph);
}
