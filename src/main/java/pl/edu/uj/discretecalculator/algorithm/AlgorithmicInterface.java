//interface
package pl.edu.uj.discretecalculator.algorithm;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import pl.edu.uj.discretecalculator.model.graph.Edge;
public interface AlgorithmicInterface<V,E> {
    //returns name of the algorithm
    String algorithmName();
    //starts the algorithm
    void start(Graph<V,E> graph,Vertex<V> start);
}
