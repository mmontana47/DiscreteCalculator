package pl.edu.uj.discretecalculator.view.command;

import pl.edu.uj.discretecalculator.view.EdgeDrawn;

public class ChangeWeightCommand implements Command {
    private final EdgeDrawn edge;
    private final String oldWeight;
    private final String newWeight;

    public ChangeWeightCommand(EdgeDrawn edge, String newWeight) {
        this.edge = edge;
        this.oldWeight = edge.getWeightText();
        this.newWeight = newWeight;
    }

    @Override
    public void execute() {
        edge.setWeightText(newWeight);
    }

    @Override
    public void undo() {
        edge.setWeightText(oldWeight);
    }
}