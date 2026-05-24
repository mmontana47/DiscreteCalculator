package pl.edu.uj.discretecalculator.io;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphExporterTXT {
    public static void exportToTxt(CanvasManager canvas, File file) throws IOException {
        List<VertexDrawn> vertices = canvas.getVertices();
        List<EdgeDrawn> edges = canvas.getEdges();

        Map<VertexDrawn, Integer> vertexToIndex = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++) {
            vertexToIndex.put(vertices.get(i), i);
        }

        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(file.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
            int n = vertices.size();
            int m = edges.size();
            int isDirected = 0;

            // Pierwsza linijka: n m [flaga]
            writer.write(n + " " + m + " " + isDirected);
            writer.newLine();

            for (EdgeDrawn edge : edges) {
                int u = vertexToIndex.get(edge.getSource());
                int v = vertexToIndex.get(edge.getTarget());
                writer.write(u + " " + v);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}