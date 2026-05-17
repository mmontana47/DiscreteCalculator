package pl.edu.uj.discretecalculator.view.command;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.function.Consumer;

public class AddEdgeCommand implements Command {
    private final CanvasManager canvas;
    private final VertexDrawn source;
    private final VertexDrawn target;
    private final Consumer<EdgeDrawn> onClick;
    private EdgeDrawn edge;

    public AddEdgeCommand(CanvasManager canvas, VertexDrawn source, VertexDrawn target,
                          Consumer<EdgeDrawn> onClick) {
        this.canvas = canvas;
        this.source = source;
        this.target = target;
        this.onClick = onClick;
    }

    @Override
    public void execute() {
        if(edge == null){
            edge = canvas.createEdge(source, target, onClick);
        }
        else {
            canvas.attachEdge(edge);
        }
    }

    @Override
    public void undo() {
        canvas.detachEdge(edge);
    }
}
