package pl.edu.uj.discretecalculator.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import com.google.gson.JsonSyntaxException;
import pl.edu.uj.discretecalculator.io.GraphExporter;
import pl.edu.uj.discretecalculator.io.GraphImporter;
import javafx.scene.control.RadioMenuItem;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.Theme;
import pl.edu.uj.discretecalculator.view.VertexDrawn;
import pl.edu.uj.discretecalculator.view.builder.BuilderContext;
import pl.edu.uj.discretecalculator.view.builder.GraphBuilders;
import pl.edu.uj.discretecalculator.view.command.*;
import java.io.File;
import java.io.IOException;

import java.util.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import pl.edu.uj.discretecalculator.algorithm.BFS;
import pl.edu.uj.discretecalculator.algorithm.BFSResult;
import pl.edu.uj.discretecalculator.algorithm.DFS;
import pl.edu.uj.discretecalculator.algorithm.DFSResult;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

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
    @FXML private RadioMenuItem lightThemeItem;
    @FXML private RadioMenuItem darkThemeItem;

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
                        Mode m = Mode.fromLabel(btn.getText());
                        if(m==null) return;
                        modeLabel.setText("Mode: " + m.label());
                        hintLabel.setText(m.hint());
                    }
                }
        );
        graphPane.setOnMouseClicked(this::onPaneClick);
    }

    @FXML private void onSelectLightTheme() { applyTheme(Theme.LIGHT); }
    @FXML private void onSelectDarkTheme()  { applyTheme(Theme.DARK);  }

    private void applyTheme(Theme theme) {
        var scene = graphPane.getScene();
        if (scene == null) return;
        theme.applyTo(scene);
        if (theme == Theme.LIGHT) lightThemeItem.setSelected(true);
        else darkThemeItem.setSelected(true);
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
    private void onOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open graph");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

        File file = chooser.showOpenDialog(graphPane.getScene().getWindow());
        if (file == null) return;

        clearSelection();
        try {
            GraphImporter.importFrom(file, buildContext());
            history.clear();
            refreshUndoRedoState();
        } catch (IOException | JsonSyntaxException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Open failed: " + ex.getMessage(), ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();
        }
    }

    @FXML
    private void onSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save graph");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        chooser.setInitialFileName("graph.json");

        File file = chooser.showSaveDialog(graphPane.getScene().getWindow());
        if (file == null) return;

        try {
            GraphExporter.export(canvas, file);
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage(), ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();
        }
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
        Optional<int[]> n_m = promptForBipartite();
        if(n_m.isEmpty()) return;
        runCommand(GraphBuilders.bipartite(buildContext(),  n_m.get()[0], n_m.get()[1]));
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
                () -> (currentMode() == Mode.MOVE),
                this::onEdgeClick);
    }

    private OptionalInt promptForInt(String title, String header, String var) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle(title);
        dlg.setHeaderText(header);
        dlg.setGraphic(null);
        dlg.setContentText(var +" =");
        Platform.runLater(() -> dlg.getEditor().requestFocus());
        Optional<String> r = dlg.showAndWait();
        if (r.isEmpty()) return OptionalInt.empty();
        try {
            int n = Integer.parseInt(r.get().trim());
            return n > 0 ? OptionalInt.of(n) : OptionalInt.empty();
        } catch (NumberFormatException ex) {
            return OptionalInt.empty();
        }
    }

    private Optional<int[]> promptForBipartite() {
        Dialog<int[]> dlg = new Dialog<>();
        dlg.setTitle("Bipartite");
        dlg.setHeaderText("Build complete bipartite K_{n,m}");

        ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField nField = new TextField();
        TextField mField = new TextField();
        nField.setPromptText("e.g. 3");
        mField.setPromptText("e.g. 4");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("n ="), 0, 0);
        grid.add(nField, 1, 0);
        grid.add(new Label("m ="), 0, 1);
        grid.add(mField, 1, 1);
        dlg.getDialogPane().setContent(grid);

        Button okBtn = (Button) dlg.getDialogPane().lookupButton(okType);
        okBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !isPositiveInt(nField.getText() ) || !isPositiveInt(mField.getText()),
                nField.textProperty(), mField.textProperty()
        ));

        Platform.runLater(nField::requestFocus);

        dlg.setResultConverter(btn -> {
            if (btn != okType) return null;
            return new int[]{
                    Integer.parseInt(nField.getText().trim()),
                    Integer.parseInt(mField.getText().trim())
            };
        });

        return dlg.showAndWait();
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

    private void onPaneClick(MouseEvent e) {
        if (currentMode()== Mode.ADD_VERTEX) {
            runCommand(new AddVertexCommand(canvas, e.getX(), e.getY(),
                    this::onVertexClick,
                    () -> (currentMode() == Mode.MOVE)));
        } else if ((currentMode() == Mode.ADD_EDGE) && source != null) {
            clearSelection();
        }
    }

    private void onVertexClick(VertexDrawn vertex) {
        switch (currentMode()) {
            case ADD_EDGE -> {
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
            case DELETE -> runCommand(new RemoveVertexCommand(canvas, vertex));
            case RUN_BFS -> runAndAnimateAlgorithm(vertex, "BFS");
            case RUN_DFS -> runAndAnimateAlgorithm(vertex, "DFS");
            case null -> {}
            default -> {}
        }
    }

    private void onEdgeClick(EdgeDrawn edge) {
        switch (currentMode()) {
            case Mode.DELETE -> runCommand(new RemoveEdgeCommand(canvas, edge));
            case null -> {}
            default -> {}
        }
    }

    private Mode currentMode() {
        Toggle tog = modeGroup.getSelectedToggle();
        if (tog == null) return null;
        return Mode.fromLabel(((ToggleButton) tog).getText());
    }

    private static boolean isPositiveInt(String s){
        try {
            return Integer.parseInt(s.trim())>0;
        }
        catch (NumberFormatException ex) {return false;}
    }

    //####################################################
    //##################### KONTROLER ####################
    //####################################################
    private Graph<String> buildMathematicalGraph() {
        Graph<String> mathGraph = new Graph<>("CanvasGraph");
        Map<String, Vertex<String>> dictionary = new HashMap<>();

        for (VertexDrawn vd : canvas.getVertices()) {
            String vId = vd.getVertexId();
            Vertex<String> v = new Vertex<>(Integer.parseInt(vd.getVertexId()), vd.getVertexId());
            mathGraph.addVertex(v);
            dictionary.put(vd.getVertexId(), v);
        }

        int edgeId = 0;
        for (EdgeDrawn ed : canvas.getEdges()) {
            Vertex<String> source = dictionary.get(ed.getSource().getVertexId());
            Vertex<String> target = dictionary.get(ed.getTarget().getVertexId());
            Edge<String> edge = new Edge<>(source, target, edgeId++);
            mathGraph.addEdge(edge);
        }
        return mathGraph;
    }

    // resetowanie płótna przed animacją
    private void resetCanvasStyles() {
        for (VertexDrawn v : canvas.getVertices()) v.resetStyle();
        for (EdgeDrawn e : canvas.getEdges()) e.resetStyle();
    }

    // logika animacji
    private void runAndAnimateAlgorithm(VertexDrawn startVisualNode, String algorithmType) {
        Graph<String> graph = buildMathematicalGraph();

        // znajdujemy startowy węzeł
        Vertex<String> startNode = null;
        for (Vertex<String> v : graph.getVertices()) {
            if (v.getValue().equals(startVisualNode.getVertexId())) { startNode = v; break; }
        }
        if (startNode == null) return;

        resetCanvasStyles();

        // zmienne wynikowe
        List<Vertex<String>> visitOrder = new ArrayList<>();
        Map<Vertex<String>, Vertex<String>> parentMap = new HashMap<>();
        Set<Edge<String>> cycles;
        Color highlightColor;

        // miejsce na typ algorytmu
        if (algorithmType.equals("BFS")) {
            BFSResult<String> result = new BFS<>(startNode).start(graph);
            visitOrder = result.getVisitOrder();
            parentMap = result.getParentMap();
            cycles = result.getNonTreeEdges();
            highlightColor = Color.web("#2ECC71");
        } else if (algorithmType.equals("DFS")) {
            DFSResult<String> result = new DFS<>(startNode).start(graph);
            visitOrder = result.getVisitOrder();
            parentMap = result.getParentMap();
            cycles = result.getNonTreeEdges();
            highlightColor = Color.web("#3498DB");
        }
        else {
            highlightColor = Color.BLACK;
            cycles = new HashSet<>();
        }

        //ANIMACJA
        Timeline timeline = new Timeline();
        double delaySeconds = 0.5;

        // malowanie wezłów
        for (int i = 0; i < visitOrder.size(); i++) {
            String nodeId = visitOrder.get(i).getValue();
            Vertex<String> currentNode = visitOrder.get(i);

            Vertex<String> parentNode = parentMap.get(currentNode);
            String parentId = (parentNode != null) ? parentNode.getValue() : null;

            KeyFrame kf = new KeyFrame(Duration.seconds(i * delaySeconds), event -> {
                for (VertexDrawn vd : canvas.getVertices()) {
                    if (vd.getVertexId().equals(nodeId)) {
                        vd.highlightForAlgorithm(highlightColor);
                        break;
                    }
                }
                if (parentId != null) {
                    for (EdgeDrawn ed : canvas.getEdges()) {
                        String source = ed.getSource().getVertexId();
                        String target = ed.getTarget().getVertexId();

                        if ((source.equals(nodeId) && target.equals(parentId)) ||
                                (source.equals(parentId) && target.equals(nodeId))) {

                            ed.highlightAsTreeEdge();
                            break;
                        }
                    }
                }
            });
            timeline.getKeyFrames().add(kf);
        }

        // krawędzie spoza drzewa na czerwono
        KeyFrame cyclesFrame = new KeyFrame(Duration.seconds(visitOrder.size() * delaySeconds), event -> {
            for (Edge<String> badEdge : cycles) {
                String sourceId = badEdge.getSource().getValue();
                String targetId = badEdge.getTarget().getValue();

                for (EdgeDrawn ed : canvas.getEdges()) {
                    if ((ed.getSource().getVertexId().equals(sourceId) && ed.getTarget().getVertexId().equals(targetId)) ||
                            (ed.getSource().getVertexId().equals(targetId) && ed.getTarget().getVertexId().equals(sourceId))) {
                        ed.highlightAsCycle();
                    }
                }
            }
        });
        timeline.getKeyFrames().add(cyclesFrame);

        timeline.play();
    }
}
