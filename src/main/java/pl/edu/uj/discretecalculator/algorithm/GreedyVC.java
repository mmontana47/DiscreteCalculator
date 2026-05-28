package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

import java.util.*;

public class GreedyVC<V> implements AlgorithmicInterface<V, GreedyVCResult<V>> {

    @Override
    public String algorithmName() {
        return "Algorytm Zachłanny Kolorowania (Greedy Vertex Coloring)";
    }

    @Override
    public GreedyVCResult<V> start(Graph<V> graph) {
        List<Vertex<V>> vertices = graph.getVertices();

        // Sortujemy wierzchołki po ID, aby algorytm był deterministyczny
        // (zawsze dawał ten sam wynik dla tego samego grafu)
        vertices.sort(Comparator.comparingInt(Vertex::getId));

        List<Vertex<V>> coloringOrder = new ArrayList<>();
        Map<Vertex<V>, Integer> colors = new LinkedHashMap<>();
        int maxColorUsed = 0;

        // Inicjalizacja: wszystkie węzły otrzymują domyślny kolor 0
        for (Vertex<V> v : vertices) {
            colors.put(v, 0);
        }

        // Główna pętla algorytmu zachłannego
        for (Vertex<V> vertex : vertices) {
            // Zbieramy kolory, które zostały już użyte przez pokolorowanych sąsiadów
            Set<Integer> neighborColors = new HashSet<>();
            for (Vertex<V> neighbor : graph.getNeighbors(vertex)) {
                Integer neighborColor = colors.get(neighbor);
                if (neighborColor != null && neighborColor > 0) {
                    neighborColors.add(neighborColor);
                }
            }

            // Szukamy najmniejszego wolnego koloru (od 1 w górę)
            int colorToAssign = 1;
            while (neighborColors.contains(colorToAssign)) {
                colorToAssign++;
            }

            // Przypisujemy znaleziony kolor i zapisujemy krok
            colors.put(vertex, colorToAssign);
            coloringOrder.add(vertex);

            if (colorToAssign > maxColorUsed) {
                maxColorUsed = colorToAssign;
            }
        }

        return new GreedyVCResult<>(coloringOrder, colors, maxColorUsed);
    }
}