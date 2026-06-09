package pl.edu.uj.discretecalculator.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;
import pl.edu.uj.discretecalculator.AppConfig;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CanvasManager {
    private final Pane graphPane;
    private final Label countsLabel;
    private final List<VertexDrawn> vertices = new ArrayList<>();
    private final List<EdgeDrawn> edges = new ArrayList<>();
    private final BooleanProperty directed = new SimpleBooleanProperty(false);
    private final BooleanProperty weighted = new SimpleBooleanProperty(false);
    public CanvasManager(Pane graphPane, Label countsLabel) {
        this.graphPane = graphPane;
        this.countsLabel = countsLabel;
        updateCounts();


        directed.addListener((observable, oldValue, newValue) -> {
            if(!isDirected()) {
                for(EdgeDrawn edge : edges) {
                    EdgeDrawn reverse = findEdge(edge.getTarget(), edge.getSource());
                    if(reverse != null && edge.getEdgeId().compareTo(reverse.getEdgeId())>0) {
                        reverse.setVisible(false);
                        edge.setOffset(0);
                    }
                }
            }
            else{
                edges.forEach(e -> e.setVisible(true));
                for(EdgeDrawn edge : edges) {
                    EdgeDrawn reverse = findEdge(edge.getTarget(), edge.getSource());
                    if(reverse != null && edge.getEdgeId().compareTo(reverse.getEdgeId())>0) {
                        double off = AppConfig.get().style.edge.bidirectionalOffset;
                        edge.setOffset(off);
                        reverse.setOffset(off);
                    }
                }
            }
        });
    }

    public VertexDrawn getVertexById(String id) {
        for (VertexDrawn v : vertices) {
            if (v.getVertexId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    public EdgeDrawn getEdgeById(String id) {
        for (EdgeDrawn e : edges) {
            if (e.getEdgeId().equals(id))
                return e;
        }

        return null;
    }

    public Pane getGraphPane() { return graphPane; }

    public List<VertexDrawn> getVertices() { return Collections.unmodifiableList(vertices); }
    public List<EdgeDrawn> getEdges() { return Collections.unmodifiableList(edges); }
    public BooleanProperty directedProperty() { return directed; }
    public boolean isDirected() { return directed.get(); }
    public void setDirected(boolean value) { directed.set(value); }
    public BooleanProperty weightedProperty() { return weighted; }

    public VertexDrawn createVertex(double x, double y, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag) {
        VertexDrawn vertex = new VertexDrawn(x, y, nextAvailableId(), onClick, canDrag);
        attachVertex(vertex);
        return vertex;
    }

    public VertexDrawn createVertex(double x, double y, String id, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag) {
        VertexDrawn vertex = new VertexDrawn(x, y, id, onClick, canDrag);
        attachVertex(vertex);
        return vertex;
    }

    public EdgeDrawn createEdge(VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick) {
        EdgeDrawn edge = new EdgeDrawn(String.valueOf(edges.size()), source, target, onClick, directed);
        edge.bindWeightVisibility(this.weighted);
        if (weighted.get()) edge.setWeightText("1.0");
        attachEdge(edge);
        return edge;
    }

    public EdgeDrawn createEdge(String id, VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick) {
        EdgeDrawn edge = new EdgeDrawn(id, source, target, onClick, directed);
        edge.bindWeightVisibility(this.weighted);
        if (weighted.get()) edge.setWeightText("1.0");
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

    //nie dziala jesli dane z klawiatury nie sa spojne
    public void renumber() {
        for (int i = 0; i < vertices.size(); i++) {
            vertices.get(i).setVertexId(String.valueOf(i));
        }
    }

    private String nextAvailableId(){
        int i=0;
        while (getVertexById(String.valueOf(i))!=null) i++;
        return String.valueOf(i);
    }

    public void sortVerticesById() {
        vertices.sort((v1, v2) -> {
            try {
                return Integer.compare(Integer.parseInt(v1.getVertexId()), Integer.parseInt(v2.getVertexId()));
            } catch (NumberFormatException e) {
                // Safe-fallback, gdyby któryś wierzchołek miał jeszcze dopisek "_wait" lub "_temp"
                return v1.getVertexId().compareTo(v2.getVertexId());
            }
        });
    }

    public void attachEdge(EdgeDrawn e) {
        edges.add(e);
        graphPane.getChildren().addFirst(e);
        updateCounts();
        updateCurveOffsets(e);
    }

    public void detachEdge(EdgeDrawn e) {
        edges.remove(e);
        graphPane.getChildren().remove(e);
        updateCounts();
        updateCurveOffsets(e);
    }

    public List<EdgeDrawn> incidentEdges(VertexDrawn v) {
        List<EdgeDrawn> result = new ArrayList<>();
        for (EdgeDrawn e : edges) {
            if (e.getSource() == v || e.getTarget() == v) result.add(e);
        }
        return result;
    }

    public EdgeDrawn findEdge(VertexDrawn a, VertexDrawn b) {
        for (EdgeDrawn e : edges) {
            if (e.getSource() == a && e.getTarget() == b) return e;
        }
        return null;
    }

    public boolean edgeExists(VertexDrawn a, VertexDrawn b) {
        for (EdgeDrawn e : edges) {
            if (e.getSource() == a && e.getTarget() == b) return true;
            if (e.getSource() == b && e.getTarget() == a) return true;
        }
        return false;
    }

    public boolean edgeExistsDirected(VertexDrawn a, VertexDrawn b) {
        for (EdgeDrawn e : edges) {
            if (e.getSource() == a && e.getTarget() == b) return true;
        }
        return false;
    }

    private void updateCurveOffsets(EdgeDrawn e) {
        EdgeDrawn reverse = null;
        for (EdgeDrawn edge : edges) {
            if (edge.getSource() == e.getTarget() && edge.getTarget() == e.getSource()) {
                reverse = edge;
                break;
            }
        }
        if (reverse == null) return;
        if (edges.contains(e)) {
            double off = AppConfig.get().style.edge.bidirectionalOffset;
            e.setOffset(off);
            reverse.setOffset(off);
        } else {
            reverse.setOffset(0);
        }
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

    public void resetAllStyles() {
        for (VertexDrawn v : vertices) {
            v.resetStyle();
        }
        for (EdgeDrawn e : edges) {
            e.resetStyle();
        }
    }
}
