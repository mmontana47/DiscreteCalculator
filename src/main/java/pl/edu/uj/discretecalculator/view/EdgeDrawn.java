package pl.edu.uj.discretecalculator.view;

import javafx.beans.value.ObservableBooleanValue;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;

import java.util.function.Consumer;

public class EdgeDrawn extends Group {
    private final String id;
    private final VertexDrawn source;
    private final VertexDrawn target;

    private final Line line;
    private final Label weightLabel;
    private final Polygon arrowHead;
    private QuadCurve curve;
    private double offset = 0;

    private static final PseudoClass TREE_EDGE  = PseudoClass.getPseudoClass("tree-edge");
    private static final PseudoClass CYCLE_EDGE = PseudoClass.getPseudoClass("cycle-edge");

    private String userStrokeHex = null;

    public EdgeDrawn(String id, VertexDrawn source, VertexDrawn target, Consumer<EdgeDrawn> onClick,
                     ObservableBooleanValue directed) {
        this.id = id;
        this.source = source;
        this.target = target;

        this.line = new Line();
        this.weightLabel = new Label();
        this.arrowHead = new Polygon(0.0, 0.0, -14.0, 6.0, -14.0, -6.0);

        line.startXProperty().bind(source.layoutXProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.startYProperty().bind(source.layoutYProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.endXProperty().bind(target.layoutXProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.endYProperty().bind(target.layoutYProperty().add(StyleSettings.get().vertexRadiusProperty()));

        curve = new QuadCurve();
        curve.getStyleClass().add("edge");
        curve.setFill(Color.TRANSPARENT);
        curve.setStrokeWidth(StyleSettings.get().getEdgeWidth());
        StyleSettings.get().edgeWidthProperty().addListener((obs, old, w) -> curve.setStrokeWidth(w.doubleValue()));

        this.weightLabel.getStyleClass().add("edge-label");
        this.arrowHead.getStyleClass().add("edge-arrow");

        weightLabel.layoutXProperty().bind(
                line.startXProperty().add(line.endXProperty()).divide(2)
        );
        weightLabel.layoutYProperty().bind(
                line.startYProperty().add(line.endYProperty()).divide(2)
        );

        arrowHead.visibleProperty().bind(directed);
        javafx.beans.value.ChangeListener<Number> arrowUpdater = (obs, old, val) -> updateArrowHead();
        line.startXProperty().addListener(arrowUpdater);
        line.startYProperty().addListener(arrowUpdater);
        line.endXProperty().addListener(arrowUpdater);
        line.endYProperty().addListener(arrowUpdater);
        StyleSettings.get().vertexRadiusProperty().addListener((obs, old, val) -> updateArrowHead());

        this.getChildren().addAll(curve, arrowHead);
        updateArrowHead();

        this.setOnMouseClicked(event -> {
            event.consume();
            if (onClick != null) {
                onClick.accept(this);
            }
        });
    }

    public void updateArrowHead() {
        updateCurve();
        double ex = line.getEndX(), ey = line.getEndY();
        double r   = StyleSettings.get().getVertexRadius();
        double tx  = ex - curve.getControlX();
        double ty  = ey - curve.getControlY();
        double len = Math.sqrt(tx * tx + ty * ty);
        if (len == 0) return;
        double cos = tx / len, sin = ty / len;
        double tipX  = ex - r * cos, tipY = ey - r * sin;
        double aLen  = r * 0.6;
        double aHalf = aLen * (2.0 / 5.0);
        arrowHead.getPoints().setAll(
            tipX, tipY,
            tipX - aLen*cos - aHalf*sin, tipY - aLen*sin + aHalf*cos,
            tipX - aLen*cos + aHalf*sin, tipY - aLen*sin - aHalf*cos
        );
    }

    public String getEdgeId() { return id; }
    public VertexDrawn getSource() { return source; }
    public VertexDrawn getTarget() { return target; }

    private void applyStroke(String hexColor) {
        String resolved = (hexColor != null && !hexColor.isEmpty()) ? hexColor : userStrokeHex;
        if (resolved != null) {
            curve.setStyle("-fx-stroke: " + resolved + ";");
            arrowHead.setStyle("-fx-fill: " + resolved + "; -fx-stroke: " + resolved + ";");
        } else {
            curve.setStyle("");
            arrowHead.setStyle("");
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

    public void setOffset(double offset) {
        this.offset = offset;
        updateArrowHead();
    }

    public void updateCurve() {
        double sx = line.getStartX(), sy = line.getStartY();
        double ex = line.getEndX(), ey = line.getEndY();
        double midX = (sx + ex) / 2;
        double midY = (sy + ey) / 2;
        double perpX = sy - ey;
        double perpY = ex - sx;
        double length = Math.sqrt(perpX * perpX + perpY * perpY);
        if (length == 0) return;
        double effectiveLength = Math.max(length, 150.0);
        double ctrlX = midX + offset * perpX * effectiveLength / length;
        double ctrlY = midY + offset * perpY * effectiveLength / length;
        curve.setControlX(ctrlX);
        curve.setControlY(ctrlY);

        double r = StyleSettings.get().getVertexRadius();
        double depX = ctrlX - sx, depY = ctrlY - sy;
        double depLen = Math.sqrt(depX * depX + depY * depY);
        if (depLen > 0) {
            curve.setStartX(sx + r * depX / depLen);
            curve.setStartY(sy + r * depY / depLen);
        } else {
            curve.setStartX(sx);
            curve.setStartY(sy);
        }

        double appX = ex - ctrlX, appY = ey - ctrlY;
        double appLen = Math.sqrt(appX * appX + appY * appY);
        if (appLen > 0) {
            curve.setEndX(ex - r * appX / appLen);
            curve.setEndY(ey - r * appY / appLen);
        } else {
            curve.setEndX(ex);
            curve.setEndY(ey);
        }
    }

    public void setWeightText(String text) {
        weightLabel.setText(text == null ? "" : text);
    }

    public void highlightAsTreeEdge() {
        curve.pseudoClassStateChanged(TREE_EDGE, true);
        curve.pseudoClassStateChanged(CYCLE_EDGE, false);
        curve.setStyle("-fx-stroke: #1a56db; -fx-stroke-width: 3;");
    }

    public void highlightAsCycle() {
        curve.pseudoClassStateChanged(CYCLE_EDGE, true);
        curve.pseudoClassStateChanged(TREE_EDGE, false);
        curve.setStyle("-fx-stroke: #9ca3af; -fx-stroke-width: 2; -fx-stroke-dash-array: 10 10;");
    }

    public void resetStyle() {
        curve.pseudoClassStateChanged(TREE_EDGE, false);
        curve.pseudoClassStateChanged(CYCLE_EDGE, false);
        applyStroke(null);
        setWeightText(null);
    }
}