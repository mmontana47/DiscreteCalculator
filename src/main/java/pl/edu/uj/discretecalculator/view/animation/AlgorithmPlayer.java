package pl.edu.uj.discretecalculator.view.animation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import pl.edu.uj.discretecalculator.controller.CanvasManager;

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
        updateMetronomeSpeed(currentSpeedMs);
    }

    public void setSpeed(double millis) {
        if (millis <= 0) return;
        this.currentSpeedMs = millis;
        updateMetronomeSpeed(millis);
    }

    private void updateMetronomeSpeed(double millis) {
        boolean wasPlaying = metronome.getStatus() == Animation.Status.RUNNING;
        metronome.stop();

        metronome.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(millis), event -> stepForward())
        );

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

    public boolean isPlaying() {
        return metronome.getStatus() == Animation.Status.RUNNING;
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

    // TUTAJ ZADZIAŁAŁA MAGIA POLIMORFIZMU!
    private void applyFrame(AlgorithmFrame frame) {
        // Odtwarzacz po prostu rzuca klatkę na płótno i mówi: "Narysuj się!"
        frame.apply(canvas);

        // W przyszłości możesz tu np. zaktualizować Label w UI:
        // uiController.setHintLabel(frame.getStepDescription());
    }
}