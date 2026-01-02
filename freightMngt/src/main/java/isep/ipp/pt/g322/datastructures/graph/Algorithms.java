package isep.ipp.pt.g322.datastructures.graph;

import isep.ipp.pt.g322.datastructures.graph.matrix.MatrixGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 *
 * @author DEI-ISEP
 *
 */
public class Algorithms {

    /** Performs breadth-first search of a Graph starting in a vertex
     *
     * @param g Graph instance
     * @param vert vertex that will be the source of the search
     * @return a LinkedList with the vertices of breadth-first search
     */
    public static <V, E> LinkedList<V> BreadthFirstSearch(Graph<V, E> g, V vert) {
        if (!g.validVertex(vert)) {
            return null;
        }

        LinkedList<V> qbfs = new LinkedList<>();
        LinkedList<V> qaux = new LinkedList<>();
        boolean[] visited = new boolean[g.numVertices()];

        qbfs.add(vert);
        qaux.add(vert);
        int vKey = g.key(vert);
        visited[vKey] = true;

        while (!qaux.isEmpty()) {
            V vInf = qaux.remove();
            for (V vAdj : g.adjVertices(vInf)) {
                int vAdjKey = g.key(vAdj);
                if (!visited[vAdjKey]) {
                    qbfs.add(vAdj);
                    qaux.add(vAdj);
                    visited[vAdjKey] = true;
                }
            }
        }
        return qbfs;
    }    

    /** Performs depth-first search starting in a vertex
     *
     * @param g Graph instance
     * @param vOrig vertex of graph g that will be the source of the search
     * @param visited set of previously visited vertices
     * @param qdfs return LinkedList with vertices of depth-first search
     */
    private static <V, E> void DepthFirstSearch(Graph<V, E> g, V vOrig, boolean[] visited, LinkedList<V> qdfs) {
        int vKey = g.key(vOrig);
        qdfs.add(vOrig);
        visited[vKey] = true;

        for (V vAdj : g.adjVertices(vOrig)) {
            int vAdjKey = g.key(vAdj);
            if (!visited[vAdjKey]) {
                DepthFirstSearch(g, vAdj, visited, qdfs);
            }
        }
    }

    /** Performs depth-first search starting in a vertex
     *
     * @param g Graph instance
     * @param vert vertex of graph g that will be the source of the search

     * @return a LinkedList with the vertices of depth-first search
     */
    public static <V, E> LinkedList<V> DepthFirstSearch(Graph<V, E> g, V vert) {
        if (!g.validVertex(vert)) {
            return null;
        }

        LinkedList<V> qdfs = new LinkedList<>();
        boolean[] visited = new boolean[g.numVertices()];
        DepthFirstSearch(g, vert, visited, qdfs);
        return qdfs;
    }

    /** Returns all paths from vOrig to vDest
     *
     * @param g       Graph instance
     * @param vOrig   Vertex that will be the source of the path
     * @param vDest   Vertex that will be the end of the path
     * @param visited set of discovered vertices
     * @param path    stack with vertices of the current path (the path is in reverse order)
     * @param paths   ArrayList with all the paths (in correct order)
     */
    private static <V, E> void allPaths(Graph<V, E> g, V vOrig, V vDest, boolean[] visited,
                                        LinkedList<V> path, ArrayList<LinkedList<V>> paths) {

        int vKey = g.key(vOrig);
        visited[vKey] = true;
        path.push(vOrig);

        for (V vAdj : g.adjVertices(vOrig)) {
            if (vAdj.equals(vDest)) {
                path.push(vDest);
                LinkedList<V> pathCopy = new LinkedList<>(path);
                Collections.reverse(pathCopy);
                paths.add(pathCopy);
                path.pop();
            } else {
                int vAdjKey = g.key(vAdj);
                if (!visited[vAdjKey]) {
                    allPaths(g, vAdj, vDest, visited, path, paths);
                }
            }
        }

        path.pop();
        visited[vKey] = false;
    }

