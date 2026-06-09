package pl.edu.uj.discretecalculator.view.animation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.util.Duration;
import pl.edu.uj.discretecalculator.AppConfig;
import pl.edu.uj.discretecalculator.controller.CanvasManager;

public class AlgorithmPlayer {
    private final CanvasManager canvas;
    private AlgorithmTrack currentTrack;
    private int currentIndex = -1;
    private final Timeline metronome;
    private final double defaultSpeedMs = AppConfig.get().animation.defaultSpeedMs;
    private Runnable onStepChanged;

    // >>> NOWOŚĆ: Flaga kierunku odtwarzania <<<
    private boolean isForward = true;

    public AlgorithmPlayer(CanvasManager canvas, DoubleProperty currentSpeedMs) {
        this.canvas = canvas;
        this.metronome = new Timeline();
        this.metronome.setCycleCount(Animation.INDEFINITE);

        // Klatka decyduje w którą stronę idziemy na podstawie flagi
        this.metronome.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(defaultSpeedMs), e -> {
                    if (isForward) stepForward();
                    else stepBackward();
                })
        );

        // Ustawianie mnożnika prędkości (Rate = Oczekiwana / Bazowa)
        currentSpeedMs.addListener((obs, oldVal, newVal) -> {
            double newSpeedDuration = newVal.doubleValue();
            if (newSpeedDuration > 0) {
                metronome.setRate(defaultSpeedMs / newSpeedDuration);
            }
        });

        if (currentSpeedMs.get() > 0) {
            metronome.setRate(defaultSpeedMs / currentSpeedMs.get());
        }
    }

    public void loadTrack(AlgorithmTrack track) {
        this.currentTrack = track;
        this.currentIndex = -1;
        this.metronome.stop();
        canvas.resetAllStyles();
        if(onStepChanged != null) {onStepChanged.run();}
    }

    public void play() {
        isForward = true;
        if (currentTrack != null) metronome.play();
    }

    // >>> NOWOŚĆ: Odtwarzanie do tyłu <<<
    public void playBackward() {
        isForward = false;
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
        if(onStepChanged != null) onStepChanged.run();
    }

    public void stepBackward() {
        // Jeśli jesteśmy na początku animacji, zatrzymujemy odtwarzanie do tyłu
        if (currentTrack == null || currentIndex <= 0) {
            pause();
            return;
        }
        currentIndex--;
        rebuildStateToCurrentIndex();
        if(onStepChanged != null) onStepChanged.run();
    }

    private void rebuildStateToCurrentIndex() {
        canvas.resetAllStyles();
        for (int i = 0; i <= currentIndex; i++) {
            applyFrame(currentTrack.getFrames().get(i));
        }
    }

    private void applyFrame(AlgorithmFrame frame) {
        frame.apply(canvas);
    }

    public void setOnStepChanged(Runnable onStepChanged) { this.onStepChanged = onStepChanged; }
    public int getCurrentStep() { return currentIndex + 1; }
    public int getNumberOfSteps() { return (currentTrack != null ? currentTrack.size() : 0); }
}