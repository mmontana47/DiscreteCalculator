package pl.edu.uj.discretecalculator.view.command;

import java.util.List;

public class CompoundCommand implements Command {
    private final List<Command> commands;

    public CompoundCommand(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public void execute() {
        for (Command c : commands) c.execute();
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) commands.get(i).undo();
    }
}
