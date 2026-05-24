package pl.edu.uj.discretecalculator.controller;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.ArrayList;
import java.util.Collections;
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

    public Pane getGraphPane() { return graphPane; }

    public List<VertexDrawn> getVertices() { return Collections.unmodifiableList(vertices); }
    public List<EdgeDrawn> getEdges() { return Collections.unmodifiableList(edges); }

    public VertexDrawn createVertex(double x, double y, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag) {
        VertexDrawn vertex = new VertexDrawn(x, y, String.valueOf(vertices.size()), onClick, canDrag);
        attachVertex(vertex);
        return vertex;
    }

    public VertexDrawn createVertex(double x, double y, String id, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag) {
        VertexDrawn vertex = new VertexDrawn(x, y, id, onClick, canDrag);
        attachVertex(vertex);
        return vertex;
    }

    public EdgeDrawn createEdge(VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick) {
        EdgeDrawn edge = new EdgeDrawn(source, target, onClick);
        attachEdge(edge);
        return edge;
    }

    public void attachVertex(VertexDrawn v) {
        vertices.add(v);
        graphPane.getChildren().add(v);
        updateCounts();
    }

    public void attachVertexAt(int index, VertexDrawn v) {
        vertices.add(index, v);
        graphPane.getChildren().add(v);
        updateCounts();
    }

    public void detachVertex(VertexDrawn v) {
        vertices.remove(v);
        graphPane.getChildren().remove(v);
        updateCounts();
    }

    public void renumber() {
        for (int i = 0; i < vertices.size(); i++) {
            vertices.get(i).setVertexId(String.valueOf(i));
        }
    }

    public void attachEdge(EdgeDrawn e) {
        edges.add(e);
        graphPane.getChildren().addFirst(e);
        updateCounts();
    }

    public void detachEdge(EdgeDrawn e) {
        edges.remove(e);
        graphPane.getChildren().remove(e);
        updateCounts();
    }

    public List<EdgeDrawn> incidentEdges(VertexDrawn v) {
        List<EdgeDrawn> result = new ArrayList<>();
        for (EdgeDrawn e : edges) {
            if (e.getSource() == v || e.getTarget() == v) result.add(e);
        }
        return result;
    }

    public boolean edgeExists(VertexDrawn a, VertexDrawn b) {
        for (EdgeDrawn e : edges) {
            if (e.getSource() == a && e.getTarget() == b) return true;
            if (e.getSource() == b && e.getTarget() == a) return true;
        }
        return false;
    }

    public void clear() {
        graphPane.getChildren().clear();
        vertices.clear();
        edges.clear();
        updateCounts();
    }

    private void updateCounts() {
        countsLabel.setText("V: " + vertices.size() + "\t E: " + edges.size());
    }
}
