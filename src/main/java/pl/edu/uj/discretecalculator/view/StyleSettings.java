package pl.edu.uj.discretecalculator.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import pl.edu.uj.discretecalculator.AppConfig;

public final class StyleSettings {
    public static double MIN_VERTEX_RADIUS;
    public static double MAX_VERTEX_RADIUS;
    public static double DEFAULT_VERTEX_RADIUS;

    public static double MIN_EDGE_WIDTH;
    public static double MAX_EDGE_WIDTH;
    public static double DEFAULT_EDGE_WIDTH;

    private static final StyleSettings INSTANCE = new StyleSettings();
    public static StyleSettings get() { return INSTANCE; }

    private final DoubleProperty vertexRadius;
    private final DoubleProperty edgeWidth;

    private StyleSettings() {
        AppConfig.VertexStyleCfg v = AppConfig.get().style.vertex;
        AppConfig.EdgeStyleCfg e = AppConfig.get().style.edge;
        MIN_VERTEX_RADIUS = v.radiusMin;
        MAX_VERTEX_RADIUS = v.radiusMax;
        DEFAULT_VERTEX_RADIUS = v.radiusDefault;
        MIN_EDGE_WIDTH = e.widthMin;
        MAX_EDGE_WIDTH = e.widthMax;
        DEFAULT_EDGE_WIDTH = e.widthDefault;
        vertexRadius = new SimpleDoubleProperty(DEFAULT_VERTEX_RADIUS);
        edgeWidth = new SimpleDoubleProperty(DEFAULT_EDGE_WIDTH);
    }

    public DoubleProperty vertexRadiusProperty() { return vertexRadius; }
    public DoubleProperty edgeWidthProperty()    { return edgeWidth; }

    public double getVertexRadius() { return vertexRadius.get(); }
    public double getEdgeWidth()    { return edgeWidth.get(); }

    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int) (c.getRed()   * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue()  * 255));
    }
}
