package pl.edu.uj.discretecalculator.io;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.VertexDrawn;
import pl.edu.uj.discretecalculator.view.builder.BuilderContext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

// import grafu z json
public final class GraphImporter {

    private static final Gson GSON = new Gson();

    private GraphImporter() {}

    public static void importFrom(File file, BuilderContext ctx) throws IOException, JsonSyntaxException {
        GraphDocument doc = read(file);
        applyToCanvas(doc, ctx);
    }

    static GraphDocument read(File file) throws IOException, JsonSyntaxException {
        try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            GraphDocument doc = GSON.fromJson(r, GraphDocument.class);
            if (doc == null || doc.graph == null) {
                throw new JsonSyntaxException("Not a valid graph document");
            }
            return doc;
        }
    }

    static void applyToCanvas(GraphDocument doc, BuilderContext ctx) {
        CanvasManager canvas = ctx.canvas();
        canvas.clear();

        Map<String, VertexDrawn> idToVertex = new HashMap<>();
        for(GraphDocument.VertexDto vertex : doc.graph.vertices){
            GraphDocument.Position p = doc.layout.get(vertex.id);
            idToVertex.put(vertex.id, canvas.createVertex(p.x, p.y, vertex.id, ctx.onVertexClick(), ctx.canDrag()));
        }

        for(GraphDocument.EdgeDto edge : doc.graph.edges){
            VertexDrawn s = idToVertex.get(edge.source);
            VertexDrawn t = idToVertex.get(edge.target);
            if(s == null || t == null) continue;
            canvas.createEdge(s,t,ctx.onEdgeClick());
        }
        canvas.renumber();
    }

}