    /** Returns all paths from vOrig to vDest
     *
     * @param g     Graph instance
     * @param vOrig information of the Vertex origin
     * @param vDest information of the Vertex destination
     * @return paths ArrayList with all paths from vOrig to vDest
     */
    public static <V, E> ArrayList<LinkedList<V>> allPaths(Graph<V, E> g, V vOrig, V vDest) {
        if (!g.validVertex(vOrig) || !g.validVertex(vDest)) {
            return null;
        }

        ArrayList<LinkedList<V>> paths = new ArrayList<>();
        boolean[] visited = new boolean[g.numVertices()];
        LinkedList<V> path = new LinkedList<>();

        allPaths(g, vOrig, vDest, visited, path, paths);

        return paths;
    }

    /**
     * Computes shortest-path distance from a source vertex to all reachable
     * vertices of a graph g with non-negative edge weights
     * This implementation uses Dijkstra's algorithm
     *
     * @param g        Graph instance
     * @param vOrig    Vertex that will be the source of the path
     * @param visited  set of previously visited vertices
     * @param pathKeys minimum path vertices keys
     * @param dist     minimum distances
     */
    private static <V, E> void shortestPathDijkstra(Graph<V, E> g, V vOrig,
                                                    Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                                    boolean[] visited, V [] pathKeys, E [] dist) {
        
    int vOrigKey = g.key(vOrig);
        dist[vOrigKey] = zero;
        pathKeys[vOrigKey] = vOrig;

        while (vOrig != null) {
            vOrigKey = g.key(vOrig);
            visited[vOrigKey] = true;

            for (V vAdj : g.adjVertices(vOrig)) {
                int vAdjKey = g.key(vAdj);
                Edge<V, E> edge = g.edge(vOrig, vAdj);
                if (!visited[vAdjKey]) {
                    E s = sum.apply(dist[vOrigKey], edge.getWeight());
                    if (dist[vAdjKey] == null || ce.compare(s, dist[vAdjKey]) < 0) {
                        dist[vAdjKey] = s;
                        pathKeys[vAdjKey] = vOrig;
                    }
                }
            }

            E minDist = null;
            vOrig = null;
            for (V vert : g.vertices()) {
                int vKey = g.key(vert);
                if (!visited[vKey] && dist[vKey] != null) {
                    if (minDist == null || ce.compare(dist[vKey], minDist) < 0) {
                        minDist = dist[vKey];
                        vOrig = vert;
                    }
                }
            }
        }
    }

   
    /** Shortest-path between two vertices
     *
     * @param g graph
     * @param vOrig origin vertex
     * @param vDest destination vertex
     * @param ce comparator between elements of type E
     * @param sum sum two elements of type E
     * @param zero neutral element of the sum in elements of type E
     * @param shortPath returns the vertices which make the shortest path
     * @return if vertices exist in the graph and are connected, true, false otherwise
     */
    public static <V, E> E shortestPath(Graph<V, E> g, V vOrig, V vDest,
                                        Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                        LinkedList<V> shortPath) {

        if (!g.validVertex(vOrig) || !g.validVertex(vDest)) {
            return null;
        }

        shortPath.clear();
        int numVerts = g.numVertices();
        boolean[] visited = new boolean[numVerts];
        @SuppressWarnings("unchecked")
        V[] pathKeys = (V[]) new Object[numVerts];
        @SuppressWarnings("unchecked")
        E[] dist = (E[]) new Object[numVerts];

        shortestPathDijkstra(g, vOrig, ce, sum, zero, visited, pathKeys, dist);

        int vDestKey = g.key(vDest);
        if (dist[vDestKey] == null) {
            return null;
        }

        getPath(g, vOrig, vDest, pathKeys, shortPath);

        return dist[vDestKey];
    }

