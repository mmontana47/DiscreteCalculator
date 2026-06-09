package pl.edu.uj.discretecalculator.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.edu.uj.discretecalculator.AppConfig;
import pl.edu.uj.discretecalculator.algorithm.*;
import pl.edu.uj.discretecalculator.exception.TopologicalSortException;
import pl.edu.uj.discretecalculator.io.*;
import pl.edu.uj.discretecalculator.model.graph.*;
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
import pl.edu.uj.discretecalculator.view.layout.ForceDirectedLayout;

public class MainController {

    // =================================================================================================
    // ========================================= POLA I KOMPONENTY =====================================
    // =================================================================================================

    private CanvasManager canvas;
    private final CommandHistory history = new CommandHistory();
    private VertexDrawn source = null;
    private ViewZoom viewZoom;
    private double lastMouseX, lastMouseY;
    private boolean panDragged = false;
    private final double panLimit = AppConfig.get().interaction.panLimit;
    private double lastPanX, lastPanY;
    private final DoubleProperty currentSpeedMs = new SimpleDoubleProperty();
    private final double DEFAULT_SPEED_MS = AppConfig.get().animation.defaultSpeedMs;
    private int customRenumberCounter = 0;
    private final Map<VertexDrawn, String> originalIdsBackup = new HashMap<>();

    private AlgorithmPlayer player;

    private Timeline timeline = new Timeline();
    private double temperature;

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
    @FXML private Slider vertexSizeSlider;
    @FXML private Slider edgeWidthSlider;
    @FXML public Button autoLayout;
    @FXML private ToggleButton liveLayout;
    @FXML private VBox inputPanel;
    @FXML private TextArea edgeInput;
    @FXML private ColorPicker colorPicker;
    @FXML private CheckBox weightedCheckbox;
    @FXML private CheckBox directedCheckbox;
    @FXML private ToolBar fileToolBar;
    @FXML private ToolBar buildToolBar;
    @FXML private ToolBar algorithmsToolBar;
    @FXML private ToggleButton addVertexMode;
    @FXML private ToggleButton addEdgeMode;
    @FXML private ToggleButton deleteMode;
    @FXML private ToggleButton editWeightMode;
    @FXML private Button resetWeightsBtn;
    @FXML private Label stepCounterLabel;
    @FXML private Slider speedSlider;
    @FXML private Label speedLabel;

    // =================================================================================================
    // ==================================== INICJALIZACJA KONTROLERA ===================================
    // =================================================================================================

    @FXML
    private void initialize() {
        ribbon.getSelectionModel().select(editGraphTab);
        canvas = new CanvasManager(graphPane, countsLabel);
        refreshUndoRedoState();
        viewZoom = new ViewZoom(graphPane, canvas);
        canvas.weightedProperty().bind(weightedCheckbox.selectedProperty());
        modeGroup.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {
                    clearSelection();

                    if (oldValue != null) {
                        Mode oldMode = Mode.fromLabel(((ToggleButton) oldValue).getText());
                        if (oldMode != null && oldMode.label().equals("Custom Renumber")) {
                            if (customRenumberCounter < canvas.getVertices().size()) {
                                abortCustomRenumbering();
                            } else {
                                finishCustomRenumbering();
                            }
                        }
                    }

                    if (newValue == null) {
                        modeLabel.setText("Mode: -");
                        hintLabel.setText("Select a mode to start");
                    } else {
                        ToggleButton btn = (ToggleButton) newValue;
                        Mode m = Mode.fromLabel(btn.getText());
                        if(m == null) return;
                        modeLabel.setText("Mode: " + m.label());
                        hintLabel.setText(m.hint());

                        if (m.label().equals("Custom Renumber")) {
                            startCustomRenumbering();
                        }
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
                            else {
                                onResetView();
                                onStopAnimation();
                                updateStepLabel();
                            }
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

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double multiplier = newVal.doubleValue();
            currentSpeedMs.set(DEFAULT_SPEED_MS / multiplier);
            speedLabel.setText(String.format(java.util.Locale.US, "%.1fx", multiplier));
        });

        currentSpeedMs.set(DEFAULT_SPEED_MS / speedSlider.getValue());
        speedLabel.setText(String.format(java.util.Locale.US, "%.1fx", speedSlider.getValue()));

        vertexSizeSlider.setMin(StyleSettings.MIN_VERTEX_RADIUS);
        vertexSizeSlider.setMax(StyleSettings.MAX_VERTEX_RADIUS);
        vertexSizeSlider.setValue(StyleSettings.get().getVertexRadius());
        Bindings.bindBidirectional(vertexSizeSlider.valueProperty(), StyleSettings.get().vertexRadiusProperty());

        edgeWidthSlider.setMin(StyleSettings.MIN_EDGE_WIDTH);
        edgeWidthSlider.setMax(StyleSettings.MAX_EDGE_WIDTH);
        edgeWidthSlider.setValue(StyleSettings.get().getEdgeWidth());
        Bindings.bindBidirectional(edgeWidthSlider.valueProperty(), StyleSettings.get().edgeWidthProperty());

        setupInputPanel();
        weightedCheckbox.selectedProperty().addListener((observable, oldValue, isVisible) -> {
            for (EdgeDrawn ed : canvas.getEdges()) {
                if (!isVisible) {
                    ed.getWeightLabel().setVisible(false);
                } else {
                    ed.getWeightLabel().setVisible(ed.getWeightText() != null && !ed.getWeightText().isEmpty());
                }
            }
        });
    }

