//interface

public interface AlgorithmicInterface<V,E> {
    //returns name of the algorithm
    String algorithmName();
    //starts the algorithm
    void start(Graph<V,E> graph,Vertex<V> start);
}
