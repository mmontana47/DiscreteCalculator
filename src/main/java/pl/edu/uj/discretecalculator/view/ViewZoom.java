package pl.edu.uj.discretecalculator.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import pl.edu.uj.discretecalculator.controller.CanvasManager;

public class ViewZoom {
    public static final double MIN_ZOOM  = 0.2;
    public static final double MAX_ZOOM  = 5.0;
    public static final double ZOOM_STEP = 1.1;

    private final Pane pane;
    private final CanvasManager canvas;
    private final DoubleProperty zoom = new SimpleDoubleProperty(1.0);

    public ViewZoom(Pane pane, CanvasManager canvas) {
        this.pane = pane;
        this.canvas = canvas;
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());
        pane.setClip(clip);
    }

    public DoubleProperty zoomProperty() { return zoom; }
    public double getZoom() { return zoom.get(); }

    public void zoomBy(double factor, double pivotX, double pivotY) {
        double newZoom = Math.clamp(zoom.get() * factor, MIN_ZOOM, MAX_ZOOM);
        if (newZoom == zoom.get()) return;

        double actualFactor = newZoom / zoom.get();

        for (VertexDrawn v : canvas.getVertices()) {
            double r = StyleSettings.get().getVertexRadius();
            double CX = v.getLayoutX()+r;
            double CY = v.getLayoutY()+r;
            double newCX = pivotX + (CX - pivotX) * actualFactor;
            double newCY = pivotY + (CY - pivotY) * actualFactor;
            v.setLayoutX(newCX-r);
            v.setLayoutY(newCY-r);
            }

        zoom.set(newZoom);
    }

    public void zoomIn(double pivotX, double pivotY)  { zoomBy(ZOOM_STEP, pivotX, pivotY); }
    public void zoomOut(double pivotX, double pivotY) { zoomBy(1.0 / ZOOM_STEP, pivotX, pivotY); }

    public void reset() {
        zoomBy(1.0 / zoom.get(), pane.getWidth() / 2, pane.getHeight() / 2);
    }

    public void pan(double dx, double dy) {
        for (VertexDrawn v : canvas.getVertices()) {
            v.setLayoutX(v.getLayoutX()+dx);
            v.setLayoutY(v.getLayoutY()+dy);
        }
    }
}
