package pl.edu.uj.discretecalculator.io;

import javafx.scene.paint.Color;
import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.StyleSettings;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.List;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class GraphExporterTikZ {

    private static final double SCALE = 100.0;

    private GraphExporterTikZ() {}

    public static void export(CanvasManager canvas, boolean directed, File file) throws IOException {
        String tex = buildTex(canvas, directed);
        Files.writeString(file.toPath(), tex, StandardCharsets.UTF_8);
    }

    static String buildTex(CanvasManager canvas, boolean directed) {
        double radius = StyleSettings.get().getVertexRadius();

        double maxY = canvas.getVertices().stream()
                .mapToDouble(v -> v.getLayoutY() + radius)
                .max().orElse(0);

        StringBuilder sb = new StringBuilder();

        for (VertexDrawn v : canvas.getVertices()) {
            Color fill = v.getUserFillColor();
            if (fill != null) {
                sb.append("\\definecolor{vc").append(v.getVertexId()).append("}{HTML}{")
                  .append(fill.toString(), 2, 8).append("}\n");
            }
        }

        List<EdgeDrawn> edges = canvas.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            Color stroke = edges.get(i).getUserStrokeColor();
            if (stroke != null) {
                sb.append("\\definecolor{ec").append(i).append("}{HTML}{")
                  .append(stroke.toString(), 2, 8).append("}\n");
            }
        }
        sb.append("\n\\begin{tikzpicture}[\n  scale=2\n]\n");

        for (VertexDrawn v : canvas.getVertices()) {
            double x = (v.getLayoutX() + radius) / SCALE;
            double y = (maxY - v.getLayoutY() - radius) / SCALE;
            String fill = (v.getUserFillColor() != null) ? ", fill=vc" + v.getVertexId() : "";
            sb.append("\\node[circle, draw").append(fill).append("] (")
              .append(v.getVertexId()).append(") at (").append(x).append(", ").append(y).append(") ")
              .append("{").append(v.getVertexId()).append("};\n");
        }
        sb.append("\n");

        for (int i = 0; i < edges.size(); i++) {
            EdgeDrawn e = edges.get(i);
            String arrow = directed ? "->" : "-";
            String color = (e.getUserStrokeColor() != null) ? ", color=ec" + i : "";
            sb.append("\\draw[").append(arrow).append(color).append("] (")
              .append(e.getSource().getVertexId()).append(") -- (")
              .append(e.getTarget().getVertexId()).append(");\n");
        }
        sb.append("\\end{tikzpicture}\n");
        return sb.toString();
    }
}
