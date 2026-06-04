package pl.edu.uj.discretecalculator.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.ArrayList;

//eksport grafu do json
public final class GraphExporterJSON {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private GraphExporterJSON() {}

    public static void export(CanvasManager canvas, File file) throws IOException {
        GraphDocument doc = fromCanvas(canvas);
        write(doc, file);
    }

    static GraphDocument fromCanvas(CanvasManager canvas) {
        GraphDocument doc = new GraphDocument();
        doc.graph = new GraphDocument.GraphPart();
        doc.graph.vertices = new ArrayList<>();
        doc.graph.edges = new ArrayList<>();
        doc.layout = new LinkedHashMap<>();
        doc.graph.isDirected = false;

        for(VertexDrawn vertex : canvas.getVertices()){
            doc.graph.vertices.add(new GraphDocument.VertexDto(vertex.getVertexId()));
            doc.layout.put(vertex.getVertexId(), new GraphDocument.Position(vertex.getLayoutX(), vertex.getLayoutY()));
        }
        for(EdgeDrawn edge : canvas.getEdges()){
            doc.graph.edges.add(new GraphDocument.EdgeDto(edge.getSource().getVertexId(), edge.getTarget().getVertexId()));
        }
        return doc;
    }

    static void write(GraphDocument doc, File file) throws IOException {
        try (Writer w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(doc, w);
        }
    }
}
