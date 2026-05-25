package pl.edu.uj.discretecalculator.view.builder;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public record BuilderContext(
        CanvasManager canvas,
        Consumer<VertexDrawn> onVertexClick,
        BooleanSupplier canDrag,
        Consumer<EdgeDrawn> onEdgeClick
) {}