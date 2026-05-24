package pl.edu.uj.discretecalculator.view;

import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class VertexDrawn extends StackPane {
    private String id;
    public static final int circleRadius=20;
    private double mouseX, mouseY;
    private boolean wasDragged=false;
    private final Circle circle;

    public VertexDrawn(double x, double y, String id, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag){
        this.id = id;
        this.circle = new Circle(x, y, circleRadius, Color.WHITE);
        circle.setStroke(Color.BLACK);
        Label label = new Label(id);

        this.getChildren().addAll(circle, label);
        this.setLayoutX(x-circleRadius);
        this.setLayoutY(y-circleRadius);

        this.setOnMousePressed(event -> {
            wasDragged=false;
            if(!canDrag.getAsBoolean()) return;
            mouseX = event.getSceneX() - this.getLayoutX();
            mouseY = event.getSceneY() - this.getLayoutY();

            this.toFront();
            event.consume();
        });

        this.setOnMouseClicked(event -> {
            if(!wasDragged)
                onClick.accept(this);
            event.consume();
        });

        this.setOnMouseDragged(event -> {
            if(!canDrag.getAsBoolean()) return;
            wasDragged = true;
            this.setLayoutX(event.getSceneX() - mouseX);
            this.setLayoutY(event.getSceneY() - mouseY);
            event.consume();
        });
    }

    public String getVertexId() { return id; }
    public void setVertexId(String id) {this.id = id;}

    public void highlightForAlgorithm(Color color) {
        circle.setFill(color);
    }

    public void select(){
        circle.setStroke(Color.YELLOW);
        circle.setStrokeWidth(3);
    }

    public void unselect(){
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1);
    }

    public void resetStyle() {
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1);
    }

}
