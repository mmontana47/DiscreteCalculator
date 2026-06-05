package pl.edu.uj.discretecalculator.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import pl.edu.uj.discretecalculator.algorithm.*;
import pl.edu.uj.discretecalculator.io.*;
import pl.edu.uj.discretecalculator.view.*;
import pl.edu.uj.discretecalculator.view.animation.*;
import pl.edu.uj.discretecalculator.view.builder.BuilderContext;
import pl.edu.uj.discretecalculator.view.builder.GraphBuilders;
import pl.edu.uj.discretecalculator.view.command.*;
import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import pl.edu.uj.discretecalculator.model.graph.Edge;
import pl.edu.uj.discretecalculator.model.graph.Graph;
import pl.edu.uj.discretecalculator.model.graph.Vertex;
import pl.edu.uj.discretecalculator.view.layout.ForceDirectedLayout;

public class MainController {
    private CanvasManager canvas;
    private final CommandHistory history = new CommandHistory();
    private VertexDrawn source = null;
    private ViewZoom viewZoom;
    private double lastMouseX, lastMouseY;
    private boolean panDragged = false;
    private static final double panLimit = 5.0;
    private double lastPanX, lastPanY;

    // Silnik odtwarzacza animacji (Nowa Architektura)
    private AlgorithmPlayer player;

    //layout
    private Timeline timeline = new Timeline();
    private double temperature;

    //live layout
    private Timeline liveTimeline;
    private ForceDirectedLayout liveEngine;

    private boolean inputPanelOpen = false;

    @FXML private Tab editGraphTab;
    @FXML private TabPane ribbon;
    @FXML private Pane graphPane;
    @FXML private ToggleGroup modeGroup;
    @FXML private Label modeLabel;
    @FXML private Label hintLabel;
    @FXML private Label countsLabel;
    @FXML private Button undoItem;
    @FXML private Button redoItem;
    @FXML private ToggleButton lightThemeItem;
    @FXML private ToggleButton darkThemeItem;
    @FXML private Button resetViewItem;
    @FXML private Slider vertexSizeSlider;
    @FXML private Slider edgeWidthSlider;
    @FXML private Button btnZoomIn;
    @FXML private Button btnZoomOut;
    @FXML private Button btnResetZoom;
    @FXML public Button autoLayout;
    @FXML private ToggleButton liveLayout;
    @FXML private VBox inputPanel;
    @FXML private TextArea edgeInput;
    @FXML private ColorPicker colorPicker;
    @FXML private CheckBox directedCheckbox;

