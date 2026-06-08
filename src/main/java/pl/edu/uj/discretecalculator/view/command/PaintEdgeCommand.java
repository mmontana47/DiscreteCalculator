package pl.edu.uj.discretecalculator.view.command;

import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;

public class PaintEdgeCommand implements Command {
    private final EdgeDrawn edge;
    private final ColorPicker colorPicker;
    private final Color prev;

    public PaintEdgeCommand(EdgeDrawn edge, ColorPicker colorPicker) {
        prev = edge.getUserStrokeColor();
        this.edge = edge;
        this.colorPicker = colorPicker;
    }

    @Override
    public void execute() {
        edge.setUserStrokeColor(colorPicker.getValue());
    }
    @Override
    public void undo() {
        edge.setUserStrokeColor(prev);
    }
}
