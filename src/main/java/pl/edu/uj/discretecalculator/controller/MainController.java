package pl.edu.uj.discretecalculator.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.*;

public class MainController{
    Integer VertexCount=0;
    Integer EdgeCount=0;

    List<VertexDrawn> vertices = new ArrayList<>();
    List<EdgeDrawn> edges = new ArrayList<>();

    VertexDrawn source = null;

    @FXML private Pane graphPane;
    @FXML private ToggleGroup modeGroup;
    @FXML private Label modeLabel;
    @FXML private Label hintLabel;
    @FXML private Label countsLabel;

    @FXML
    private void initialize(){
        modeGroup.selectedToggleProperty().addListener(
                ((observable, oldValue, newValue) ->
                {
                    if(source!=null){
                        source.unselect();
                        source=null;
                    }
                    if(newValue==null){
                        modeLabel.setText("Mode: -");
                        hintLabel.setText("Select a mode to start");
                    }
                    else{
                        ToggleButton btn= (ToggleButton)newValue;
                        modeLabel.setText("Mode: "+btn.getText());
                        hintLabel.setText(setHint(btn.getText()));
                    }
                })
        );
        graphPane.setOnMouseClicked(this::onPaneClick);
    }

    private String setHint(String mode){
        return switch (mode){
            case "Add Vertex" -> "Click to add a vertex";
            case "Add Edge" -> "Click two vertices to add an edge";
            case "Delete" -> "Click a vertex or an edge to delete it";
            case "Move" -> "Click and drag a vertex to change its position";
            default -> "";
        };
    }

    private void onPaneClick(MouseEvent e){
        double x = e.getX();
        double y = e.getY();

        if(Objects.equals(currentMode(), "Add Vertex")){
            VertexDrawn vertex = new
                    VertexDrawn(x,y,VertexCount.toString(), this::onVertexClick, ()->(Objects.equals(currentMode(), "Move")));
            vertices.add(vertex);
            graphPane.getChildren().add(vertex);
            updateCounts(++VertexCount, EdgeCount);
        }
        else if(Objects.equals(currentMode(), "Add Edge") && source!=null){
            source.unselect();
            source=null;
        }
    }

    private void onVertexClick(VertexDrawn vertex){
        switch (currentMode()){
            case "Add Edge" ->{
                if(source==null){
                    source=vertex;
                    vertex.select();
                }
                else if (source == vertex || isEdge(source, vertex)){
                    source.unselect();
                    source=null;
                }
                else{
                    EdgeDrawn edge = new EdgeDrawn(source, vertex, this::onEdgeClick);
                    edges.add(edge);
                    graphPane.getChildren().addFirst(edge);
                    source.unselect();
                    source=null;
                    updateCounts(VertexCount, ++EdgeCount);
                }
            }
            case "Delete" ->{
                List <EdgeDrawn> toDelete = new ArrayList<>();
                for(EdgeDrawn e : edges){
                    if(e.getSource() == vertex || e.getTarget() == vertex) toDelete.add(e);
                }
                for(EdgeDrawn e: toDelete) deleteEdge(e);
                vertices.remove(vertex);
                graphPane.getChildren().remove(vertex);
                updateCounts(--VertexCount, EdgeCount);
            }
            case null -> {}
            default-> {}
        }
    }

    private void onEdgeClick(EdgeDrawn edge){
        switch (currentMode()){
            case "Delete" -> {
                deleteEdge(edge);
                updateCounts(VertexCount, EdgeCount);
            }
            case null -> {}
            default -> {}
        }
    }

    private void deleteEdge(EdgeDrawn edge){
        edges.remove(edge);
        EdgeCount--;
        graphPane.getChildren().remove(edge);
    }

    private boolean isEdge(VertexDrawn vertex1, VertexDrawn vertex2) {
        for(EdgeDrawn e: edges){
            if(e.getSource() == vertex1 && e.getTarget() == vertex2) return true;
            if(e.getSource() == vertex2 && e.getTarget() == vertex1) return true;
        }
        return false;
    }

    private void updateCounts(int vCount, int eCount){
        countsLabel.setText("V: " + vCount + "\t E: " + eCount);
    }

    private String currentMode(){
        Toggle tog = modeGroup.getSelectedToggle();
        if(tog==null) return null;
        return ((ToggleButton) tog).getText();
    }
}