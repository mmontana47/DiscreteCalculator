package pl.edu.uj.discretecalculator.view;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class VertexDrawn extends StackPane {
    public static final int circleRadius=20;
    private static final String SELECTED_CLASS = "selected";

    private double mouseX, mouseY;
    private boolean wasDragged=false;
    private final Circle circle;
    private final Label label;
    private String id;

    public VertexDrawn(double x, double y, String id, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag){
        this.id = id;
        this.circle = new Circle(x, y, circleRadius);
        circle.getStyleClass().add("vertex-circle");

        this.label = new Label(id);
        label.getStyleClass().add("vertex-label");

        this.getStyleClass().add("vertex");
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

    public void setVertexId(String newId) {
        this.id = newId;
        this.label.setText(newId);
    }

    public void select(){
        if (!getStyleClass().contains(SELECTED_CLASS)) {
            getStyleClass().add(SELECTED_CLASS);
        }
    }

    public void unselect(){
        getStyleClass().remove(SELECTED_CLASS);
    }

}
