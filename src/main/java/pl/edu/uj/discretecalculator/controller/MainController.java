package pl.edu.uj.discretecalculator.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;
import pl.edu.uj.discretecalculator.view.builder.BuilderContext;
import pl.edu.uj.discretecalculator.view.builder.GraphBuilders;
import pl.edu.uj.discretecalculator.view.command.*;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public class MainController {
    private CanvasManager canvas;
    private final CommandHistory history = new CommandHistory();
    private VertexDrawn source = null;

    @FXML private Pane graphPane;
    @FXML private ToggleGroup modeGroup;
    @FXML private Label modeLabel;
    @FXML private Label hintLabel;
    @FXML private Label countsLabel;
    @FXML private MenuItem undoItem;
    @FXML private MenuItem redoItem;

    @FXML
    private void initialize() {
        canvas = new CanvasManager(graphPane, countsLabel);
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN));
        refreshUndoRedoState();

        modeGroup.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {
                    clearSelection();
                    if (newValue == null) {
                        modeLabel.setText("Mode: -");
                        hintLabel.setText("Select a mode to start");
                    } else {
                        ToggleButton btn = (ToggleButton) newValue;
                        modeLabel.setText("Mode: " + btn.getText());
                        hintLabel.setText(setHint(btn.getText()));
                    }
                }
        );
        graphPane.setOnMouseClicked(this::onPaneClick);
    }

    @FXML
    private void newGraph() {
        clearSelection();
        canvas.clear();
        history.clear();
        refreshUndoRedoState();
    }
    @FXML
    private void onExit() {
        Platform.exit();
    }

    @FXML
    private void onUndo() {
        clearSelection();
        history.undo();
        refreshUndoRedoState();
    }

    @FXML
    private void onRedo() {
        clearSelection();
        history.redo();
        refreshUndoRedoState();
    }

    @FXML private void onBuildCycle() {
        clearSelection();
        OptionalInt n = promptForInt("Cycle", "Build cycle C_n", "n" );
        if (n.isEmpty()) return;
        runCommand(GraphBuilders.cycle(buildContext(),  n.getAsInt()));
    }
    @FXML private void onBuildComplete() {
        clearSelection();
        OptionalInt n = promptForInt("Clique", "Build clique K_n", "n");
        if(n.isEmpty()) return;
        runCommand(GraphBuilders.clique(buildContext(),  n.getAsInt()));
    }
    @FXML private void onBuildBipartite() {
        clearSelection();
        OptionalInt n = promptForInt("Bipartite", "Build bipartite K_{n, m}", "n");
        if (n.isEmpty()) return;
        OptionalInt m = promptForInt("Bipartite", "Build bipartite K_{n, m}", "m");
        if (m.isEmpty()) return;
        runCommand(GraphBuilders.bipartite(buildContext(),  n.getAsInt(), m.getAsInt()));
    }
    @FXML private void onBuildTree() {
        clearSelection();
        OptionalInt n = promptForInt("Tree", "Build random tree on n vertices", "n");
        if (n.isEmpty()) return;
        runCommand(GraphBuilders.randomTree(buildContext(),  n.getAsInt()));
    }

    private BuilderContext buildContext() {
        return new BuilderContext(
                canvas,
                this::onVertexClick,
                () -> Objects.equals(currentMode(), "Move"),
                this::onEdgeClick);
    }

    private OptionalInt promptForInt(String title, String header, String var) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle(title);
        dlg.setHeaderText(header);
        dlg.setContentText(var +" =");
        Optional<String> r = dlg.showAndWait();
        if (r.isEmpty()) return OptionalInt.empty();
        try {
            int n = Integer.parseInt(r.get().trim());
            return n > 0 ? OptionalInt.of(n) : OptionalInt.empty();
        } catch (NumberFormatException ex) {
            return OptionalInt.empty();
        }
    }

    private void runCommand(Command cmd) {
        history.execute(cmd);
        refreshUndoRedoState();
    }

    private void refreshUndoRedoState() {
        undoItem.setDisable(!history.canUndo());
        redoItem.setDisable(!history.canRedo());
    }

    private void clearSelection() {
        if (source != null) {
            source.unselect();
            source = null;
        }
    }

    private String setHint(String mode) {
        return switch (mode) {
            case "Add Vertex" -> "Click to add a vertex";
            case "Add Edge" -> "Click two vertices to add an edge";
            case "Delete" -> "Click a vertex or an edge to delete it";
            case "Move" -> "Click and drag a vertex to change its position";
            default -> "";
        };
    }

    private void onPaneClick(MouseEvent e) {
        if (Objects.equals(currentMode(), "Add Vertex")) {
            runCommand(new AddVertexCommand(canvas, e.getX(), e.getY(),
                    this::onVertexClick,
                    () -> Objects.equals(currentMode(), "Move")));
        } else if (Objects.equals(currentMode(), "Add Edge") && source != null) {
            clearSelection();
        }
    }

    private void onVertexClick(VertexDrawn vertex) {
        switch (currentMode()) {
            case "Add Edge" -> {
                if (source == null) {
                    source = vertex;
                    vertex.select();
                } else if (source == vertex || canvas.edgeExists(source, vertex)) {
                    clearSelection();
                } else {
                    runCommand(new AddEdgeCommand(canvas, source, vertex, this::onEdgeClick));
                    clearSelection();
                }
            }
            case "Delete" -> runCommand(new RemoveVertexCommand(canvas, vertex));
            case null -> {}
            default -> {}
        }
    }

    private void onEdgeClick(EdgeDrawn edge) {
        switch (currentMode()) {
            case "Delete" -> runCommand(new RemoveEdgeCommand(canvas, edge));
            case null -> {}
            default -> {}
        }
    }

    private String currentMode() {
        Toggle tog = modeGroup.getSelectedToggle();
        if (tog == null) return null;
        return ((ToggleButton) tog).getText();
    }
}
