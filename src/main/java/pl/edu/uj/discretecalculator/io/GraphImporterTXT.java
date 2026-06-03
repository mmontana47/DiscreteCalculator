package pl.edu.uj.discretecalculator.io;

import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.VertexDrawn;
import pl.edu.uj.discretecalculator.view.builder.BuilderContext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public class GraphImporterTXT {
    public static void importFromTxt(File file, BuilderContext ctx) throws IOException {
        CanvasManager canvas = ctx.canvas();

        try (java.io.BufferedReader reader = java.nio.file.Files.newBufferedReader(file.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
            String firstLine = reader.readLine();
            if (firstLine == null || firstLine.trim().isEmpty()) {
                throw new IOException("The file is empty");
            }

            String[] header = firstLine.trim().split("\\s+");
            if (header.length < 2) {
                throw new IOException("Invalid input on the first line. Expected: n m [isDirected]");
            }

            int n = Integer.parseInt(header[0]);
            int m = Integer.parseInt(header[1]);

            boolean isDirected = (header.length > 2 && header[2].equals("1"));

            canvas.clear();
            VertexDrawn[] createdVertices = new VertexDrawn[n];

            double paneW = canvas.getGraphPane().getWidth();
            double paneH = canvas.getGraphPane().getHeight();
            if (paneW <= 0) paneW = 800;
            if (paneH <= 0) paneH = 500;
            double centerX = paneW / 2;
            double centerY = paneH / 2;
            double radius = Math.max(80, Math.min(centerX, centerY) - VertexDrawn.circleRadius.get() - 20);

            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);

                createdVertices[i] = canvas.createVertex(x, y, String.valueOf(i), ctx.onVertexClick(), ctx.canDrag());
            }

            for (int i = 0; i < m; i++) {
                String line = reader.readLine();
                if (line == null || line.trim().isEmpty()) continue;

                String[] edgeData = line.trim().split("\\s+");
                int u = Integer.parseInt(edgeData[0]);
                int v = Integer.parseInt(edgeData[1]);

                if (u >= 0 && u < n && v >= 0 && v < n) {
                    canvas.createEdge(u + "-" + v, createdVertices[u], createdVertices[v], ctx.onEdgeClick());
                }
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid input. Natural numbers expected.");
        }
    }
}
