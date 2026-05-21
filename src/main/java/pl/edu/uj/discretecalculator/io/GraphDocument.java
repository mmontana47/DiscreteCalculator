package pl.edu.uj.discretecalculator.io;

import java.util.List;
import java.util.Map;

public class GraphDocument {
    public GraphPart graph;
    public Map<String, Position> layout;

    public static class GraphPart {
        public boolean isDirected;
        public List<VertexDto> vertices;
        public List<EdgeDto> edges;
    }

    public static class VertexDto {
        public String id;

        public VertexDto() {}
        public VertexDto(String id) { this.id = id; }
    }

    public static class EdgeDto {
        public String source;
        public String target;

        public EdgeDto() {}
        public EdgeDto(String source, String target) {
            this.source = source;
            this.target = target;
        }
    }

    public static class Position {
        public double x;
        public double y;

        public Position() {}
        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
