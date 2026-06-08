package pl.edu.uj.discretecalculator;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.Objects;

public final class AppConfig {

    public static class VertexStyleCfg {
        public double radiusMin;
        public double radiusMax;
        public double radiusDefault;
        public double distanceLabelOffset;
    }

    public static class EdgeStyleCfg {
        public double widthMin;
        public double widthMax;
        public double widthDefault;
        public double activeWidthMultiplier;
        public double bidirectionalOffset;
        public double minEffectiveLength;
    }

    public static class ArrowCfg {
        public double length;
        public double halfWidth;
        public double lenFactor;
        public double halfFactor;
    }

    public static class ColorsCfg {
        public String bfsVisited;
        public String dfsVisited;
        public String treeEdge;
        public double treeEdgeWidth;
        public String cycleEdge;
        public double cycleEdgeWidth;
    }

    public static class StyleCfg {
        public VertexStyleCfg vertex;
        public EdgeStyleCfg edge;
        public ArrowCfg arrow;
        public ColorsCfg colors;
    }

    public static class AnimationCfg {
        public double defaultSpeedMs;
    }

    public static class InteractionCfg {
        public double panLimit;
    }

    public static class ForceDirectedCfg {
        public double eps;
        public double damping;
        public double minTemperature;
        public double kickTemperature;
        public double coolFactor;
        public double kScaleBase;
        public double kScaleRange;
        public double kScaleExponent;
        public double gravityBase;
        public double gravityRange;
        public double maxIdealDivisor;
        public double repulsionThreshold;
        public double attractionFactor;
        public double turbulenceRange;
    }

    public StyleCfg style;
    public AnimationCfg animation;
    public InteractionCfg interaction;
    public ForceDirectedCfg forceDirected;

    private static AppConfig INSTANCE;

    public static AppConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    private static void load() {
        try (var reader = new InputStreamReader(
                Objects.requireNonNull(AppConfig.class.getResourceAsStream("/app-config.json")))) {
            INSTANCE = new Gson().fromJson(reader, AppConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load app-config.json", e);
        }
    }
}
