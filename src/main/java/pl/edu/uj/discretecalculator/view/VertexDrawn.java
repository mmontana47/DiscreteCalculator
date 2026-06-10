package pl.edu.uj.discretecalculator.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import pl.edu.uj.discretecalculator.AppConfig;

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
    private final Label distanceLabel;
    private String userFillHex = null;

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass BFS_VISITED = PseudoClass.getPseudoClass("bfs-visited");
    private static final PseudoClass DFS_VISITED = PseudoClass.getPseudoClass("dfs-visited");

    public VertexDrawn(double x, double y, String id, Consumer<VertexDrawn> onClick, BooleanSupplier canDrag) {
        this.id = id;
        this.circle = new Circle(x, y, circleRadius.get());
        this.displacement = Point2D.ZERO;
        circle.radiusProperty().bind(circleRadius);

        this.label = new Label(id);

        // Dynamiczne bindowanie wielkości czcionki proporcjonalnie do promienia (np. 60% promienia)
        this.label.styleProperty().bind(Bindings.createStringBinding(
                () -> "-fx-font-size: " + (circleRadius.get() * 0.6) + "px;",
                circleRadius
        ));

        this.distanceLabel = new Label();
        this.distanceLabel.translateYProperty().bind(circleRadius.add(AppConfig.get().style.vertex.distanceLabelOffset));

        this.getStyleClass().add("vertex");
        this.circle.getStyleClass().add("vertex-circle");
        this.label.getStyleClass().add("vertex-label");
        this.distanceLabel.getStyleClass().add("distance-label");
        this.distanceLabel.setVisible(false);

        this.getChildren().addAll(circle, label, distanceLabel);
        this.setLayoutX(x - StyleSettings.get().getVertexRadius());
        this.setLayoutY(y - StyleSettings.get().getVertexRadius());

        this.setOnMousePressed(event -> {
            wasDragged = false;
            if(!canDrag.getAsBoolean()) return;
            mouseX = event.getSceneX() - this.getLayoutX();
            mouseY = event.getSceneY() - this.getLayoutY();
            pinned = true;

            this.toFront();
            event.consume();
        });

        this.setOnMouseReleased(event ->
        {
            pinned = false;
        });

        this.setOnMouseClicked(event -> {
            if (!wasDragged) onClick.accept(this);
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

    public void setUserFillColor(Color c) {
        userFillHex = (c != null) ? StyleSettings.toHex(c) : null;
        applyFill(userFillHex);
    }

    public Color getUserFillColor() {
        return userFillHex != null ? Color.web(userFillHex) : null;
    }

    private void applyFill(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty()) {
            circle.setStyle("-fx-fill: " + hexColor + ";");
        } else if (userFillHex != null) {
            circle.setStyle("-fx-fill: " + userFillHex + ";");
        } else {
            circle.setStyle("");
        }
    }

    public void setFillColor(String hexColor) {
        applyFill(hexColor);
    }

    public void setBottomLabelText(String text) {
        boolean hasText = text != null && !text.isEmpty();
        distanceLabel.setText(hasText ? text : "");
        distanceLabel.setVisible(hasText);
    }

    public void setX(double x) {
        this.setLayoutX(x - StyleSettings.get().getVertexRadius());
    }

    public void setY(double y) {
        this.setLayoutY(y - StyleSettings.get().getVertexRadius());
    }

    public void markVisited(String algorithmType) {
        pseudoClassStateChanged(BFS_VISITED, "BFS".equals(algorithmType));
        pseudoClassStateChanged(DFS_VISITED, "DFS".equals(algorithmType));
        AppConfig.ColorsCfg c = AppConfig.get().style.colors;
        if ("BFS".equals(algorithmType)) applyFill(c.bfsVisited);
        else if ("DFS".equals(algorithmType)) applyFill(c.dfsVisited);
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
        applyFill(null);
        setBottomLabelText(null);
    }
}