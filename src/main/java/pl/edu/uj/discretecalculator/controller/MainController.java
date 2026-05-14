package pl.edu.uj.discretecalculator.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Objects;

public class MainController{
    final int circleRadius = 20;
    Integer VertexCount=0;
    Integer EdgeCount=0;

    @FXML private Pane graphPane;
    @FXML private ToggleGroup modeGroup;
    @FXML private Label modeLabel;
    @FXML private Label hintLabel;
    @FXML private Label countsLabel;

    private class VertexDrawn extends StackPane {
        private double mouseX, mouseY;

        public VertexDrawn(double x, double y, String id){
            Circle circle = new Circle(x, y, circleRadius, Color.WHITE);
            circle.setStroke(Color.BLACK);
            Label label = new Label(id);
            countsLabel.setText("V: "+(++VertexCount)+  "\t E: " + (EdgeCount));
            this.getChildren().addAll(circle, label);

            this.setLayoutX(x-circleRadius);
            this.setLayoutY(y-circleRadius);

            this.setOnMousePressed(event -> {
                mouseX = event.getSceneX() - this.getLayoutX();
                mouseY = event.getSceneY() - this.getLayoutY();
                this.toFront();
            });

            this.setOnMouseDragged(event -> {
                this.setLayoutX(event.getSceneX() - mouseX);
                this.setLayoutY(event.getSceneY() - mouseY);

                this.setOnMouseClicked(Event::consume);
            });
        }
    }

    @FXML
    private void initialize(){
        modeGroup.selectedToggleProperty().addListener(
                ((observable, oldValue, newValue) ->
                {
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
            VertexDrawn vertex = new VertexDrawn(x,y,VertexCount.toString());
            graphPane.getChildren().add(vertex);
        }
        
    }

    private String currentMode(){
        Toggle tog = modeGroup.getSelectedToggle();
        if(tog==null) return null;
        return ((ToggleButton) tog).getText();
    }
}