package pl.edu.uj.discretecalculator.controller;

public enum Mode {
    ADD_VERTEX("Add Vertex", "Click to add a vertex"),
    ADD_EDGE("Add Edge", "Click two vertices to add an edge"),
    DELETE("Delete", "Click a vertex or an edge to delete it"),
    MOVE("Move", "Click and drag a vertex to change its position"),
    RUN_BFS("Run BFS", "Click a vertex to start Breadth-First Search"),
    RUN_DFS("Run DFS", "Click a vertex to start Depth-First Search");
    private final String label;
    private final String hint;

    Mode(String label, String hint) {
        this.label = label;
        this.hint = hint;
    }

    public String label() { return label; }
    public String hint() { return hint; }

    public static Mode fromLabel(String label) {
        for (Mode m : values()) {
            if (m.label.equals(label)) return m;
        }
        return null;
    }
}
