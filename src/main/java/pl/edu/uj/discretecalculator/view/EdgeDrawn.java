package pl.edu.uj.discretecalculator.view;

import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.function.Consumer;

public class EdgeDrawn extends Group {
    private final String id;
    private final VertexDrawn source;
    private final VertexDrawn target;

    private final Line line;
    private final Label weightLabel;

    private static final PseudoClass TREE_EDGE  = PseudoClass.getPseudoClass("tree-edge");
    private static final PseudoClass CYCLE_EDGE = PseudoClass.getPseudoClass("cycle-edge");

    private String userStrokeHex = null;

    public EdgeDrawn(String id, VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick) {
        this.id = id;
        this.source = source;
        this.target = target;

        this.line = new Line();
        this.weightLabel = new Label();

        this.line.getStyleClass().add("edge");
        this.weightLabel.getStyleClass().add("edge-label");

        line.startXProperty().bind(source.layoutXProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.startYProperty().bind(source.layoutYProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.endXProperty().bind(target.layoutXProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.endYProperty().bind(target.layoutYProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.setStrokeWidth(StyleSettings.get().getEdgeWidth());
        StyleSettings.get().edgeWidthProperty().addListener((obs, old, w) -> line.setStrokeWidth(w.doubleValue()));

        weightLabel.layoutXProperty().bind(
                line.startXProperty().add(line.endXProperty()).divide(2)
        );
        weightLabel.layoutYProperty().bind(
                line.startYProperty().add(line.endYProperty()).divide(2)
        );

        this.getChildren().addAll(line, weightLabel);

        this.setOnMouseClicked(event -> {
            event.consume();
            if (onClick != null) {
                onClick.accept(this);
            }
        });
    }

    public String getEdgeId() { return id; }
    public VertexDrawn getSource() { return source; }
    public VertexDrawn getTarget() { return target; }

    private void applyStroke(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty()) {
            line.setStyle("-fx-stroke: " + hexColor + ";");
        } else if (userStrokeHex != null) {
            line.setStyle("-fx-stroke: " + userStrokeHex + ";");
        } else {
            line.setStyle("");
        }
    }

    public void setUserStrokeColor(Color c) {
        userStrokeHex = (c != null) ? StyleSettings.toHex(c) : null;
        applyStroke(null);
    }

    public Color getUserStrokeColor() {
        return userStrokeHex != null ? Color.web(userStrokeHex) : null;
    }

    public void setStrokeColor(String hexColor) {
        applyStroke(hexColor);
    }

    public void setWeightText(String text) {
        weightLabel.setText(text == null ? "" : text);
    }

    public void highlightAsTreeEdge() {
        line.pseudoClassStateChanged(TREE_EDGE, true);
        line.pseudoClassStateChanged(CYCLE_EDGE, false);
        line.setStyle("-fx-stroke: #1a56db; -fx-stroke-width: 3;");
    }

    public void highlightAsCycle() {
        line.pseudoClassStateChanged(CYCLE_EDGE, true);
        line.pseudoClassStateChanged(TREE_EDGE, false);
        line.setStyle("-fx-stroke: #9ca3af; -fx-stroke-width: 2; -fx-stroke-dash-array: 10 10;");
    }

    public void resetStyle() {
        line.pseudoClassStateChanged(TREE_EDGE, false);
        line.pseudoClassStateChanged(CYCLE_EDGE, false);
        applyStroke(null);
        setWeightText(null);
    }
}