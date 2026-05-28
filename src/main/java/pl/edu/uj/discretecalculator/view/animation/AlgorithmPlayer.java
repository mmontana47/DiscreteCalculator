package pl.edu.uj.discretecalculator.view.animation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.Map;

public class AlgorithmPlayer {
    private final CanvasManager canvas;
    private AlgorithmTrack currentTrack;
    private int currentIndex = -1;
    private final Timeline metronome;
    private double currentSpeedMs = 500; // Domyślna prędkość: 500ms na krok

    public AlgorithmPlayer(CanvasManager canvas) {
        this.canvas = canvas;

        this.metronome = new Timeline();
        this.metronome.setCycleCount(Animation.INDEFINITE);
        // Inicjalizacja metronomu z domyślną prędkością
        updateMetronomeSpeed(currentSpeedMs);
    }

    /**
     * Zmienia prędkość metronomu w locie (w milisekundach).
     * @param millis czas trwania jednego kroku animacji
     */
    public void setSpeed(double millis) {
        if (millis <= 0) return;
        this.currentSpeedMs = millis;
        updateMetronomeSpeed(millis);
    }

    private void updateMetronomeSpeed(double millis) {
        // Sprawdzamy, czy animacja była w trakcie odtwarzania
        boolean wasPlaying = metronome.getStatus() == Animation.Status.RUNNING;

        // Zatrzymujemy metronom na czas modyfikacji
        metronome.stop();

        // Czyścimy starą klatkę i wstrzykujemy nową z nowym czasem trwania
        metronome.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(millis), event -> stepForward())
        );

        // Jeśli animacja grała, wznawiamy ją automatycznie z nowym tempem
        if (wasPlaying) {
            metronome.play();
        }
    }

    public void loadTrack(AlgorithmTrack track) {
        this.currentTrack = track;
        this.currentIndex = -1;
        this.metronome.stop();
        canvas.resetAllStyles();
    }

    public void play() {
        if (currentTrack != null) metronome.play();
    }

    public void pause() {
        metronome.pause();
    }

    public void stepForward() {
        if (currentTrack == null || currentIndex >= currentTrack.size() - 1) {
            pause();
            return;
        }
        currentIndex++;
        applyFrame(currentTrack.getFrames().get(currentIndex));
    }

    public void stepBackward() {
        if (currentTrack == null || currentIndex <= 0) return;
        currentIndex--;
        rebuildStateToCurrentIndex();
    }

    private void rebuildStateToCurrentIndex() {
        canvas.resetAllStyles();
        for (int i = 0; i <= currentIndex; i++) {
            applyFrame(currentTrack.getFrames().get(i));
        }
    }

    private void applyFrame(AlgorithmFrame frame) {
        // Kolorowanie wierzchołków
        for (Map.Entry<String, String> entry : frame.getVertexColors().entrySet()) {
            VertexDrawn vd = canvas.getVertexById(entry.getKey());
            if (vd != null) vd.setFillColor(entry.getValue());
        }

        // Kolorowanie krawędzi
        for (Map.Entry<String, String> entry : frame.getEdgeColors().entrySet()) {
            EdgeDrawn ed = canvas.getEdgeById(entry.getKey());
            if (ed != null) ed.setStrokeColor(entry.getValue());
        }

        // Teksty (np. dystanse Dijkstry)
        for (Map.Entry<String, String> entry : frame.getVertexLabels().entrySet()) {
            VertexDrawn vd = canvas.getVertexById(entry.getKey());
            if (vd != null) vd.setBottomLabelText(entry.getValue());
        }

        // Wagi krawędzi
        for (Map.Entry<String, String> entry : frame.getEdgeWeights().entrySet()) {
            EdgeDrawn ed = canvas.getEdgeById(entry.getKey());
            if (ed != null) ed.setWeightText(entry.getValue());
        }

        // Pozycje (Topological Sort)
        for (Map.Entry<String, AlgorithmFrame.Coordinate> entry : frame.getVertexPositions().entrySet()) {
            VertexDrawn vd = canvas.getVertexById(entry.getKey());
            if (vd != null) {
                vd.setX(entry.getValue().x());
                vd.setY(entry.getValue().y());
            }
        }
    }
}