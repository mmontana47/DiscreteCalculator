package pl.edu.uj.discretecalculator.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import com.google.gson.JsonSyntaxException;
import pl.edu.uj.discretecalculator.algorithm.*;
import pl.edu.uj.discretecalculator.io.GraphExporter;
import pl.edu.uj.discretecalculator.io.GraphExporterTXT;
import pl.edu.uj.discretecalculator.io.GraphImporter;
import pl.edu.uj.discretecalculator.io.GraphImporterTXT;
import pl.edu.uj.discretecalculator.view.*;
import pl.edu.uj.discretecalculator.view.animation.*;
import pl.edu.uj.discretecalculator.view.builder.BuilderContext;
import pl.edu.uj.discretecalculator.view.builder.GraphBuilders;
import pl.edu.uj.discretecalculator.view.command.*;
import java.io.File;
import java.io.IOException;

import java.util.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;

public class MainController {
    private CanvasManager canvas;
    private final CommandHistory history = new CommandHistory();
    private VertexDrawn source = null;
    private Timeline activeAnimation = null;
    private ViewZoom viewZoom;
    private double lastMouseX, lastMouseY;
    private boolean panDragged = false;
    private static final double panLimit = 5.0;
    private double lastPanX, lastPanY;

    @FXML private Pane graphPane;
    @FXML private ToggleGroup modeGroup;
    @FXML private Label modeLabel;
    @FXML private Label hintLabel;
    @FXML private Label countsLabel;
    @FXML private MenuItem undoItem;
    @FXML private MenuItem redoItem;
    @FXML private RadioMenuItem lightThemeItem;
    @FXML private RadioMenuItem darkThemeItem;
    @FXML private MenuItem resetViewItem;
    @FXML private Slider vertexSizeSlider;
    @FXML private Slider edgeWidthSlider;
    @FXML private Button btnZoomIn;
    @FXML private Button btnZoomOut;
    @FXML private Button btnResetZoom;


