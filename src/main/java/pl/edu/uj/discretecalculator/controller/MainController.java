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
    private CanvasManager canvas;
    private final CommandHistory history = new CommandHistory();
    private VertexDrawn source = null;
    private ViewZoom viewZoom;
    private double lastMouseX, lastMouseY;
    private boolean panDragged = false;
    private static final double panLimit = 5.0;
    private double lastPanX, lastPanY;
    private final DoubleProperty currentSpeedMs = new SimpleDoubleProperty();
    private final double DEFAULT_SPEED_MS = 500.0;
    private int customRenumberCounter = 0;
    private final Map<VertexDrawn, String> originalIdsBackup = new HashMap<>();
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
    @FXML public ComboBox<SpeedOption> speedComboBox;
    @FXML private ToolBar fileToolBar;
    @FXML private ToolBar buildToolBar;
    @FXML private ToolBar algorithmsToolBar;
    @FXML private ToggleButton addVertexMode;
    @FXML private ToggleButton addEdgeMode;
    @FXML private ToggleButton deleteMode;
    @FXML private ToggleButton editWeightMode;
    @FXML private Button resetWeightsBtn;


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

                    // --- SPRZĄTANIE (Wychodzenie z trybu) ---
                    if (oldValue != null) {
                        Mode oldMode = Mode.fromLabel(((ToggleButton) oldValue).getText());
                        if (oldMode != null && oldMode.label().equals("Custom Renumber")) {
                            // Jeśli nie wyklikano wszystkich, to znaczy że użytkownik przerwał
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

                        // --- INICJALIZACJA (Wejście w tryb) ---
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

        speedComboBox.getItems().addAll(
                new SpeedOption("0.5x", 0.5),
                new SpeedOption("1.0x", 1.0),
                new SpeedOption("2.0x", 2.0),
                new SpeedOption("5.0x", 5.0),
                new SpeedOption("10.0x", 10.0)
        );

        speedComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double calculatedMs =  DEFAULT_SPEED_MS * newVal.multiplier;
                currentSpeedMs.set(calculatedMs);
            }
        });

        speedComboBox.getSelectionModel().select(1);

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
                // Jeśli wyłączamy tryb ważony, ukrywamy etykiety niezależnie od tekstu
                if (!isVisible) {
                    ed.getWeightLabel().setVisible(false);
                } else {
                    // Jeśli włączamy, pokazujemy je tylko wtedy, gdy mają tekst
                    ed.getWeightLabel().setVisible(ed.getWeightText() != null && !ed.getWeightText().isEmpty());
                }
            }
        });
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

    //graph visfual properties
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

        if (currentMode() == Mode.CUSTOM_RENUMBER) {
            abortCustomRenumbering();
            selectMode(Mode.MOVE); // Bezpieczny powrót
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

                    // Jeśli to był ostatni węzeł -> sukces!
                    if (customRenumberCounter >= canvas.getVertices().size()) {
                        selectMode(Mode.MOVE); // Listener wykryje wyjście i wyczyści backup
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
                OptionalDouble newWeight = promptForDouble("Waga krawędzi", "Zmień wagę dla wybranej krawędzi", "Waga");
                if (newWeight.isPresent()) {
                    // Wymuszamy zaznaczenie checkboxa "Weighted Graph", jeśli użytkownik zaczął ręcznie edytować wagi
                    if (!weightedCheckbox.isSelected()) {
                        weightedCheckbox.setSelected(true);
                    }
                    String weightStr = String.valueOf(newWeight.getAsDouble());
                    runCommand(new ChangeWeightCommand(edge, weightStr));
                }
            }
            case CUSTOM_RENUMBER -> {
                return; // Absolutnie nic nie rób!
            }
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

    @FXML private CheckBox weightedCheckbox;

    @FXML
    private void onToggleWeighted() {
        boolean isWeighted = weightedCheckbox.isSelected();
        if (isWeighted) {
            // Jeśli zaznaczono, dla każdej krawędzi bez wagi ustaw "1.0"
            for (EdgeDrawn ed : canvas.getEdges()) {
                if (ed.getWeightText() == null || ed.getWeightText().isEmpty()) {
                    ed.setWeightText("1.0");
                }
            }
        }
    }

    @FXML
    private void onResetWeights() {
        // Twardy, świadomy reset wszystkich wag do domyślnych wartości
        for (EdgeDrawn ed : canvas.getEdges()) {
            if (weightedCheckbox.isSelected()) {
                // Jeśli wagi są włączone, resetujemy je do domyślnego "1.0"
                // (Dzięki bindWeightVisibility w EdgeDrawn od razu się pojawią)
                ed.setWeightText("1.0");
            } else {
                // Jeśli wagi są wyłączone, po prostu czyścimy ich "pamięć" z powrotem do pustego pola
                ed.setWeightText("");
            }
        }

        // Zabezpieczenie: jeśli jesteś w trybie podglądu algorytmu, warto zaktualizować layout
        kickLiveLayout(1);
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
            // Zamiana przecinków na kropki, by zapobiec błędom formatowania regionalnego
            double n = Double.parseDouble(r.get().trim().replace(",", "."));
            return OptionalDouble.of(n);
        } catch (NumberFormatException ex) {
            return OptionalDouble.empty();
        }
    }

    // ─── Color / Paint handlers ────────────────────────────────────────────────

    @FXML
    private void onResetAllColors() {
        for (VertexDrawn v : canvas.getVertices()) v.setUserFillColor(null);
        for (EdgeDrawn e : canvas.getEdges()) e.setUserStrokeColor(null);
    }

    /**
     * Zamraża lub rozmraża interfejs użytkownika w zależności od tego, czy trwa animacja.
     */
    /**
     * Zamraża krytyczne operacje na grafie podczas trwania animacji.
     */
    private void setAnimationModeUI(boolean isAnimating) {
        // Blokujemy całe grupy (Paski narzędzi)
        fileToolBar.setDisable(isAnimating);
        buildToolBar.setDisable(isAnimating);
        algorithmsToolBar.setDisable(isAnimating);

        // Blokujemy Quick Input z lewej strony
        inputPanel.setDisable(isAnimating);

        // Blokujemy specyficzne przyciski w zakładce Edit Graph
        addVertexMode.setDisable(isAnimating);
        addEdgeMode.setDisable(isAnimating);
        deleteMode.setDisable(isAnimating);
        editWeightMode.setDisable(isAnimating);

        // Blokujemy atrybuty globalne
        directedCheckbox.setDisable(isAnimating);
        weightedCheckbox.setDisable(isAnimating);
        if (resetWeightsBtn != null) resetWeightsBtn.setDisable(isAnimating);

        if (isAnimating) {
            // Bezpiecznik: Jeśli użytkownik był w trybie dodawania/usuwania,
            // wymuszamy przełączenie na bezpieczny tryb MOVE.
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
            player.pause();
            // Możemy ewentualnie zresetować track: player.loadTrack(null);
        }
        resetCanvasStyles();
        clearSelection();
        setAnimationModeUI(false); // Rozmrażamy interfejs!
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

    // --- FUNKCJE PRZENUMEROWANIA (VERTEX ORDERING) ---

    @FXML
    private void onRenumberBFS() {
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        // Zaczynamy od dowolnego wierzchołka (np. tego, który system widzi jako pierwszy)
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

        // Sortowanie malejąco po stopniu wierzchołka (Largest Degree First)
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

        // Sortowanie rosnąco po stopniu wierzchołka (Smallest Degree First)
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
            originalIdsBackup.put(v, v.getVertexId()); // Robimy twardy backup
            v.setVertexId(v.getVertexId() + "*");
            v.setFillColor("#BDC3C7"); // Szary kolor
        }
    }

    // Wywoływane TYLKO gdy użytkownik przerwie proces
    private void abortCustomRenumbering() {
        for (VertexDrawn v : canvas.getVertices()) {
            // Przywracamy oryginalne ID
            if (originalIdsBackup.containsKey(v)) {
                v.setVertexId(originalIdsBackup.get(v));
            }
            v.setFillColor(null); // Resetujemy kolory
        }
        originalIdsBackup.clear();
    }

    // Wywoływane TYLKO gdy użytkownik pomyślnie wyklika wszystkie wierzchołki
    private void finishCustomRenumbering() {
        originalIdsBackup.clear();
        canvas.sortVerticesById();
    }

    /**
     * Bezpiecznie aktualizuje ID wierzchołków na płótnie, unikając kolizji nazw.
     */
    private void applyRenumbering(List<String> orderedOldIds) {
        clearSelection();

        // Faza 1: Zmiana ID na tymczasowe (zapobiega kolizjom, gdy próbujemy nazwać wierzchołek "0", a stary "0" jeszcze istnieje)
        for (String oldId : orderedOldIds) {
            VertexDrawn vd = canvas.getVertexById(oldId);
            if (vd != null) {
                vd.setVertexId(oldId + "_temp");
            }
        }

        // Faza 2: Nadanie właściwych numerów docelowych (0, 1, 2...)
        for (int i = 0; i < orderedOldIds.size(); i++) {
            String tempId = orderedOldIds.get(i) + "_temp";
            VertexDrawn vd = canvas.getVertexById(tempId);
            if (vd != null) {
                vd.setVertexId(String.valueOf(i));
            }
        }
        canvas.sortVerticesById();
    }


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
        if (player == null) player = new AlgorithmPlayer(canvas, currentSpeedMs);
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        // Przekazujemy null jako węzeł startowy - algorytm sam weźmie pierwszy z brzegu
        GreedyVertexColoring<String> algorithm = new GreedyVertexColoring<>(null);
        var result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildGreedyColoringTrack(result, graph);

        player.loadTrack(track);
        setAnimationModeUI(true);
        player.play();
    }

    @FXML
    private void onRunGreedyEdgeColoring() {
        if (player == null) player = new AlgorithmPlayer(canvas, currentSpeedMs);
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

    /**
     * Wyświetla ostrzeżenie przed uruchomieniem algorytmów wykładniczych dla dużych grafów.
     * @return true jeśli użytkownik chce kontynuować, false jeśli anulował.
     */
    private boolean confirmBacktracking(int currentSize, int safeLimit, String elementType) {
        if (currentSize <= safeLimit) {
            return true; // Graf jest mały, puszczamy bez ostrzeżenia
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setHeaderText("Your graph has " + currentSize + " " + elementType);
        alert.setContentText(
                "Animating backtracking may result in the app crashing. \n" +
                        "It is recommended to make sure that the chromatic number/index is reasonably bounded. \nDo you want to continue?"
        );

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void onRunBacktrackingVertexColoring() {
        if (player == null) player = new AlgorithmPlayer(canvas, currentSpeedMs);
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;

        if (!confirmBacktracking(graph.getVertices().size(), 15, "vertices")) {
            return;
        }
        // Przekazujemy null, algorytm obsłuży to prawidłowo
        BacktrackingAlgorithmForVertices<String> algorithm = new BacktrackingAlgorithmForVertices<>(null);
        var result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildBacktrackingVertexTrack(result, graph);

        player.loadTrack(track);
        setAnimationModeUI(true);
        player.play();
    }

    @FXML
    private void onRunBacktrackingEdgeColoring() {
        if (player == null) player = new AlgorithmPlayer(canvas, currentSpeedMs);
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getEdges().isEmpty()) return;

        if (!confirmBacktracking(graph.getVertices().size(), 15, "edges")) {
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
        if (player == null) player = new AlgorithmPlayer(canvas, currentSpeedMs);
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
        if (player == null) player = new AlgorithmPlayer(canvas, currentSpeedMs);
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
            // Obsługa alertu gdy użytkownik próbuje posortować graf z cyklem
            Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK);
            alert.setHeaderText("Błąd TopoSort");
            alert.showAndWait();
        }
    }

    @FXML
    private void onRunKosaraju() {
        if (player == null) player = new AlgorithmPlayer(canvas, currentSpeedMs);
        clearSelection();
        Graph<String> graph = buildMathematicalGraph();
        if (graph.getVertices().isEmpty()) return;


        if (!canvas.isDirected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Algorytm Kosaraju wymaga grafu skierowanego.\nWłącz opcję 'Directed' i spróbuj ponownie.",
                    ButtonType.OK);
            alert.setHeaderText("Graf nieskierowany");
            alert.showAndWait();
            return;
        }

        KosarajuAlgorithm<String> algorithm = new KosarajuAlgorithm<>();
        KosarajuAlgorithmResult<String> result = algorithm.start(graph);
        AlgorithmTrack track = TrackFactory.buildKosarajuTrack(result, graph);

        player.loadTrack(track);
        player.play();
    }

    //####################################################
    //##################### KONTROLER ####################
    //####################################################

    private Graph<String> buildMathematicalGraph() {
        Graph<String> mathGraph;

        // Decyzja o strukturze zależy od stanu przełącznika z UI (DirectedCheckbox)
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

        // Wewnątrz buildMathematicalGraph()
        int edgeId = 0; // Nasza konwencja liczbowa
        for (EdgeDrawn ed : canvas.getEdges()) {
            Vertex<String> source = dictionary.get(ed.getSource().getVertexId());
            Vertex<String> target = dictionary.get(ed.getTarget().getVertexId());

            double weight = 1.0;
            try {
                if (ed.getWeightText() != null && !ed.getWeightText().isEmpty()) {
                    weight = Double.parseDouble(ed.getWeightText());
                }
            } catch (NumberFormatException ignored) {}

            Edge<String> edge;
            if (canvas.isDirected()) {
                edge = new WeightedDirectedEdge<>(source, target, edgeId++, weight);
            } else {
                edge = new WeightedEdge<>(source, target, edgeId++, weight);
            }
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
        if (player == null) player = new AlgorithmPlayer(canvas, currentSpeedMs);

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
                DijkstraAlgorithm<String> algorithm = new DijkstraAlgorithm<>(startNode); // path_end zostało usunięte
                var result = algorithm.start(graph);
                generatedTrack = TrackFactory.buildDijkstraTrack(result, graph);
            }
            else if (algorithmType.equals("BELLMAN_FORD")) {
                BellmanFordAlgorithm<String> algorithm = new BellmanFordAlgorithm<>(startNode);
                var result = algorithm.start(graph);
                generatedTrack = TrackFactory.buildBellmanFordTrack(result, graph);
            }
        } catch (Exception e) {
            System.err.println("Błąd wykonania algorytmu: " + e.getMessage());
        }

        if (generatedTrack != null) {
            player.loadTrack(generatedTrack);
            setAnimationModeUI(true);
            player.play();
        }
    }

    private static class SpeedOption {
        final String label;
        final double multiplier;

        SpeedOption(String label, double multiplier) {
            this.label = label;
            this.multiplier = multiplier;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}