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

    public AlgorithmPlayer(CanvasManager canvas, DoubleProperty currentSpeedMs) {
        this.canvas = canvas;
        this.metronome = new Timeline();
        this.metronome.setCycleCount(Animation.INDEFINITE);

        this.metronome.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(defaultSpeedMs), e -> stepForward())
        );

        currentSpeedMs.addListener((obs, oldVal, newVal) -> {
            double newSpeed = newVal.doubleValue();
            if (newSpeed > 0) {
                metronome.setRate(currentSpeedMs.get()/defaultSpeedMs);
            }
        });

        if(currentSpeedMs.get()>0) {
            metronome.setRate(currentSpeedMs.get()/defaultSpeedMs);
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

    private void applyFrame(AlgorithmFrame frame) {
        frame.apply(canvas);

    }
}