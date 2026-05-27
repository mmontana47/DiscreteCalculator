package pl.edu.uj.discretecalculator.view.animation;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmTrack {
    private final List<AlgorithmFrame> frames = new ArrayList<>();

    public void addFrame(AlgorithmFrame frame) {
        frames.add(frame);
    }

    public List<AlgorithmFrame> getFrames() {
        return frames;
    }

    public int size() {
        return frames.size();
    }
}