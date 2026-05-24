package pl.edu.uj.discretecalculator.view;

import javafx.scene.shape.Line;

import javafx.scene.paint.Color;
import java.util.function.Consumer;

public class EdgeDrawn extends Line {
    private final VertexDrawn source;
    private final VertexDrawn target;

    public EdgeDrawn(VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick) {
        this.source=source;
        this.target=target;
        this.setStroke(Color.GRAY);

        startXProperty().bind(source.layoutXProperty().add(VertexDrawn.circleRadius));
        startYProperty().bind(source.layoutYProperty().add(VertexDrawn.circleRadius));
        endXProperty().bind(target.layoutXProperty().add(VertexDrawn.circleRadius));
        endYProperty().bind(target.layoutYProperty().add(VertexDrawn.circleRadius));

        //getStyleClass().add("edge")// na razie zakomentowałem - psuje mi animację

        this.setOnMouseClicked(event -> {
            onClick.accept(this);
        });
    }

    public VertexDrawn getSource() { return source;}
    public VertexDrawn getTarget() { return target;}

    public void highlightAsTreeEdge() {
        this.setStroke(Color.BLACK);
        this.setStrokeWidth(3);
    }

    public void highlightAsCycle() {
        this.setStroke(Color.LIGHTGRAY);
        this.getStrokeDashArray().setAll(10d, 10d);
    }

    public void resetStyle() {
        this.setStroke(Color.GRAY);
        this.getStrokeDashArray().clear();
        this.setStrokeWidth(1);
    }
}
