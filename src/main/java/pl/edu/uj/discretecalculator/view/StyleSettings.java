package pl.edu.uj.discretecalculator.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public final class StyleSettings {
    public static final double MIN_VERTEX_RADIUS = 10.0;
    public static final double MAX_VERTEX_RADIUS = 30.0;
    public static final double DEFAULT_VERTEX_RADIUS = 20.0;

    public static final double MIN_EDGE_WIDTH = 0.5;
    public static final double MAX_EDGE_WIDTH = 4.0;
    public static final double DEFAULT_EDGE_WIDTH = 2.0;

    private static final StyleSettings INSTANCE = new StyleSettings();
    public static StyleSettings get() { return INSTANCE; }

    private final DoubleProperty vertexRadius = new SimpleDoubleProperty(DEFAULT_VERTEX_RADIUS);
    private final DoubleProperty edgeWidth = new SimpleDoubleProperty(DEFAULT_EDGE_WIDTH);

    private StyleSettings() {}

    public DoubleProperty vertexRadiusProperty() { return vertexRadius; }
    public DoubleProperty edgeWidthProperty() { return edgeWidth; }

    public double getVertexRadius() { return vertexRadius.get(); }
    public double getEdgeWidth() { return edgeWidth.get(); }
}
