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

import java.util.Objects;

public class MainController{
    private CanvasManager canvas;
    private VertexDrawn source = null;

    @FXML private Pane graphPane;
    @FXML private ToggleGroup modeGroup;
    @FXML private Label modeLabel;
    @FXML private Label hintLabel;
    @FXML private Label countsLabel;

    @FXML
    private void initialize(){
        canvas = new CanvasManager(graphPane, countsLabel);

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
    @FXML
    private void newGraph(){
        if(source!=null){
            source.unselect();
            source=null;
        }
        canvas.clear();
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
        if(Objects.equals(currentMode(), "Add Vertex")){
            canvas.addVertex(e.getX(), e.getY(),
                    this::onVertexClick,
                    () -> Objects.equals(currentMode(), "Move"));
        }
        else if(Objects.equals(currentMode(), "Add Edge") && source!=null){
            source.unselect();
            source=null;
        }
    }

    private void onVertexClick(VertexDrawn vertex){
        switch (currentMode()){
            case "Add Edge" -> {
                if(source==null){
                    source=vertex;
                    vertex.select();
                }
                else if (source == vertex || canvas.edgeExists(source, vertex)){
                    source.unselect();
                    source=null;
                }
                else{
                    canvas.addEdge(source, vertex, this::onEdgeClick);
                    source.unselect();
                    source=null;
                }
            }
            case "Delete" -> canvas.removeVertex(vertex);
            case null -> {}
            default -> {}
        }
    }

    private void onEdgeClick(EdgeDrawn edge){
        switch (currentMode()){
            case "Delete" -> canvas.removeEdge(edge);
            case null -> {}
            default -> {}
        }
    }

    private String currentMode(){
        Toggle tog = modeGroup.getSelectedToggle();
        if(tog==null) return null;
        return ((ToggleButton) tog).getText();
    }
}
