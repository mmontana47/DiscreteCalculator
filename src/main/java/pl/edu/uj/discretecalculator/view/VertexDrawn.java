package pl.edu.uj.discretecalculator.view;

import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class VertexDrawn extends StackPane {
    private String id;
    public static final DoubleProperty circleRadius = StyleSettings.get().vertexRadiusProperty();
    private double mouseX, mouseY;
    private boolean wasDragged=false;
    private boolean pinned = false;
    private final Circle circle;
    private final Label label;
    private Point2D displacement;

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass BFS_VISITED = PseudoClass.getPseudoClass("bfs-visited");
    private static final PseudoClass DFS_VISITED = PseudoClass.getPseudoClass("dfs-visited");

    public VertexDrawn(double x, double y, String id, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag){
        this.id = id;
        this.circle = new Circle(x, y, circleRadius.get());
        this.displacement = Point2D.ZERO;
        circle.radiusProperty().bind(circleRadius);

        this.label = new Label(id);

        this.getStyleClass().add("vertex");
        this.circle.getStyleClass().add("vertex-circle");
        this.label.getStyleClass().add("vertex-label");

        this.getChildren().addAll(circle, label);
        this.setLayoutX(x-StyleSettings.get().getVertexRadius());
        this.setLayoutY(y-StyleSettings.get().getVertexRadius());

        this.setOnMousePressed(event -> {
            wasDragged=false;
            if(!canDrag.getAsBoolean()) return;
            mouseX = event.getSceneX() - this.getLayoutX();
            mouseY = event.getSceneY() - this.getLayoutY();
            pinned = true;

            this.toFront();
            event.consume();
        });

        this.setOnMouseReleased(event -> {
            pinned = false;
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
    public void setVertexId(String id) {
        this.id = id;
        this.label.setText(id);
    }

    public void markVisited(String algorithmType) {
        pseudoClassStateChanged(BFS_VISITED, "BFS".equals(algorithmType));
        pseudoClassStateChanged(DFS_VISITED, "DFS".equals(algorithmType));
    }

    public void addDisplacement(Point2D point) {this.displacement = this.displacement.add(point);}

    public void removeDisplacement(Point2D point) {this.displacement = this.displacement.subtract(point);}

    public void resetDisplacement() {this.displacement = Point2D.ZERO;}

    public Point2D getDisplacement() {return displacement;}

    public boolean isPinned() {return pinned;}

    public void select() {
        pseudoClassStateChanged(SELECTED, true);
    }

    public void unselect() {
        pseudoClassStateChanged(SELECTED, false);
    }

    public void resetStyle() {
        pseudoClassStateChanged(BFS_VISITED, false);
        pseudoClassStateChanged(DFS_VISITED, false);
        pseudoClassStateChanged(SELECTED, false);
    }
}
