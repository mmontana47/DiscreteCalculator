package pl.edu.uj.discretecalculator.view.command;

import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

public class PaintVertexCommand implements Command {
    private final VertexDrawn vertex;
    private final ColorPicker colorPicker;
    private final Color prev;

    public PaintVertexCommand(VertexDrawn vertex, ColorPicker colorPicker) {
        prev = vertex.getUserFillColor();
        this.vertex = vertex;
        this.colorPicker = colorPicker;
    }

    @Override
    public void execute() {
        vertex.setUserFillColor(colorPicker.getValue());
    }
    @Override
    public void undo() {
        vertex.setUserFillColor(prev);
    }
}