    @FXML
    private void initialize() {
        ribbon.getSelectionModel().select(editGraphTab);
        canvas = new CanvasManager(graphPane, countsLabel);
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
                newValue.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                        this::onUndo);
                newValue.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                        this::onRedo);
                newValue.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.ESCAPE),
                        () -> {
                            if(inputPanelOpen) onToggleInputPanel();
                            else onResetView();
                        });
                newValue.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN),
                        this::onToggleInputPanel);
                newValue.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if(event.getTarget() instanceof TextInputControl) return;
                    if (event.getCode() == KeyCode.V) {selectMode(Mode.ADD_VERTEX);}
                    if (event.getCode() == KeyCode.E) selectMode(Mode.ADD_EDGE);
                    if (event.getCode() == KeyCode.D) selectMode(Mode.DELETE);
                    if (event.getCode() == KeyCode.M) selectMode(Mode.MOVE);
                    if (event.getCode() == KeyCode.P) selectMode(Mode.PAINT);
                });
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

        setupInputPanel();
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
    private void onResetView() {
        if (player != null) player.pause();
        clearSelection();
        resetCanvasStyles();
    }

    //Auto Layout
    @FXML
    private void onToggleLiveLayout() {
        if (liveLayout.isSelected()) {
            if (timeline != null) timeline.stop();

            liveEngine = new ForceDirectedLayout(canvas);
            liveEngine.kick(3);
            liveTimeline = new Timeline(new KeyFrame(Duration.millis(25), e -> liveEngine.tick()));
            liveTimeline.setCycleCount(Timeline.INDEFINITE);
            liveTimeline.play();
        } else {
            if (liveTimeline != null) liveTimeline.stop();
            liveEngine = null;
        }
    }

    private void kickLiveLayout(double scale) {
        if (liveEngine != null && liveLayout.isSelected()) {
            liveEngine.kick(scale);
        }
    }

    @FXML
    private void onAutoLayout() {
        if(timeline!=null) timeline.stop();
        if(liveTimeline!=null) liveTimeline.stop();
        if(liveLayout.isSelected()) {liveLayout.setSelected(false);}

        ForceDirectedLayout layout = new ForceDirectedLayout(canvas);
        double t0 = canvas.getGraphPane().getWidth()/50;
        int iterations = 120;
        AtomicInteger i= new AtomicInteger();
        temperature = t0;
        layout.turbulence();
        timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(25), e -> {
            i.getAndIncrement();
            layout.iteration(temperature);
            temperature=t0*(1- (double) i.get() /iterations);
            if(i.get() >= iterations) {
                timeline.stop();
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    @FXML
    private void newGraph() {
        clearSelection();
        canvas.clear();
        history.clear();
        refreshUndoRedoState();
        directedCheckbox.setSelected(false);
        canvas.setDirected(false);
    }
    @FXML
    private void onExit() {
        Platform.exit();
    }

    @FXML
    private void onUndo() {
        if (player != null) player.pause();
        clearSelection();
        history.undo();
        refreshUndoRedoState();
        kickLiveLayout(1);
    }

    @FXML
    private void onRedo() {
        if (player != null) player.pause();
        clearSelection();
        history.redo();
        refreshUndoRedoState();
        kickLiveLayout(1);
    }

    @FXML private void onBuildCycle() {
        clearSelection();
        OptionalInt n = promptForInt("Cycle", "Build cycle C_n", "n" );
        if (n.isEmpty()) return;
        runCommand(GraphBuilders.cycle(buildContext(),  n.getAsInt()));
        kickLiveLayout(3);
    }
    @FXML private void onBuildComplete() {
        clearSelection();
        OptionalInt n = promptForInt("Clique", "Build clique K_n", "n");
        if(n.isEmpty()) return;
        runCommand(GraphBuilders.clique(buildContext(),  n.getAsInt()));
        kickLiveLayout(3);
    }
    @FXML private void onBuildBipartite() {
        clearSelection();
        Optional<int[]> n_m = promptForBipartite();
        if(n_m.isEmpty()) return;
        runCommand(GraphBuilders.bipartite(buildContext(),  n_m.get()[0], n_m.get()[1]));
        kickLiveLayout(3);
    }
    @FXML private void onBuildTree() {
        clearSelection();
        OptionalInt n = promptForInt("Tree", "Build random tree on n vertices", "n");
        if (n.isEmpty()) return;
        runCommand(GraphBuilders.randomTree(buildContext(),  n.getAsInt()));
        kickLiveLayout(3);
    }

    //graph vizual properties
    @FXML
    public void OnZoomIn() {
        viewZoom.zoomIn(graphPane.getWidth()/2, graphPane.getHeight()/2);
    }

    @FXML
    public void OnZoomOut() {
        viewZoom.zoomOut(graphPane.getWidth()/2, graphPane.getHeight()/2);
    }

    @FXML
    public void OnResetZoom() {
        viewZoom.reset();
    }

    private void setupInputPanel() {
        edgeInput.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            String lines[] = edgeInput.getText().split("\\n");
            for (String line : lines) {
                parseLine(line.trim());
            }
            edgeInput.clear();
            e.consume();
        });
    }

    private void parseLine(String line) {
        if (line.isBlank()) return;
        String[] tokens = line.split("\\s+");
        if (tokens.length == 1) {
            if(canvas.getVertexById(tokens[0])==null) kickLiveLayout(1);
            getOrCreateVertex(tokens[0]);
        } else if (tokens.length == 2) {
            VertexDrawn s = getOrCreateVertex(tokens[0]);
            VertexDrawn t = getOrCreateVertex(tokens[1]);

            if(((!canvas.isDirected() && !canvas.edgeExists(s,t))||
                    (canvas.isDirected() && !canvas.edgeExistsDirected(s,t)))) {
                history.execute(new AddEdgeCommand(canvas, s, t, this::onEdgeClick));
                kickLiveLayout(1);
            }
        }
    }

    private VertexDrawn getOrCreateVertex(String id) {
        VertexDrawn existing = canvas.getVertexById(id);
        if (existing != null) return existing;
        Random r = new Random();
        double x = canvas.getGraphPane().getWidth()/2 + 400*(r.nextDouble()-0.5);
        double y = canvas.getGraphPane().getHeight()/2 + 400*(r.nextDouble()-0.5);
        BuilderContext ctx = buildContext();
        AddVertexCommand cmd = new AddVertexCommand(canvas, x, y, ctx.onVertexClick(), ctx.canDrag(), id);
        history.execute(cmd);
        return cmd.getVertex();
    }

    @FXML
    private void onToggleInputPanel() {
        double target = inputPanelOpen ? -150 : 0;
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), inputPanel);
        transition.setToX(target);
        if(inputPanelOpen) {
            graphPane.requestFocus();
        }
        else edgeInput.requestFocus();
        transition.play();
        inputPanelOpen = !inputPanelOpen;
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

    //-----------------on Click---------------------

    private void onPaneClick(MouseEvent e) {
        //panic button
        if (panDragged) {
            panDragged = false;
            return;
        }
        if (player != null) {
            player.pause();
            resetCanvasStyles();
            clearSelection();
            // Możemy ewentualnie schować odtwarzacz: player = null;
        }
        if (currentMode()== Mode.ADD_VERTEX) {
            runCommand(new AddVertexCommand(canvas, e.getX(), e.getY(),
                    this::onVertexClick,
                    () -> (currentMode() == Mode.MOVE)));
            kickLiveLayout(1);
        } else if ((currentMode() == Mode.ADD_EDGE) && source != null) {
            clearSelection();
        }
    }

    private void onVertexClick(VertexDrawn vertex) {
        if (player != null && player.isPlaying()) return;

        Mode currentMode = currentMode();
        if (currentMode == null) return;

        switch (currentMode) {
            case ADD_EDGE -> {
                if (source == null) {
                    source = vertex;
                    vertex.select();
                } else if (source == vertex ||
                        (canvas.isDirected() && canvas.edgeExistsDirected(source, vertex)) ||
                        (!canvas.isDirected() && canvas.edgeExists(source, vertex))) {
                    clearSelection();
                } else {
                    runCommand(new AddEdgeCommand(canvas, source, vertex, this::onEdgeClick));
                    clearSelection();
                    kickLiveLayout(1);
                }
            }
            case DELETE -> {
                runCommand(new RemoveVertexCommand(canvas, vertex));
                kickLiveLayout(1);
            }
            case RUN_BFS -> runAndAnimateAlgorithm(vertex, "BFS");
            case RUN_DFS -> runAndAnimateAlgorithm(vertex, "DFS");
            case PAINT -> vertex.setUserFillColor(colorPicker.getValue());
            default -> {
                // Ręczna obsługa algorytmów kolegów, dopóki nie dodasz ich do enum Mode
                if (currentMode.label().equals("Run Dijkstra")) {
                    runAndAnimateAlgorithm(vertex, "DIJKSTRA");
                }
            }
        }
    }

    private void onEdgeClick(EdgeDrawn edge) {
        if (player != null && player.isPlaying()) return;

        Mode currentMode = currentMode();
        if (currentMode == null) return;

        switch (currentMode) {
            case DELETE -> {
                runCommand(new RemoveEdgeCommand(canvas, edge));
                kickLiveLayout(1);
            }
            case PAINT -> edge.setUserStrokeColor(colorPicker.getValue());
            default -> {}
        }
    }

    //------------------------------------------------

    private Mode currentMode() {
        Toggle tog = modeGroup.getSelectedToggle();
        if (tog == null) return null;
        return Mode.fromLabel(((ToggleButton) tog).getText());
    }

    void selectMode(Mode mode) {
        for(Toggle tog: modeGroup.getToggles()) {
            if(tog instanceof Labeled) {
                Labeled labeled = (Labeled) tog;
                if (labeled.getText().equals(mode.label())){
                    modeGroup.selectToggle(tog);
                    return;
                }
            }
        }
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

        boolean isTxt = (Objects.equals(decideExt(file, chooser.getSelectedExtensionFilter()), ".txt"));

        clearSelection();
        try {
            if (isTxt) GraphImporterTXT.importFromTxt(file, buildContext());
            else GraphImporterJSON.importFrom(file, buildContext());

            history.clear();
            refreshUndoRedoState();
            kickLiveLayout(1);
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
        FileChooser.ExtensionFilter tikzFilter = new FileChooser.ExtensionFilter("TikZ Picture", "*.tex");
        chooser.getExtensionFilters().addAll(txtFilter, jsonFilter, tikzFilter);
        chooser.setSelectedExtensionFilter(txtFilter);
        chooser.setInitialFileName("graph.txt");

        File file = chooser.showSaveDialog(graphPane.getScene().getWindow());
        if (file == null) return;

        String ext = decideExt(file, chooser.getSelectedExtensionFilter());

        if (!file.getName().toLowerCase().endsWith(ext)) {
            file = new File(file.getParentFile(), file.getName() + ext);
        }

        try {
            if (ext.equals(".txt")) GraphExporterTXT.exportToTxt(canvas, file);
            if (ext.equals(".json")) GraphExporterJSON.export(canvas, file);
            if (ext.equals(".tex")) GraphExporterTikZ.export(canvas, canvas.isDirected(), file);
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage(), ButtonType.OK);
            a.setHeaderText("Export exception");
            a.showAndWait();
        }
    }

    @FXML
    private void onToggleDirected() {
        canvas.setDirected(directedCheckbox.isSelected());
    }

    // ─── Color / Paint handlers ────────────────────────────────────────────────

    @FXML
    private void onResetAllColors() {
        for (VertexDrawn v : canvas.getVertices()) v.setUserFillColor(null);
        for (EdgeDrawn e : canvas.getEdges()) e.setUserStrokeColor(null);
    }

    // ──────────────────────────────────────────────────────────────────────────

    private static String decideExt(File file, FileChooser.ExtensionFilter selected) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".txt"))  return ".txt";
        if (name.endsWith(".json")) return ".json";
        if (name.endsWith(".tex")) return ".tex";
        if (selected != null) {
            for (String ext : selected.getExtensions()) {
                if (ext.equalsIgnoreCase("*.txt"))  return ".txt";
                if (ext.equalsIgnoreCase("*.json")) return ".json";
                if (ext.equalsIgnoreCase("*.tex")) return ".tex";
            }
        }
        return null;
    }

    //####################################################
    //##################### TESTY ########################
    //####################################################

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
        if (player == null) player = new AlgorithmPlayer(canvas);
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        GreedyVC<String> algorithm = new GreedyVC<>();
        GreedyVCResult<String> result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildGreedyColoringTrack(result, graph);

        player.loadTrack(track);
        player.play();
    }

    //####################################################
    //##################### KONTROLER ####################
    //####################################################

    private Graph<String> buildMathematicalGraph() {
        // TODO: W przyszłości zmienimy na tworzenie WeightedGraph, jeśli algorytm to wymusi.
        Graph<String> mathGraph = new Graph<>("CanvasGraph");
        Map<String, Vertex<String>> dictionary = new HashMap<>();

        for (VertexDrawn vd : canvas.getVertices()) {
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

    private void resetCanvasStyles() {
        canvas.resetAllStyles();
    }

    /**
     * Główna metoda sterująca uruchamianiem i animowaniem algorytmów
     * zależnych od wierzchołka startowego.
     */
    private void runAndAnimateAlgorithm(VertexDrawn startVisualNode, String algorithmType) {
        if (player == null) player = new AlgorithmPlayer(canvas);

        Graph<String> graph = buildMathematicalGraph();

        Vertex<String> startNode = null;
        for (Vertex<String> v : graph.getVertices()) {
            if (v.getValue().equals(startVisualNode.getVertexId())) { startNode = v; break; }
        }
        if (startNode == null) return;

        AlgorithmTrack generatedTrack = null;

        try {
            if (algorithmType.equals("BFS")) {
                BFSResult<String> result = new BFS<>(startNode).start(graph);
                generatedTrack = TrackFactory.buildBfsTrack(result, graph);
            }
            else if (algorithmType.equals("DFS")) {
                DFSResult<String> result = new DFS<>(startNode).start(graph);
                generatedTrack = TrackFactory.buildDfsTrack(result, graph);
            }
            else if (algorithmType.equals("DIJKSTRA")) {
                // DijkstraAlgorithm<String> algorithm = new DijkstraAlgorithm<>(startNode, null);
                // DijkstraAlgorithmResult<String> result = algorithm.start(graph);
                // generatedTrack = TrackFactory.buildDijkstraTrack(result, graph);
            }
        } catch (Exception e) {
            System.err.println("Błąd wykonania algorytmu: " + e.getMessage());
        }

        if (generatedTrack != null) {
            player.loadTrack(generatedTrack);
            player.play();
        }
    }
}