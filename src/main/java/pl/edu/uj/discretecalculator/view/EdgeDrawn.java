package pl.edu.uj.discretecalculator.view;

import javafx.beans.value.ObservableBooleanValue;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;

import pl.edu.uj.discretecalculator.AppConfig;

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
    private boolean isActive = false;

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
        AppConfig.ArrowCfg arrow = AppConfig.get().style.arrow;
        this.arrowHead = new Polygon(0.0, 0.0, -arrow.length, arrow.halfWidth, -arrow.length, -arrow.halfWidth);

        line.startXProperty().bind(source.layoutXProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.startYProperty().bind(source.layoutYProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.endXProperty().bind(target.layoutXProperty().add(StyleSettings.get().vertexRadiusProperty()));
        line.endYProperty().bind(target.layoutYProperty().add(StyleSettings.get().vertexRadiusProperty()));

        curve = new QuadCurve();
        curve.getStyleClass().add("edge");
        curve.setFill(Color.TRANSPARENT);
        curve.setStrokeWidth(StyleSettings.get().getEdgeWidth());
        StyleSettings.get().edgeWidthProperty().addListener((obs, old, w) -> {
            double mult = AppConfig.get().style.edge.activeWidthMultiplier;
            curve.setStrokeWidth(isActive ? w.doubleValue() * mult : w.doubleValue());
        });

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

        this.getChildren().addAll(curve, arrowHead, weightLabel);;
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
        AppConfig.ArrowCfg arrowCfg = AppConfig.get().style.arrow;
        double tipX  = ex - r * cos, tipY = ey - r * sin;
        double aLen  = r * arrowCfg.lenFactor;
        double aHalf = aLen * arrowCfg.halfFactor;
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
        double effectiveLength = Math.max(length, AppConfig.get().style.edge.minEffectiveLength);
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

    public String getWeightText() {
        return weightLabel.getText();
    }

    public void highlightAsTreeEdge() {
        AppConfig.ColorsCfg c = AppConfig.get().style.colors;
        curve.pseudoClassStateChanged(TREE_EDGE, true);
        curve.pseudoClassStateChanged(CYCLE_EDGE, false);
        curve.setStyle("-fx-stroke: " + c.treeEdge + "; -fx-stroke-width: " + c.treeEdgeWidth + ";");
    }

    public void highlightAsCycle() {
        AppConfig.ColorsCfg c = AppConfig.get().style.colors;
        curve.pseudoClassStateChanged(CYCLE_EDGE, true);
        curve.pseudoClassStateChanged(TREE_EDGE, false);
        curve.setStyle("-fx-stroke: " + c.cycleEdge + "; -fx-stroke-width: " + c.cycleEdgeWidth + "; -fx-stroke-dash-array: 10 10;");
    }

    // NOWA METODA
    public void setActive(boolean active) {
        this.isActive = active;
        double baseWidth = StyleSettings.get().getEdgeWidth();
        double mult = AppConfig.get().style.edge.activeWidthMultiplier;
        curve.setStrokeWidth(active ? baseWidth * mult : baseWidth);
    }

    public void bindWeightVisibility(javafx.beans.value.ObservableBooleanValue visibilityProperty) {
        weightLabel.visibleProperty().bind(visibilityProperty);
    } // do naprawienia kwestii zwiazanej z pustymi polami labelow wag

    public void resetStyle() {
        curve.pseudoClassStateChanged(TREE_EDGE, false);
        curve.pseudoClassStateChanged(CYCLE_EDGE, false);
        applyStroke(null);
        setWeightText(null);
        setActive(false); // Reset grubości przy czyszczeniu płótna
    }
}