    @FXML
    private void initialize() {
        canvas = new CanvasManager(graphPane, countsLabel);
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN));

        resetViewItem.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
        refreshUndoRedoState();
        viewZoom = new ViewZoom(graphPane, canvas);

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
        graphPane.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            lastPanX = e.getX();
            lastPanY = e.getY();
            panDragged = false;
        });
        graphPane.setOnMouseDragged(e -> {
            if(Math.hypot(lastPanX - e.getX(), lastPanY - e.getY()) < panLimit) return;
            viewZoom.pan(e.getX() - lastMouseX, e.getY() - lastMouseY);
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            panDragged = true;
        });
        graphPane.setOnScroll(e -> {
            double scrollDelta = e.getDeltaY();
            if(scrollDelta == 0) return;
            double factor = (scrollDelta > 0) ? ViewZoom.ZOOM_STEP : (1.0) / ViewZoom.ZOOM_STEP;
            viewZoom.zoomBy(factor, e.getX(), e.getY());
        });

        graphPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN),
                        () -> viewZoom.zoomIn(graphPane.getWidth() / 2, graphPane.getHeight() / 2));
                newValue.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN),
                        () -> viewZoom.zoomOut(graphPane.getWidth() / 2, graphPane.getHeight() / 2));
            }
        });

        graphPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                KeyCodeCombination CtrlMinus = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN);
                Mnemonic mn = new Mnemonic(btnZoomOut, CtrlMinus);
                newValue.addMnemonic(mn);
            }
        });

        vertexSizeSlider.setMin(StyleSettings.MIN_VERTEX_RADIUS);
        vertexSizeSlider.setMax(StyleSettings.MAX_VERTEX_RADIUS);
        vertexSizeSlider.setValue(StyleSettings.get().getVertexRadius());
        Bindings.bindBidirectional(vertexSizeSlider.valueProperty(), StyleSettings.get().vertexRadiusProperty());

        edgeWidthSlider.setMin(StyleSettings.MIN_EDGE_WIDTH);
        edgeWidthSlider.setMax(StyleSettings.MAX_EDGE_WIDTH);
        edgeWidthSlider.setValue(StyleSettings.get().getEdgeWidth());
        Bindings.bindBidirectional(edgeWidthSlider.valueProperty(), StyleSettings.get().edgeWidthProperty());
    }

    @FXML private void onSelectLightTheme() { applyTheme(Theme.LIGHT); }
    @FXML private void onSelectDarkTheme()  { applyTheme(Theme.DARK);  }

    @FXML
    private void onResetView() {
        if (activeAnimation != null) return;
        clearSelection();
        resetCanvasStyles();
    }

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
    private void onUndo() {
        if (activeAnimation != null) return;
        clearSelection();
        history.undo();
        refreshUndoRedoState();
    }

    @FXML
    private void onRedo() {
        if (activeAnimation != null) return;
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

    //graph vizual properties
    @FXML
    public void OnZoomIn(ActionEvent actionEvent) {
        viewZoom.zoomIn(graphPane.getWidth()/2, graphPane.getHeight()/2);
    }

    @FXML
    public void OnZoomOut(ActionEvent actionEvent) {
        viewZoom.zoomOut(graphPane.getWidth()/2, graphPane.getHeight()/2);
    }

    @FXML
    public void OnResetZoom(ActionEvent actionEvent) {
        viewZoom.reset();
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
        //panic button
        if (panDragged) {
            panDragged = false;
            return;
        }
        if (activeAnimation != null) {
            activeAnimation.stop();
            activeAnimation = null;
            resetCanvasStyles();
            clearSelection();
            return;
        }
        if (currentMode()== Mode.ADD_VERTEX) {
            runCommand(new AddVertexCommand(canvas, e.getX(), e.getY(),
                    this::onVertexClick,
                    () -> (currentMode() == Mode.MOVE)));
        } else if ((currentMode() == Mode.ADD_EDGE) && source != null) {
            clearSelection();
        }

    }

    private void onVertexClick(VertexDrawn vertex) {
        if (activeAnimation != null) return;
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
        if (activeAnimation != null) return;
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
    //#################### IMPORT ########################
    //####################################################

    @FXML
    private void onOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open graph");

        FileChooser.ExtensionFilter allFilter  = new FileChooser.ExtensionFilter("All Supported Formats", "*.json", "*.txt");
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON Graph", "*.json");
        FileChooser.ExtensionFilter txtFilter  = new FileChooser.ExtensionFilter("Text (OI Format)", "*.txt");
        chooser.getExtensionFilters().addAll(allFilter, jsonFilter, txtFilter);
        chooser.setSelectedExtensionFilter(allFilter);

        File file = chooser.showOpenDialog(graphPane.getScene().getWindow());
        if (file == null) return;

        boolean isTxt = decideIsTxt(file, chooser.getSelectedExtensionFilter());

        clearSelection();
        try {
            if (isTxt) GraphImporterTXT.importFromTxt(file, buildContext());
            else       GraphImporter.importFrom(file, buildContext());

            history.clear();
            refreshUndoRedoState();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Open failed: " + ex.getMessage(), ButtonType.OK);
            a.setHeaderText("Import exception");
            a.showAndWait();
        }
    }

    @FXML
    private void onSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save graph");

        FileChooser.ExtensionFilter txtFilter  = new FileChooser.ExtensionFilter("Text (OI Format)", "*.txt");
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON Graph", "*.json");
        chooser.getExtensionFilters().addAll(txtFilter, jsonFilter);
        chooser.setSelectedExtensionFilter(txtFilter);
        chooser.setInitialFileName("graph.txt");

        File file = chooser.showSaveDialog(graphPane.getScene().getWindow());
        if (file == null) return;

        boolean isTxt = decideIsTxt(file, chooser.getSelectedExtensionFilter());

        String ext = isTxt ? ".txt" : ".json";
        if (!file.getName().toLowerCase().endsWith(ext)) {
            file = new File(file.getParentFile(), file.getName() + ext);
        }

        try {
            if (isTxt) GraphExporterTXT.exportToTxt(canvas, file);
            else       GraphExporter.export(canvas, file);
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage(), ButtonType.OK);
            a.setHeaderText("Export exception");
            a.showAndWait();
        }
    }

    // Filename takes priority (it's what the user typed); fall back to the selected filter.
    private static boolean decideIsTxt(File file, FileChooser.ExtensionFilter selected) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".txt"))  return true;
        if (name.endsWith(".json")) return false;
        if (selected != null) {
            for (String ext : selected.getExtensions()) {
                if (ext.equalsIgnoreCase("*.txt"))  return true;
                if (ext.equalsIgnoreCase("*.json")) return false;
            }
        }
        return false;
    }

    //####################################################
    //##################### TESTY ########################
    //####################################################

    // --- TYMCZASOWE METODY TESTOWE DO ODTWARZACZA ---

    @FXML
    private void onTestPlay() {
        if (player != null) player.play();
    }

    @FXML
    private void onTestPause() {
        if (player != null) player.pause();
    }

    @FXML
    private void onTestStepForward() {
        // Zatrzymujemy automatyczne odtwarzanie, jeśli użytkownik klika krok po kroku
        if (player != null) {
            player.pause();
            player.stepForward();
        }
    }

    @FXML
    private void onTestStepBackward() {
        if (player != null) {
            player.pause();
            player.stepBackward();
        }
    }

    // --- ALGORYTMY GLOBALNE (Nie wymagają wierzchołka startowego) ---

    @FXML
    private void onRunGreedyColoring() {
        // 1. Jeśli odtwarzacz nie istnieje, tworzymy go
        if (player == null) {
            player = new AlgorithmPlayer(canvas);
        }

        // Czyszczenie ewentualnych zaznaczeń w trybach edycji
        clearSelection();

        // 2. Budujemy model matematyczny
        Graph<String> graph = buildMathematicalGraph();

        // Zabezpieczenie: nie uruchamiamy animacji na pustym płótnie
        if (graph.getVertices().isEmpty()) return;

        // 3. Uruchamiamy logikę
        GreedyVC<String> algorithm = new GreedyVC<>();
        GreedyVCResult<String> result = algorithm.start(graph);

        // 4. Budujemy klatki animacji i ładujemy do odtwarzacza
        AlgorithmTrack track = TrackFactory.buildGreedyColoringTrack(result, graph);

        player.loadTrack(track);

        // Jeśli chcesz używać ręcznych przycisków (krok w przód), zakomentuj tę linijkę:
        player.play();
    }

    //####################################################
    //##################### KONTROLER ####################
    //####################################################

    // Silnik odtwarzacza animacji
    private AlgorithmPlayer player;

    /**
     * Buduje czysty graf matematyczny (Model) na podstawie obiektów narysowanych na płótnie.
     */
    private Graph<String> buildMathematicalGraph() {
        Graph<String> mathGraph = new Graph<>("CanvasGraph");
        Map<String, Vertex<String>> dictionary = new HashMap<>();

        // 1. Mapowanie wierzchołków wizualnych na wierzchołki matematyczne
        for (VertexDrawn vd : canvas.getVertices()) {
            Vertex<String> v = new Vertex<>(Integer.parseInt(vd.getVertexId()), vd.getVertexId());
            mathGraph.addVertex(v);
            dictionary.put(vd.getVertexId(), v);
        }

        // 2. Mapowanie krawędzi wizualnych na krawędzie matematyczne
        for (EdgeDrawn ed : canvas.getEdges()) {
            Vertex<String> source = dictionary.get(ed.getSource().getVertexId());
            Vertex<String> target = dictionary.get(ed.getTarget().getVertexId());

            // Rzutujemy Stringowe ID krawędzi z GUI na int dla modelu obliczeniowego
            int edgeId = Integer.parseInt(ed.getEdgeId());
            Edge<String> edge = new Edge<>(source, target, edgeId);
            mathGraph.addEdge(edge);
        }
        return mathGraph;
    }

    /**
     * Czyści twardo narzucone kolory i etykiety z płótna, przywracając bazowy styl CSS.
     */
    private void resetCanvasStyles() {
        canvas.resetAllStyles();
    }

    /**
     * Główna metoda sterująca uruchamianiem i animowaniem algorytmów.
     */
    private void runAndAnimateAlgorithm(VertexDrawn startVisualNode, String algorithmType) {
        // 1. Leniwa inicjalizacja odtwarzacza przy pierwszym uruchomieniu
        if (player == null) {
            player = new AlgorithmPlayer(canvas);
        }

        // 2. Budujemy świeżą strukturę matematyczną grafu
        Graph<String> graph = buildMathematicalGraph();

        // 3. Znajdujemy matematyczny wierzchołek startowy odpowiadający klikniętemu w GUI
        Vertex<String> startNode = null;
        for (Vertex<String> v : graph.getVertices()) {
            if (v.getValue().equals(startVisualNode.getVertexId())) {
                startNode = v;
                break;
            }
        }
        if (startNode == null) return;

        // Taśma animacji, którą przekażemy do odtwarzacza
        AlgorithmTrack generatedTrack = null;

        // 4. Wykonanie algorytmu i natychmiastowe zamienienie wyniku na taśmę animacji przez fabrykę
        if (algorithmType.equals("BFS")) {
            BFSResult<String> result = new BFS<>(startNode).start(graph);
            generatedTrack = TrackFactory.buildBfsTrack(result, graph);
        } else if (algorithmType.equals("DFS")) {
            DFSResult<String> result = new DFS<>(startNode).start(graph);
            generatedTrack = TrackFactory.buildDfsTrack(result, graph);
        } else if (algorithmType.equals("GREEDY_VC")) {
            GreedyVC<String> algorithm = new GreedyVC<>();
            GreedyVCResult<String> result = algorithm.start(graph);
            generatedTrack = TrackFactory.buildGreedyColoringTrack(result, graph);
        }

        // 5. Jeśli fabryka pomyślnie wygenerowała klatki, ładujemy je i odpalamy odtwarzacz
        if (generatedTrack != null) {
            player.loadTrack(generatedTrack);
            //player.play();
        }
    }
}
