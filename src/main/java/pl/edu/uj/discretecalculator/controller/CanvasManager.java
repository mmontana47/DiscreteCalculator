package pl.edu.uj.discretecalculator.controller;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CanvasManager {
    private final Pane graphPane;
    private final Label countsLabel;
    private final List<VertexDrawn> vertices = new ArrayList<>();
    private final List<EdgeDrawn> edges = new ArrayList<>();

    public CanvasManager(Pane graphPane, Label countsLabel) {
        this.graphPane = graphPane;
        this.countsLabel = countsLabel;
        updateCounts();
    }

    public VertexDrawn addVertex(double x, double y, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag) {
        VertexDrawn vertex = new VertexDrawn(x, y, String.valueOf(vertices.size()), onClick, canDrag);
        vertices.add(vertex);
        graphPane.getChildren().add(vertex);
        updateCounts();
        return vertex;
    }

    public EdgeDrawn addEdge(VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick) {
        EdgeDrawn edge = new EdgeDrawn(source, target, onClick);
        edges.add(edge);
        graphPane.getChildren().addFirst(edge);
        updateCounts();
        return edge;
    }

    public void removeVertex(VertexDrawn vertex) {
        List<EdgeDrawn> toDelete = new ArrayList<>();
        for (EdgeDrawn e : edges) {
            if (e.getSource() == vertex || e.getTarget() == vertex) toDelete.add(e);
        }
        for (EdgeDrawn e : toDelete) removeEdgeInternal(e);
        vertices.remove(vertex);
        graphPane.getChildren().remove(vertex);
        updateCounts();
    }

    public void removeEdge(EdgeDrawn edge) {
        removeEdgeInternal(edge);
        updateCounts();
    }

    private void removeEdgeInternal(EdgeDrawn edge) {
        edges.remove(edge);
        graphPane.getChildren().remove(edge);
    }

    public boolean edgeExists(VertexDrawn a, VertexDrawn b) {
        for (EdgeDrawn e : edges) {
            if (e.getSource() == a && e.getTarget() == b) return true;
            if (e.getSource() == b && e.getTarget() == a) return true;
        }
        return false;
    }

    private void updateCounts() {
        countsLabel.setText("V: " + vertices.size() + "\t E: " + edges.size());
    }

    public void clear(){
        graphPane.getChildren().clear();
        vertices.clear();
        edges.clear();
        updateCounts();
    }
}
