package pl.edu.uj.discretecalculator.view.command;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command cmd){
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void undo(){
        if(undoStack.isEmpty()){return;}
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo(){
        if(redoStack.isEmpty()){return;}
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    public boolean canUndo(){ return !undoStack.isEmpty();}
    public boolean canRedo(){ return !redoStack.isEmpty();}
    public void clear(){ undoStack.clear();redoStack.clear();}
}