    // =================================================================================================
    // ==================================== PLIKI I STAN APLIKACJI =====================================
    // =================================================================================================

    @FXML
    private void newGraph() {
        clearSelection();
        canvas.clear();
        history.clear();
        refreshUndoRedoState();
        directedCheckbox.setSelected(false);
        canvas.setDirected(false);
        setWindowTitle(null);
    }

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
            setWindowTitle(file.getName());
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
        if (ext == null) ext = ".txt";

        if (!file.getName().toLowerCase().endsWith(ext)) {
            file = new File(file.getParentFile(), file.getName() + ext);
        }

        try {
            if (ext.equals(".txt")) GraphExporterTXT.exportToTxt(canvas, file);
            if (ext.equals(".json")) GraphExporterJSON.export(canvas, file);
            if (ext.equals(".tex")) GraphExporterTikZ.export(canvas, canvas.isDirected(), file);
            setWindowTitle(file.getName());
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage(), ButtonType.OK);
            a.setHeaderText("Export exception");
            a.showAndWait();
        }
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    private void setWindowTitle(String filename) {
        Stage stage = (Stage) graphPane.getScene().getWindow();
        if (stage == null) return;
        stage.setTitle(filename != null ? "DiscreteCalculator – " + filename : "DiscreteCalculator");
    }

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

    // =================================================================================================
    // ===================================== WIDOK I UKŁAD (LAYOUT) ====================================
    // =================================================================================================

    @FXML
    private void onSelectLightTheme() { applyTheme(Theme.LIGHT); }

    @FXML
    private void onSelectDarkTheme()  { applyTheme(Theme.DARK);  }

    private void applyTheme(Theme theme) {
        var scene = graphPane.getScene();
        if (scene == null) return;
        theme.applyTo(scene);
        if (theme == Theme.LIGHT) lightThemeItem.setSelected(true);
        else darkThemeItem.setSelected(true);
    }

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

    @FXML
    private void onResetView() {
        if (player != null) {player.pause(); updateStepLabel();}
        clearSelection();
        resetCanvasStyles();
    }

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
        double t0 = canvas.getGraphPane().getWidth() / 50;
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

    // =================================================================================================
    // ===================================== TRYBY PRACY (MODES) =======================================
    // =================================================================================================

    @FXML
    private void onSelectAlgorithmMode(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        selectModeToggle(item.getText());
    }

    private void selectModeToggle(String label) {
        for (Toggle toggle : modeGroup.getToggles()) {
            ToggleButton btn = (ToggleButton) toggle;
            if (btn.getText().equals(label)) {
                modeGroup.selectToggle(toggle);
                return;
            }
        }
    }

    private Mode currentMode() {
        Toggle tog = modeGroup.getSelectedToggle();
        if (tog == null) return null;
        return Mode.fromLabel(((ToggleButton) tog).getText());
    }

    void selectMode(Mode mode) {
        for(Toggle tog: modeGroup.getToggles()) {
            if (tog instanceof Labeled labeled) {
                if (labeled.getText().equals(mode.label())) {
                    modeGroup.selectToggle(tog);
                    return;
                }
            }
        }
    }

    private void clearSelection() {
        if (source != null) {
            source.unselect();
            source = null;
        }
    }

    // =================================================================================================
    // ====================================== INTERAKCJE MYSZĄ =========================================
    // =================================================================================================

    private void onPaneClick(MouseEvent e) {
        if (panDragged) {
            panDragged = false;
            return;
        }

        if (currentMode() == Mode.CUSTOM_RENUMBER) {
            abortCustomRenumbering();
            selectMode(Mode.MOVE);
            return;
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
            case PAINT -> runCommand(new PaintVertexCommand(vertex, colorPicker));
            case CUSTOM_RENUMBER -> {
                if (vertex.getVertexId().endsWith("*")) {
                    vertex.setVertexId(String.valueOf(customRenumberCounter++));
                    vertex.setFillColor(null);

                    if (customRenumberCounter >= canvas.getVertices().size()) {
                        selectMode(Mode.MOVE);
                    }
                }
            }
            default -> {
                if (currentMode.label().equals("Run Dijkstra")) runAndAnimateAlgorithm(vertex, "DIJKSTRA");
                else if (currentMode.label().equals("Run Bellman-Ford")) runAndAnimateAlgorithm(vertex, "BELLMAN_FORD");
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
            case PAINT -> runCommand(new PaintEdgeCommand(edge, colorPicker));
            case EDIT_WEIGHT -> {
                OptionalDouble newWeight = promptForDouble("Edge Weight", "Change weight for selected edge", "Weight");
                if (newWeight.isPresent()) {
                    if (!weightedCheckbox.isSelected()) {
                        weightedCheckbox.setSelected(true);
                    }
                    String weightStr = String.valueOf(newWeight.getAsDouble());
                    runCommand(new ChangeWeightCommand(edge, weightStr));
                }
            }
            case CUSTOM_RENUMBER -> {}
            default -> {}
        }
    }

    // =================================================================================================
    // ========================================== KOMENDY (UNDO/REDO) ==================================
    // =================================================================================================

    private void runCommand(Command cmd) {
        history.execute(cmd);
        refreshUndoRedoState();
    }

    private void refreshUndoRedoState() {
        undoItem.setDisable(!history.canUndo());
        redoItem.setDisable(!history.canRedo());
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

    // =================================================================================================
    // ===================================== GENERATORY GRAFÓW =========================================
    // =================================================================================================

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
        kickLiveLayout(5);
    }

    @FXML private void onBuildBipartite() {
        clearSelection();
        Optional<int[]> n_m = promptForBipartite();
        if(n_m.isEmpty()) return;
        runCommand(GraphBuilders.bipartite(buildContext(),  n_m.get()[0], n_m.get()[1]));
        kickLiveLayout(5);
    }

    @FXML private void onBuildTree() {
        clearSelection();
        OptionalInt n = promptForInt("Tree", "Build random tree on n vertices", "n");
        if (n.isEmpty()) return;
        runCommand(GraphBuilders.randomTree(buildContext(),  n.getAsInt()));
        kickLiveLayout(3);
    }

    private BuilderContext buildContext() {
        return new BuilderContext(
                canvas,
                this::onVertexClick,
                () -> (currentMode() == Mode.MOVE),
                this::onEdgeClick);
    }

    // =================================================================================================
    // ============================== WŁAŚCIWOŚCI GRAFU I QUICK INPUT ==================================
    // =================================================================================================

    @FXML
    private void onToggleDirected() {
        canvas.setDirected(directedCheckbox.isSelected());
    }

    @FXML
    private void onToggleWeighted() {
        boolean isWeighted = weightedCheckbox.isSelected();
        if (isWeighted) {
            setDefaultWeights();
        }
    }

    @FXML
    private void onResetWeights() {
        for (EdgeDrawn ed : canvas.getEdges()) {
            if (weightedCheckbox.isSelected()) {
                ed.setWeightText("1.0");
            } else {
                ed.setWeightText("");
            }
        }
    }

    private void setDefaultWeights() {
        for (EdgeDrawn ed : canvas.getEdges()) {
            if (ed.getWeightText() == null || ed.getWeightText().isEmpty()) {
                ed.setWeightText("1.0");
            }
        }
    }

    private void setupInputPanel() {
        edgeInput.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.ENTER) return;
            String[] lines = edgeInput.getText().split("\\n");
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
        } else if (tokens.length == 3) {
            try{
                Double.parseDouble(tokens[2]);
            }catch (NumberFormatException e) {return;}
            VertexDrawn s = getOrCreateVertex(tokens[0]);
            VertexDrawn t = getOrCreateVertex(tokens[1]);

            if ((!canvas.isDirected() && !canvas.edgeExists(s, t)) ||
                    (canvas.isDirected() && !canvas.edgeExistsDirected(s, t))) {
                AddEdgeCommand cmd = new AddEdgeCommand(canvas, s, t, this::onEdgeClick);
                history.execute(cmd);

                cmd.getEdge().setWeightText(tokens[2]);
                if(!weightedCheckbox.isSelected()) {weightedCheckbox.setSelected(true); setDefaultWeights();}
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

    @FXML
    private void onResetAllColors() {
        for (VertexDrawn v : canvas.getVertices()) v.setUserFillColor(null);
        for (EdgeDrawn e : canvas.getEdges()) e.setUserStrokeColor(null);
    }

    // =================================================================================================
    // ================================= RENUMEROWANIE WIERZCHOŁKÓW ====================================
    // =================================================================================================

    @FXML
    private void onRenumberBFS() {
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        Vertex<String> startNode = graph.getVertices().iterator().next();
        BFSResult<String> result = new BFS<>(startNode).start(graph);

        List<String> newOrder = result.getVisitOrder().stream()
                .map(Vertex::getValue)
                .toList();

        applyRenumbering(newOrder);
    }

    @FXML
    private void onRenumberDFS() {
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        Vertex<String> startNode = graph.getVertices().iterator().next();
        DFSResult<String> result = new DFS<>(startNode).start(graph);

        List<String> newOrder = result.getVisitOrder().stream()
                .map(Vertex::getValue)
                .toList();

        applyRenumbering(newOrder);
    }

    @FXML
    private void onRenumberDegreeDesc() {
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        List<String> newOrder = graph.getVertices().stream()
                .sorted((v1, v2) -> Integer.compare(
                        graph.getIncidentEdges(v2).size(),
                        graph.getIncidentEdges(v1).size()
                ))
                .map(Vertex::getValue)
                .toList();

        applyRenumbering(newOrder);
    }

    @FXML
    private void onRenumberDegreeAsc() {
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        List<String> newOrder = graph.getVertices().stream()
                .sorted((v1, v2) -> Integer.compare(
                        graph.getIncidentEdges(v1).size(),
                        graph.getIncidentEdges(v2).size()
                ))
                .map(Vertex::getValue)
                .toList();

        applyRenumbering(newOrder);
    }

    private void startCustomRenumbering() {
        customRenumberCounter = 0;
        originalIdsBackup.clear();

        for (VertexDrawn v : canvas.getVertices()) {
            originalIdsBackup.put(v, v.getVertexId());
            v.setVertexId(v.getVertexId() + "*");
            v.setFillColor("#BDC3C7");
        }
    }

    private void abortCustomRenumbering() {
        for (VertexDrawn v : canvas.getVertices()) {
            if (originalIdsBackup.containsKey(v)) {
                v.setVertexId(originalIdsBackup.get(v));
            }
            v.setFillColor(null);
        }
        originalIdsBackup.clear();
    }

    private void finishCustomRenumbering() {
        originalIdsBackup.clear();
        canvas.sortVerticesById();
    }

    private void applyRenumbering(List<String> orderedOldIds) {
        clearSelection();

        for (String oldId : orderedOldIds) {
            VertexDrawn vd = canvas.getVertexById(oldId);
            if (vd != null) {
                vd.setVertexId(oldId + "_temp");
            }
        }

        for (int i = 0; i < orderedOldIds.size(); i++) {
            String tempId = orderedOldIds.get(i) + "_temp";
            VertexDrawn vd = canvas.getVertexById(tempId);
            if (vd != null) {
                vd.setVertexId(String.valueOf(i));
            }
        }
        canvas.sortVerticesById();
    }

    // =================================================================================================
    // ====================================== OBSŁUGA ALGORYTMÓW =======================================
    // =================================================================================================

    private Graph<String> buildMathematicalGraph() {
        Graph<String> mathGraph;

        if (canvas.isDirected()) {
            mathGraph = new WeightedDirectedGraph<>("CanvasGraph");
        } else {
            mathGraph = new WeightedGraph<>("CanvasGraph");
        }

        Map<String, Vertex<String>> dictionary = new HashMap<>();

        for (VertexDrawn vd : canvas.getVertices()) {
            Vertex<String> v = new Vertex<>(Integer.parseInt(vd.getVertexId()), vd.getVertexId());
            mathGraph.addVertex(v);
            dictionary.put(vd.getVertexId(), v);
        }

        for (EdgeDrawn ed : canvas.getEdges()) {
            Vertex<String> source = dictionary.get(ed.getSource().getVertexId());
            Vertex<String> target = dictionary.get(ed.getTarget().getVertexId());

            double weight = 1.0;
            try {
                if (ed.getWeightText() != null && !ed.getWeightText().isEmpty()) {
                    weight = Double.parseDouble(ed.getWeightText());
                }
            } catch (NumberFormatException ignored) {}

            int parsedEdgeId = Integer.parseInt(ed.getEdgeId());

            Edge<String> edge;
            if (canvas.isDirected()) {
                edge = new WeightedDirectedEdge<>(source, target, parsedEdgeId, weight);
            } else {
                edge = new WeightedEdge<>(source, target, parsedEdgeId, weight);
            }
            mathGraph.addEdge(edge);
        }
        return mathGraph;
    }

    private void resetCanvasStyles() {
        canvas.resetAllStyles();
    }

    private void runAndAnimateAlgorithm(VertexDrawn startVisualNode, String algorithmType) {
        ensurePlayer();

        Graph<String> graph = buildMathematicalGraph();

        Vertex<String> startNode = null;
        for (Vertex<String> v : graph.getVertices()) {
            if (v.getValue().equals(startVisualNode.getVertexId())) { startNode = v; break; }
        }
        if (startNode == null) return;

        AlgorithmTrack generatedTrack = null;

        try {
            switch (algorithmType) {
                case "BFS" -> {
                    BFSResult<String> result = new BFS<>(startNode).start(graph);
                    generatedTrack = TrackFactory.buildBfsTrack(result, graph);
                }
                case "DFS" -> {
                    DFSResult<String> result = new DFS<>(startNode).start(graph);
                    generatedTrack = TrackFactory.buildDfsTrack(result, graph);
                }
                case "DIJKSTRA" -> {
                    boolean hasNegativeWeight = false;
                    for (pl.edu.uj.discretecalculator.view.EdgeDrawn ed : canvas.getEdges()) {
                        if (ed.getWeightText() != null && !ed.getWeightText().isEmpty()) {
                            try {
                                if (Double.parseDouble(ed.getWeightText().replace(",", ".")) < 0) {
                                    hasNegativeWeight = true;
                                    break;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    if (hasNegativeWeight) {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Dijkstra's algorithm cannot handle negative edge weights. Please use the Bellman-Ford algorithm instead.",
                                ButtonType.OK);
                        alert.setHeaderText("Negative Weight Detected");
                        alert.showAndWait();
                        return;
                    }

                    DijkstraAlgorithm<String> algorithm = new DijkstraAlgorithm<>(startNode);
                    var result = algorithm.start(graph);
                    generatedTrack = TrackFactory.buildDijkstraTrack(result, graph);
                }
                case "BELLMAN_FORD" -> {
                    BellmanFordAlgorithm<String> algorithm = new BellmanFordAlgorithm<>(startNode);
                    var result = algorithm.start(graph);
                    generatedTrack = TrackFactory.buildBellmanFordTrack(result, graph);
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Algorithm execution error: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK);
            alert.setHeaderText("Algorithm Error");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Unexpected execution error: " + e.getMessage());
        }

        if (generatedTrack != null) {
            player.loadTrack(generatedTrack);
            setAnimationModeUI(true);
            player.play();
        }
    }

    @FXML
    private void onConvertToLineGraph() {
        if (canvas.isDirected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Graf krawędziowy w tej implementacji obsługuje tylko grafy nieskierowane.\nOdznacz opcję 'Directed' i spróbuj ponownie.",
                    ButtonType.OK);
            alert.setHeaderText("Tryb nieskierowany wymagany");
            alert.showAndWait();
            return;
        }

        Graph<String> graph = buildMathematicalGraph();
        if (graph.getEdges().isEmpty()) return;

        Map<String, javafx.geometry.Point2D> midpoints = new HashMap<>();
        for (EdgeDrawn ed : canvas.getEdges()) {
            double mx = (ed.getSource().getLayoutX() + ed.getTarget().getLayoutX()) / 2.0;
            double my = (ed.getSource().getLayoutY() + ed.getTarget().getLayoutY()) / 2.0;
            midpoints.put(ed.getEdgeId(), new javafx.geometry.Point2D(mx, my));
        }

        Graph<Edge<String>> lineGraph = GraphToLineGraph.Convert(graph);

        canvas.clear();
        history.clear();
        refreshUndoRedoState();
        weightedCheckbox.setSelected(false);

        Map<String, VertexDrawn> newVertices = new HashMap<>();
        for (Vertex<Edge<String>> v : lineGraph.getVertices()) {
            String oldEdgeId = String.valueOf(v.getValue().getId());

            javafx.geometry.Point2D pos = midpoints.getOrDefault(oldEdgeId, new javafx.geometry.Point2D(400, 300));

            VertexDrawn newV = canvas.createVertex(
                    pos.getX(),
                    pos.getY(),
                    oldEdgeId,
                    this::onVertexClick,
                    () -> currentMode() == Mode.MOVE
            );
            newVertices.put(oldEdgeId, newV);
        }

        for (Edge<Edge<String>> e : lineGraph.getEdges()) {
            String uId = String.valueOf(e.getSource().getValue().getId());
            String vId = String.valueOf(e.getTarget().getValue().getId());

            VertexDrawn source = newVertices.get(uId);
            VertexDrawn target = newVertices.get(vId);

            if (source != null && target != null) {
                canvas.createEdge(source, target, this::onEdgeClick);
            }
        }
    }

    @FXML
    private void onRunGreedyColoring() {
        ensurePlayer();
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        GreedyVertexColoring<String> algorithm = new GreedyVertexColoring<>(null);
        var result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildGreedyColoringTrack(result, graph);

        player.loadTrack(track);
        setAnimationModeUI(true);
        player.play();
    }

    @FXML
    private void onRunGreedyEdgeColoring() {
        ensurePlayer();
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getEdges().isEmpty()) return;

        GreedyEdgeColoring<String> algorithm = new GreedyEdgeColoring<>();
        var result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildGreedyEdgeColoringTrack(result, graph);

        player.loadTrack(track);
        setAnimationModeUI(true);
        player.play();
    }

    private boolean confirmBacktracking(int currentSize, int safeLimit, String elementType) {
        if (currentSize <= safeLimit) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setHeaderText("Your graph has " + currentSize + " " + elementType);
        alert.setContentText("""
        Animating backtracking may result in the app crashing.\n
        It is recommended to make sure that the chromatic number/index is reasonably bounded.\n
        Do you want to continue?""");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void onRunBacktrackingVertexColoring() {
        ensurePlayer();
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        if (!confirmBacktracking(graph.getVertices().size(), 15, "vertices")) {
            return;
        }
        BacktrackingAlgorithmForVertices<String> algorithm = new BacktrackingAlgorithmForVertices<>(null);
        var result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildBacktrackingVertexTrack(result, graph);

        player.loadTrack(track);
        setAnimationModeUI(true);
        player.play();
    }

    @FXML
    private void onRunBacktrackingEdgeColoring() {
        ensurePlayer();
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getEdges().isEmpty()) return;

        if (!confirmBacktracking(graph.getEdges().size(), 15, "edges")) {
            return;
        }

        BacktrackingAlgorithmForEdges<String> algorithm = new BacktrackingAlgorithmForEdges<>();
        var result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildBacktrackingEdgeTrack(result, graph);

        player.loadTrack(track);
        setAnimationModeUI(true);
        player.play();
    }

    @FXML
    private void onRunSCC() {
        ensurePlayer();
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        StronglyConnectedComponent<String> algorithm = new StronglyConnectedComponent<>();
        var result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildSccTrack(result, graph);

        player.loadTrack(track);
        setAnimationModeUI(true);
        player.play();
    }

    @FXML
    private void onRunTopoSort() {
        ensurePlayer();
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        TopologicalSort<String> algorithm = new TopologicalSort<>();
        try {
            var result = algorithm.start(graph);
            AlgorithmTrack track = TrackFactory.buildTopoSortTrack(result, graph);
            player.loadTrack(track);
            setAnimationModeUI(true);
            player.play();
        } catch (TopologicalSortException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK);
            alert.setHeaderText("Topological Sort Error");
            alert.showAndWait();
        }
    }

    @FXML
    private void onRunKosaraju() {
        ensurePlayer();
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        if (!canvas.isDirected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Kosaraju's algorithm requires a directed graph.\nEnable the 'Directed' option and try again.",
                    ButtonType.OK);
            alert.setHeaderText("Undirected Graph");
            alert.showAndWait();
            return;
        }

        KosarajuAlgorithm<String> algorithm = new KosarajuAlgorithm<>();
        KosarajuAlgorithmResult<String> result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildKosarajuTrack(result, graph);

        player.loadTrack(track);
        setAnimationModeUI(true);
        player.play();
    }

    // =================================================================================================
    // ===================================== OBSŁUGA ANIMACJI ==========================================
    // =================================================================================================

    private void ensurePlayer() {
        if (player == null) {
            player = new AlgorithmPlayer(canvas, currentSpeedMs);
            player.setOnStepChanged(this::updateStepLabel);
        }
    }

    private void updateStepLabel(){
        int num = player.getCurrentStep();
        int total = player.getNumberOfSteps();
        if(total==0){
            stepCounterLabel.setVisible(false);
            stepCounterLabel.setManaged(false);
        }
        else{
            stepCounterLabel.setVisible(true);
            stepCounterLabel.setManaged(true);
            stepCounterLabel.setText("Step: " + num + " / " + total);
        }
    }

    @FXML
    private void onTestPlay() {
        if (player != null) player.play();
    }

    @FXML
    private void onTestPlayBackward() {
        if (player != null) player.playBackward();
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

    private void setAnimationModeUI(boolean isAnimating) {
        fileToolBar.setDisable(isAnimating);
        buildToolBar.setDisable(isAnimating);
        algorithmsToolBar.setDisable(isAnimating);

        inputPanel.setDisable(isAnimating);

        addVertexMode.setDisable(isAnimating);
        addEdgeMode.setDisable(isAnimating);
        deleteMode.setDisable(isAnimating);
        editWeightMode.setDisable(isAnimating);

        directedCheckbox.setDisable(isAnimating);
        weightedCheckbox.setDisable(isAnimating);
        if (resetWeightsBtn != null) resetWeightsBtn.setDisable(isAnimating);

        if (isAnimating) {
            Mode current = currentMode();
            if (current == Mode.ADD_VERTEX || current == Mode.ADD_EDGE ||
                    current == Mode.DELETE || current == Mode.EDIT_WEIGHT || current == Mode.CUSTOM_RENUMBER) {
                selectMode(Mode.MOVE);
            }
        }
    }

    @FXML
    private void onStopAnimation() {
        if (player != null) {
            player.loadTrack(null);
        }
        resetCanvasStyles();
        clearSelection();
        setAnimationModeUI(false);
        updateStepLabel();
    }

    // =================================================================================================
    // =================================== FUNKCJE POMOCNICZE (UTILITY) ================================
    // =================================================================================================

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

    private OptionalDouble promptForDouble(String title, String header, String var) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle(title);
        dlg.setHeaderText(header);
        dlg.setGraphic(null);
        dlg.setContentText(var + " =");
        Platform.runLater(() -> dlg.getEditor().requestFocus());
        Optional<String> r = dlg.showAndWait();
        if (r.isEmpty()) return OptionalDouble.empty();
        try {
            double n = Double.parseDouble(r.get().trim().replace(",", "."));
            return OptionalDouble.of(n);
        } catch (NumberFormatException ex) {
            return OptionalDouble.empty();
        }
    }

    private static boolean isPositiveInt(String s){
        try {
            return Integer.parseInt(s.trim())>0;
        }
        catch (NumberFormatException ex) {return false;}
    }


}