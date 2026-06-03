package pl.edu.uj.discretecalculator.view.command;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class AddVertexCommand implements Command {
    private final CanvasManager canvas;
    private final double x, y;
    private String id;
    private final Consumer<VertexDrawn> onClick;
    private final BooleanSupplier canDrag;
    private VertexDrawn vertex;


    public AddVertexCommand(CanvasManager canvas, double x, double y,
                            Consumer<VertexDrawn> onClick, BooleanSupplier canDrag) {
        this.canvas = canvas;
        this.x = x;
        this.y = y;
        this.onClick = onClick;
        this.canDrag = canDrag;
    }

    public AddVertexCommand(CanvasManager canvas, double x, double y,
                            Consumer<VertexDrawn> onClick, BooleanSupplier canDrag, String id) {
        this.canvas = canvas;
        this.x = x;
        this.y = y;
        this.onClick = onClick;
        this.canDrag = canDrag;
        this.id = id;
    }

    @Override
    public void execute() {
        if(vertex == null){
            vertex = (id!=null) ? canvas.createVertex(x,y,id, onClick,canDrag) : canvas.createVertex(x, y, onClick,canDrag);
        }
        else{
            canvas.attachVertex(vertex);
        }
    }

    @Override
    public void undo() {
        canvas.detachVertex(vertex);
    }

    public VertexDrawn getVertex() { return vertex; }
}
