package pl.edu.uj.discretecalculator.view;

import javafx.css.PseudoClass;
import javafx.scene.shape.Line;

import java.util.function.Consumer;

public class EdgeDrawn extends Line {
    private final VertexDrawn source;
    private final VertexDrawn target;

    private static final PseudoClass TREE_EDGE  = PseudoClass.getPseudoClass("tree-edge");
    private static final PseudoClass CYCLE_EDGE = PseudoClass.getPseudoClass("cycle-edge");

    public EdgeDrawn(VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick) {
        this.source = source;
        this.target = target;

        startXProperty().bind(source.layoutXProperty().add(VertexDrawn.circleRadius));
        startYProperty().bind(source.layoutYProperty().add(VertexDrawn.circleRadius));
        endXProperty().bind(target.layoutXProperty().add(VertexDrawn.circleRadius));
        endYProperty().bind(target.layoutYProperty().add(VertexDrawn.circleRadius));

        getStyleClass().add("edge");

        this.setOnMouseClicked(event -> onClick.accept(this));
    }

    public VertexDrawn getSource() { return source; }
    public VertexDrawn getTarget() { return target; }

    public void highlightAsTreeEdge() {
        pseudoClassStateChanged(TREE_EDGE, true);
        pseudoClassStateChanged(CYCLE_EDGE, false);
    }

    public void highlightAsCycle() {
        pseudoClassStateChanged(CYCLE_EDGE, true);
        pseudoClassStateChanged(TREE_EDGE, false);
    }

    public void resetStyle() {
        pseudoClassStateChanged(TREE_EDGE, false);
        pseudoClassStateChanged(CYCLE_EDGE, false);
    }
}
