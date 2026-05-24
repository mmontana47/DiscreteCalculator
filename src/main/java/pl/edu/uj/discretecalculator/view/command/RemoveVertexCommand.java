package pl.edu.uj.discretecalculator.view.command;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.List;

public class RemoveVertexCommand implements Command {
    private final CanvasManager canvas;
    private final VertexDrawn vertex;
    private final List<EdgeDrawn> incidentEdges;
    private final int removedIndex;

    public RemoveVertexCommand(CanvasManager canvas, VertexDrawn vertex) {
        this.canvas = canvas;
        this.vertex = vertex;
        this.incidentEdges = canvas.incidentEdges(vertex);
        this.removedIndex = canvas.getVertices().indexOf(vertex);
    }

    @Override
    public void execute() {
        for(EdgeDrawn incident : incidentEdges) {
            canvas.detachEdge(incident);
        }
        canvas.detachVertex(vertex);
        canvas.renumber();
    }

    @Override
    public void undo() {
        canvas.attachVertexAt(removedIndex, vertex);
        for (EdgeDrawn incident : incidentEdges) {
            canvas.attachEdge(incident);
        }
        canvas.renumber();
    }
}
