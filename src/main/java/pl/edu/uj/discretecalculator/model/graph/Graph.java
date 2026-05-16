import java.util.*;
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
        if(neighborhoodList.containsKey(vertex))
            return false;
        else
        {
            List<Vertex<V>> neighbors=new ArrayList<>();
            neighborhoodList.put(vertex,neighbors);
            size++;
            return true;
        }
    }

    public void addEdge(Edge<V> edge)
    {
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

    public List<Vertex<V>> getNeighbors(Vertex<V> vertex) {
        return neighborhoodList.getOrDefault(vertex, new ArrayList<>());
    }
}


