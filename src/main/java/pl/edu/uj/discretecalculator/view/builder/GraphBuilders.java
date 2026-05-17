package pl.edu.uj.discretecalculator.view.builder;

import javafx.scene.layout.Pane;
import pl.edu.uj.discretecalculator.view.command.AddEdgeCommand;
import pl.edu.uj.discretecalculator.view.command.AddVertexCommand;
import pl.edu.uj.discretecalculator.view.command.Command;
import pl.edu.uj.discretecalculator.view.command.CompoundCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GraphBuilders {
    private static final double MARGIN = 40.0;

    private GraphBuilders() {}

    public static Command cycle(BuilderContext ctx, int n) {
        if (n <= 0) return new CompoundCommand(List.of());

        List<Command> cmds = new ArrayList<>();
        AddVertexCommand[] vCmds = new AddVertexCommand[n];

        Pane pane = ctx.canvas().getGraphPane();
        double cx = pane.getWidth() / 2.0;
        double cy = pane.getHeight() / 2.0;
        double r = Math.min(pane.getWidth(), pane.getHeight()) / 2.0 - MARGIN;

        for (int i = 0; i < n; i++) {
            double theta = 2  * Math.PI * i / n;
            double x = cx + r*Math.cos(theta);
            double y = cy + r*Math.sin(theta);
            vCmds[i] = new AddVertexCommand(ctx.canvas(), x, y, ctx.onVertexClick(), ctx.canDrag());
            cmds.add(vCmds[i]);
        }
        int edgeCount;
        if(n==1) edgeCount = 0;
        else if(n==2) edgeCount = 1;
        else edgeCount = n;

        for (int i = 0; i < edgeCount; i++) {
            cmds.add(lazyEdge(ctx, vCmds[i], vCmds[(i + 1) % n]));
        }

        return new CompoundCommand(cmds);
    }

    public static Command clique(BuilderContext ctx, int n) {
        if (n <= 0) return new CompoundCommand(List.of());

        List<Command> cmds = new ArrayList<>();
        AddVertexCommand[] vCmds = new AddVertexCommand[n];

        Pane pane = ctx.canvas().getGraphPane();
        double cx = pane.getWidth() / 2.0;
        double cy = pane.getHeight() / 2.0;
        double r = Math.min(pane.getWidth(), pane.getHeight()) / 2.0 - MARGIN;

        for (int i = 0; i < n; i++) {
            double theta = 2  * Math.PI * i / n;
            double x = cx + r*Math.cos(theta);
            double y = cy + r*Math.sin(theta);
            vCmds[i] = new AddVertexCommand(ctx.canvas(), x, y, ctx.onVertexClick(), ctx.canDrag());
            cmds.add(vCmds[i]);
        }

        for (int i = 0; i < n; i++) {
            for(int j=0; j<i; j++){
                cmds.add(lazyEdge(ctx, vCmds[i], vCmds[j]));
            }
        }
        return new CompoundCommand(cmds);
    }

    public static Command bipartite(BuilderContext ctx, int n, int m) {
        if (n <= 0 || m <= 0 ) return new CompoundCommand(List.of());

        List<Command> cmds = new ArrayList<>();
        AddVertexCommand[] vCmds = new AddVertexCommand[n+m];

        Pane pane = ctx.canvas().getGraphPane();
        double cx = pane.getWidth() / 2.0;
        double cy = pane.getHeight() / 2.0;
        double r = Math.min(pane.getWidth(), pane.getHeight()) / 2.0 - MARGIN;

        for (int i = 0; i < n; i++) {
            double x = cx - r;
            double y = (n==1 ? cy : cy - r + 2*i*r/(n - 1));
            vCmds[i] = new AddVertexCommand(ctx.canvas(), x, y, ctx.onVertexClick(), ctx.canDrag());
            cmds.add(vCmds[i]);
        }
        for (int i = 0; i < m; i++) {
            double x = cx + r;
            double y = (m==1 ? cy : cy - r + 2*i*r/(m - 1));
            vCmds[n+i] = new AddVertexCommand(ctx.canvas(), x, y, ctx.onVertexClick(), ctx.canDrag());
            cmds.add(vCmds[n+i]);
        }

        for (int i = 0; i < n; i++) {
            for(int j=n; j<n+m; j++){
                cmds.add(lazyEdge(ctx, vCmds[i], vCmds[j]));
            }
        }
        return new CompoundCommand(cmds);
    }

    public static Command randomTree(BuilderContext ctx, int n) {
        if (n <= 0) return new CompoundCommand(List.of());

        Random rng = new Random();
        int[] parent = new int[n];
        int[] depth = new int[n];

        for(int i = 1; i < n; i++){
            parent[i] = rng.nextInt(i);
            depth[i] = depth[parent[i]] + 1;
        }
        int[] perLayer = new int[n];
        int maxDepth = 0;

        for(int i = 0; i < n; i++){
            perLayer[depth[i]]++;
            maxDepth = Math.max(maxDepth, depth[i]);
        }

        Pane pane = ctx.canvas().getGraphPane();
        double usableW = pane.getWidth() - 2 * MARGIN;
        double usableH = pane.getHeight() - 2 * MARGIN;

        AddVertexCommand[] vCmds = new AddVertexCommand[n];
        int[] placedOnLayer = new int[n];
        List<Command> cmds = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            int d = depth[i];
            double y = (maxDepth==0 ? MARGIN/2 :  MARGIN + (double) d / maxDepth * usableH);
            double x = MARGIN + (double) (placedOnLayer[d] + 1) / (perLayer[d] + 1) * usableW;

            vCmds[i] = new AddVertexCommand(ctx.canvas(), x, y, ctx.onVertexClick(), ctx.canDrag());
            cmds.add(vCmds[i]);
            placedOnLayer[d]++;
        }

        for (int i = 1; i < n; i++) {
            cmds.add(lazyEdge(ctx, vCmds[parent[i]], vCmds[i]));
        }
        return new CompoundCommand(cmds);
    }

    private static Command lazyEdge(BuilderContext ctx, AddVertexCommand src, AddVertexCommand tgt) {
        return new Command() {
            private AddEdgeCommand delegate;

            @Override
            public void execute() {
                if (delegate == null) {
                    delegate = new AddEdgeCommand(
                            ctx.canvas(), src.getVertex(), tgt.getVertex(), ctx.onEdgeClick());
                }
                delegate.execute();
            }

            @Override
            public void undo() {
                delegate.undo();
            }
        };
    }
}
