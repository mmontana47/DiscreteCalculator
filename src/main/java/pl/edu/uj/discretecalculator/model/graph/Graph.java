package pl.edu.uj.discretecalculator.model.graph;
import java.util.*;
import pl.edu.uj.discretecalculator.exception.*;
public class Graph<V,E> {
    private final Map<Vertex<V>,List<Vertex<V>>> neighborhoodList=new LinkedHashMap<>();
    private final List<Vertex<V>> vertices=new ArrayList<>();
    private final E name;
    private int size=0;
    private boolean isDirected=false;
    private boolean colouredVertices=false;
    private boolean colouredEdges=false;

    public Graph(E name)
    {
        this.name=name;
    }
    public Graph(E name,boolean isDirected,boolean colouredVertices,boolean colouredEdges)
    {
        this.name=name;
        this.isDirected=isDirected;
        this.colouredVertices=colouredVertices;
        this.colouredEdges=colouredEdges;
    }

    public boolean addVertex(Vertex<V> vertex)
    {
        if(vertex==null)
            throw new IllegalArgumentException("Vertex cannot be null");
        if(neighborhoodList.containsKey(vertex))
            throw new VertexAlreadyExistsException("Vertex "+vertex.getId()+" is already in the graph.");
        else
        {
            List<Vertex<V>> neighbors=new ArrayList<>();
            neighborhoodList.put(vertex,neighbors);
            vertices.add(vertex);
            size++;
            return true;
        }
    }

    public void addEdge(Edge<V> edge)
    {
        if(edge==null)
            throw new IllegalArgumentException("Edge cannot be null.");
        if(edge.getSource().getId()==edge.getTarget().getId())
            throw new WrongEdgeTypeException("Edge from vertex to itself is not allowed.");
        if(!neighborhoodList.containsKey(edge.getSource()))
        {
            List<Vertex<V>> list=new ArrayList<>();
            neighborhoodList.put(edge.getSource(),list);
            vertices.add(edge.getSource());
            size++;
        }
        if(!neighborhoodList.containsKey(edge.getTarget()))
        {
            List<Vertex<V>> list=new ArrayList<>();
            neighborhoodList.put(edge.getTarget(),list);
            vertices.add(edge.getTarget());
            size++;
        }
        if(neighborhoodList.get(edge.getSource()).contains(edge.getTarget()))
            throw new EdgeAlreadyExistsException("Edge from "+edge.getSource().getId()+" to "+edge.getTarget()+" already exists.");
        if(!neighborhoodList.get(edge.getSource()).contains(edge.getTarget()))
        {
            neighborhoodList.get(edge.getSource()).add(edge.getTarget());
        }
        if(!isDirected)
        {
            if(!neighborhoodList.get(edge.getTarget()).contains(edge.getSource()))
            {
                neighborhoodList.get(edge.getTarget()).add(edge.getSource());
            }
        }
    }

    public boolean deleteVertex(Vertex<V> vertex)
    {
        if(vertices.contains(vertex))
            vertices.remove(vertex);
        if(neighborhoodList.containsKey(vertex))
        {
            if(isDirected)
            {
                neighborhoodList.remove(vertex);
                for(Vertex<V> v: vertices)
                {
                    neighborhoodList.get(v).remove(vertex);
                }
                size--;
            }
            else
            {
                for(Vertex<V> v: neighborhoodList.get(vertex))
                {
                    neighborhoodList.get(v).remove(vertex);
                }
                neighborhoodList.remove(vertex);
                size--;
            }
            return true;
        }
        else
            return false;
    }

    public boolean deleteEdge(Edge<V> edge)
    {
        if(isDirected)
        {
            return neighborhoodList.get(edge.getSource()).remove(edge.getTarget());
        }
        else {
            neighborhoodList.get(edge.getSource()).remove(edge.getTarget());
            return neighborhoodList.get(edge.getTarget()).remove(edge.getSource());
        }
    }

    public List<Vertex<V>> getVertices() {
        return vertices;
    }

    public E getName(){return name;}

    public List<Vertex<V>> getNeighbors(Vertex<V> vertex) {
        return neighborhoodList.getOrDefault(vertex, new ArrayList<>());
    }
    @Override
    public boolean equals(Object o)
    {
        if(o==this)return true;
        if(o == null || getClass() != o.getClass())return false;
        Graph<?,?> graph=(Graph<?, ?>) o;
        if (size != graph.size ||
                isDirected != graph.isDirected ||
                colouredVertices != graph.colouredVertices ||
                colouredEdges != graph.colouredEdges ||
                !Objects.equals(name, graph.name)) {
            return false;
        }
        if (!neighborhoodList.keySet().equals(graph.neighborhoodList.keySet())) {
            return false;
        }
        for (Map.Entry<Vertex<V>, List<Vertex<V>>> entry : neighborhoodList.entrySet()) {
            Vertex<V> currentVertex = entry.getKey();
            List<Vertex<V>> thisNeighbors = entry.getValue();
            List<?> otherNeighbors = graph.neighborhoodList.get(currentVertex);
            if (otherNeighbors == null || thisNeighbors.size() != otherNeighbors.size()) {
                return false;
            }
            if (!new HashSet<>(thisNeighbors).equals(new HashSet<>(otherNeighbors))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hash(name, size, isDirected, colouredVertices, colouredEdges);
        int edgesHash = 0;
        for (Map.Entry<Vertex<V>, List<Vertex<V>>> entry : neighborhoodList.entrySet()) {
            int keyHash = entry.getKey() != null ? entry.getKey().hashCode() : 0;
            int neighborsHash = 0;
            for (Vertex<V> neighbor : entry.getValue()) {
                neighborsHash += (neighbor != null ? neighbor.hashCode() : 0);
            }
            edgesHash += (keyHash ^ neighborsHash);
        }

        return 31 * result + edgesHash;
    }
}