    /** Shortest-path between a vertex and all other vertices
     *
     * @param g graph
     * @param vOrig start vertex
     * @param ce comparator between elements of type E
     * @param sum sum two elements of type E
     * @param zero neutral element of the sum in elements of type E
     * @param paths returns all the minimum paths
     * @param dists returns the corresponding minimum distances
     * @return if vOrig exists in the graph true, false otherwise
     */
    public static <V, E> boolean shortestPaths(Graph<V, E> g, V vOrig,
                                               Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                               ArrayList<LinkedList<V>> paths, ArrayList<E> dists) {

        if (!g.validVertex(vOrig)) {
            return false;
        }

        paths.clear();
        dists.clear();
        int numVerts = g.numVertices();
        boolean[] visited = new boolean[numVerts];
        @SuppressWarnings("unchecked")
        V[] pathKeys = (V[]) new Object[numVerts];
        @SuppressWarnings("unchecked")
        E[] dist = (E[]) new Object[numVerts];

        shortestPathDijkstra(g, vOrig, ce, sum, zero, visited, pathKeys, dist);

        for (V vert : g.vertices()) {
            int vKey = g.key(vert);
            LinkedList<V> path = new LinkedList<>();
            if (dist[vKey] != null) {
                getPath(g, vOrig, vert, pathKeys, path);
            }
            paths.add(path);
            dists.add(dist[vKey]);
        }

        return true;
    }

    /**
     * Extracts from pathKeys the minimum path between voInf and vdInf
     * The path is constructed from the end to the beginning
     *
     * @param g        Graph instance
     * @param vOrig    information of the Vertex origin
     * @param vDest    information of the Vertex destination
     * @param pathKeys minimum path vertices keys
     * @param path     stack with the minimum path (correct order)
     */
    private static <V, E> void getPath(Graph<V, E> g, V vOrig, V vDest,
                                       V [] pathKeys, LinkedList<V> path) {

        path.clear();
        if (!vOrig.equals(vDest)) {
            path.push(vDest);
            int vDestKey = g.key(vDest);
            V vPred = pathKeys[vDestKey];
            while (vPred != null && !vPred.equals(vOrig)) {
                path.push(vPred);
                int vPredKey = g.key(vPred);
                vPred = pathKeys[vPredKey];
            }
            path.push(vOrig);
        } else {
            path.push(vOrig);
        }
    }

    /** Calculates the minimum distance graph using Floyd-Warshall
     * 
     * @param g initial graph
     * @param ce comparator between elements of type E
     * @param sum sum two elements of type E
     * @return the minimum distance graph
     */
    public static <V,E> MatrixGraph <V,E> minDistGraph(Graph <V,E> g, Comparator<E> ce, BinaryOperator<E> sum) {
        int numVerts = g.numVertices();
        
        // Create a matrix graph with the same properties
        MatrixGraph<V, E> resultGraph = new MatrixGraph<>(g.isDirected(), numVerts);
        
        // Copy all vertices - preserve order
        for (V vert : g.vertices()) {
            resultGraph.addVertex(vert);
        }
        
        // Copy all edges - initial distances
        for (Edge<V, E> edge : g.edges()) {
            resultGraph.addEdge(edge.getVOrig(), edge.getVDest(), edge.getWeight());
        }

        List<V> vertexList = new ArrayList<>();
        for (V v : g.vertices()) {
            vertexList.add(v);
        }
        
        // Floyd-Warshall: For each intermediate vertex k
        for (V k : vertexList) {
            // For each source vertex i
            for (V i : vertexList) {
                // For each destination vertex j  
                for (V j : vertexList) {
                    // Get edges for path i -> k -> j
                    Edge<V, E> edgeIK = resultGraph.edge(i, k);
                    Edge<V, E> edgeKJ = resultGraph.edge(k, j);
                    
                    // If both edges exist, we have a path through k
                    if (edgeIK != null && edgeKJ != null) {
                        // Calculate distance via k
                        E distViaK = sum.apply(edgeIK.getWeight(), edgeKJ.getWeight());
                        
                        // Get current direct edge (may not exist)
                        Edge<V, E> edgeIJ = resultGraph.edge(i, j);
                        
                        // If no edge exists or new path is shorter
                        if (edgeIJ == null) {
                            // Add new edge for this path
                            resultGraph.addEdge(i, j, distViaK);
                        } else if (ce.compare(distViaK, edgeIJ.getWeight()) < 0) {
                            // Update with shorter path
                            resultGraph.removeEdge(i, j);
                            resultGraph.addEdge(i, j, distViaK);
                        }
                    }
                }
            }
        }
        
        return resultGraph;
    }

}