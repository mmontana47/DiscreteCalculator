package pl.edu.uj.discretecalculator.algorithm;

import pl.edu.uj.discretecalculator.model.graph.Vertex;
import java.util.List;
import java.util.Map;

public class GreedyVCResult<V> {
    private final List<Vertex<V>> coloringOrder;
    private final Map<Vertex<V>, Integer> assignedColors;
    private final int chromaticNumberUpperBound;

    public GreedyVCResult(List<Vertex<V>> coloringOrder, Map<Vertex<V>, Integer> assignedColors, int chromaticNumberUpperBound) {
        this.coloringOrder = coloringOrder;
        this.assignedColors = assignedColors;
        this.chromaticNumberUpperBound = chromaticNumberUpperBound;
    }

    /**
     * Zwraca listę wierzchołków w kolejności ich kolorowania.
     */
    public List<Vertex<V>> getColoringOrder() {
        return coloringOrder;
    }

    /**
     * Zwraca mapę powiązań: Wierzchołek -> Numer koloru (1, 2, 3...).
     * Przed pokolorowaniem wierzchołki miały domyślny kolor 0.
     */
    public Map<Vertex<V>, Integer> getAssignedColors() {
        return assignedColors;
    }

    /**
     * Zwraca maksymalną liczbę użytych kolorów (górne oszacowanie liczby chromatycznej).
     */
    public int getChromaticNumberUpperBound() {
        return chromaticNumberUpperBound;
    }
}