package pl.edu.uj.discretecalculator.view.command;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;

public class RemoveEdgeCommand implements Command {
    private final CanvasManager canvas;
    private final EdgeDrawn edge;

    public RemoveEdgeCommand(CanvasManager canvas, EdgeDrawn edge) {
        this.canvas = canvas;
        this.edge = edge;
    }

    @Override
    public void execute() { canvas.detachEdge(edge); }

    @Override
    public void undo() { canvas.attachEdge(edge); }
}
