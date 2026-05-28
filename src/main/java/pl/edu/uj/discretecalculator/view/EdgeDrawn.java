package pl.edu.uj.discretecalculator.view;

import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.control.Label;
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
        line.strokeWidthProperty().bind(StyleSettings.get().edgeWidthProperty());

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


    public void setStrokeColor(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            line.setStyle("");
        } else {
            line.setStyle("-fx-stroke: " + hexColor + ";");
        }
    }

    public void setWeightText(String text) {
        weightLabel.setText(text == null ? "" : text);
    }


    public void highlightAsTreeEdge() {
        line.pseudoClassStateChanged(TREE_EDGE, true);
        line.pseudoClassStateChanged(CYCLE_EDGE, false);
    }

    public void highlightAsCycle() {
        line.pseudoClassStateChanged(CYCLE_EDGE, true);
        line.pseudoClassStateChanged(TREE_EDGE, false);
    }

    public void resetStyle() {
        line.pseudoClassStateChanged(TREE_EDGE, false);
        line.pseudoClassStateChanged(CYCLE_EDGE, false);
        setStrokeColor(null);
        setWeightText(null);
    }
}