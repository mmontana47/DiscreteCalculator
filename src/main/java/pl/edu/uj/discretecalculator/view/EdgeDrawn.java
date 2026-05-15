package pl.edu.uj.discretecalculator.view;

import javafx.scene.shape.Line;

import java.util.function.Consumer;

public class EdgeDrawn extends Line {
    private final VertexDrawn source;
    private final VertexDrawn target;

    public EdgeDrawn(VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick) {
        this.source=source;
        this.target=target;

        startXProperty().bind(source.layoutXProperty().add(VertexDrawn.circleRadius));
        startYProperty().bind(source.layoutYProperty().add(VertexDrawn.circleRadius));
        endXProperty().bind(target.layoutXProperty().add(VertexDrawn.circleRadius));
        endYProperty().bind(target.layoutYProperty().add(VertexDrawn.circleRadius));

        setStrokeWidth(2);

        this.setOnMouseClicked(event -> {
            onClick.accept(this);
        });
    }

    public VertexDrawn getSource() { return source;}
    public VertexDrawn getTarget() { return target;}
}
