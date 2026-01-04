package isep.ipp.pt.g322.datastructures.graph;

import java.util.*;
import java.util.function.BinaryOperator;

/**
 * Algoritmo de Bellman-Ford para encontrar caminhos mais curtos.
 * Suporta arestas com pesos negativos (custos, bónus, penalizações) e deteta ciclos negativos.
 * 
 * Aplicação: Calcular rotas ferroviárias considerando custos que podem ser negativos
 * (ex: rotas com subsídios/descontos) e identificar configurações inválidas (ciclos negativos).
 */
public class BellmanFord {

    /**
     * Result of Bellman-Ford shortest path computation
     */
    public static class BellmanFordResult<V, E> {
        private final boolean hasNegativeCycle;
        private final List<V> negativeCycle;
        private final Map<V, Double> distances;
        private final Map<V, Edge<V, E>> predecessors;
        private final V source;
        private final String complexityAnalysis;

        public BellmanFordResult(boolean hasNegativeCycle, List<V> negativeCycle,
                                Map<V, Double> distances, Map<V, Edge<V, E>> predecessors,
                                V source, int numVertices, int numEdges) {
            this.hasNegativeCycle = hasNegativeCycle;
            this.negativeCycle = negativeCycle != null ? new ArrayList<>(negativeCycle) : null;
            this.distances = distances != null ? new HashMap<>(distances) : new HashMap<>();
            this.predecessors = predecessors != null ? new HashMap<>(predecessors) : new HashMap<>();
            this.source = source;
            this.complexityAnalysis = String.format(
                "Análise Temporal Bellman-Ford:\n" +
                "- Complexidade Temporal: O(V * E) = O(%d * %d) = O(%d)\n" +
                "  Explicação: O algoritmo relaxa todas as arestas V-1 vezes, onde V é o número de vértices\n" +
                "  e E é o número de arestas. Cada iteração processa todas as E arestas, resultando em\n" +
                "  V * E operações no pior caso.\n" +
                "- Complexidade Espacial: O(V) = O(%d)\n" +
                "  Explicação: Armazena distâncias e predecessores para cada vértice.\n" +
                "- Relaxações: %d iterações × %d arestas = %d operações\n" +
                "- Deteção de ciclo negativo: %d verificações de arestas",
                numVertices, numEdges, numVertices * numEdges,
                numVertices,
                numVertices - 1, numEdges, (numVertices - 1) * numEdges,
                numEdges
            );
        }

        public boolean hasNegativeCycle() {
            return hasNegativeCycle;
        }

        public List<V> getNegativeCycle() {
            return negativeCycle != null ? new ArrayList<>(negativeCycle) : null;
        }

        public Double getDistance(V vertex) {
            return distances.getOrDefault(vertex, Double.POSITIVE_INFINITY);
        }

        public List<V> getPath(V destination) {
            if (!distances.containsKey(destination) || 
                distances.get(destination) == Double.POSITIVE_INFINITY) {
                return null; // No path exists
            }

            LinkedList<V> path = new LinkedList<>();
            V current = destination;
            
            while (current != null) {
                path.addFirst(current);
                Edge<V, E> pred = predecessors.get(current);
                current = (pred != null) ? pred.getVOrig() : null;
                
                // Prevent infinite loops
                if (path.size() > distances.size()) {
                    return null;
                }
            }

            return path;
        }

        public Edge<V, E> getPredecessorEdge(V vertex) {
            return predecessors.get(vertex);
        }

        public V getSource() {
            return source;
        }

        public String getComplexityAnalysis() {
            return complexityAnalysis;
        }

        public Map<V, Double> getAllDistances() {
            return new HashMap<>(distances);
        }
    }

    /**
     * Detailed path information from source to destination
     */
    public static class PathInfo<V, E> {
        private final List<V> path;
        private final List<Edge<V, E>> edges;
        private final List<Double> cumulativeCosts;
        private final double totalCost;

        public PathInfo(List<V> path, List<Edge<V, E>> edges, 
                       List<Double> cumulativeCosts, double totalCost) {
            this.path = path != null ? new ArrayList<>(path) : new ArrayList<>();
            this.edges = edges != null ? new ArrayList<>(edges) : new ArrayList<>();
            this.cumulativeCosts = cumulativeCosts != null ? new ArrayList<>(cumulativeCosts) : new ArrayList<>();
            this.totalCost = totalCost;
        }

        public List<V> getPath() {
            return new ArrayList<>(path);
        }

        public List<Edge<V, E>> getEdges() {
            return new ArrayList<>(edges);
        }

        public List<Double> getCumulativeCosts() {
            return new ArrayList<>(cumulativeCosts);
        }

        public double getTotalCost() {
            return totalCost;
        }

        public boolean hasPath() {
            return path != null && !path.isEmpty();
        }
    }

    /**
     * Computes shortest paths from a source vertex using Bellman-Ford algorithm.
     * Handles negative edge weights and detects negative cycles.
     *
     * @param graph the graph
     * @param source the source vertex
     * @param negate whether to negate edge weights (for longest path conversion)
     * @return BellmanFordResult containing distances, paths, and negative cycle info
     */
    public static <V, E> BellmanFordResult<V, E> shortestPath(
            Graph<V, E> graph, V source, boolean negate) {
        
        if (!graph.validVertex(source)) {
            return new BellmanFordResult<>(false, null, new HashMap<>(), 
                new HashMap<>(), source, 0, 0);
        }

        int numVertices = graph.numVertices();
        int numEdges = graph.numEdges();
        ArrayList<V> vertices = graph.vertices();
        
        // Initialize distances and predecessors
        Map<V, Double> distances = new HashMap<>();
        Map<V, Edge<V, E>> predecessors = new HashMap<>();
        
        for (V vertex : vertices) {
            distances.put(vertex, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);

        // Relax edges |V| - 1 times
        for (int i = 0; i < numVertices - 1; i++) {
            boolean updated = false;
            
            for (V u : vertices) {
                if (distances.get(u) == Double.POSITIVE_INFINITY) {
                    continue;
                }
                
                for (Edge<V, E> edge : graph.outgoingEdges(u)) {
                    V v = edge.getVDest();
                    double weight = getEdgeWeight(edge.getWeight());
                    if (negate) weight = -weight;
                    
                    double newDist = distances.get(u) + weight;
                    if (newDist < distances.get(v)) {
                        distances.put(v, newDist);
                        predecessors.put(v, edge);
                        updated = true;
                    }
                }
            }
            
            // Early termination if no updates
            if (!updated) break;
        }

        // Check for negative cycles
        List<V> negativeCycle = detectNegativeCycle(graph, distances, negate);

        return new BellmanFordResult<>(
            negativeCycle != null,
            negativeCycle,
            distances,
            predecessors,
            source,
            numVertices,
            numEdges
        );
    }

    /**
     * Shorthand for shortestPath without negation
     */
    public static <V, E> BellmanFordResult<V, E> shortestPath(Graph<V, E> graph, V source) {
        return shortestPath(graph, source, false);
    }

    /**
     * Detects negative cycles in the graph after Bellman-Ford relaxation
     */
    private static <V, E> List<V> detectNegativeCycle(
            Graph<V, E> graph, Map<V, Double> distances, boolean negate) {
        
        ArrayList<V> vertices = graph.vertices();
        
        // Try to relax edges one more time
        for (V u : vertices) {
            if (distances.get(u) == Double.POSITIVE_INFINITY) {
                continue;
            }
            
            for (Edge<V, E> edge : graph.outgoingEdges(u)) {
                V v = edge.getVDest();
                double weight = getEdgeWeight(edge.getWeight());
                if (negate) weight = -weight;
                
                if (distances.get(u) + weight < distances.get(v)) {
                    // Found negative cycle, trace it
                    return traceCycle(graph, v, distances, negate);
                }
            }
        }
        
        return null; // No negative cycle
    }

    /**
     * Traces the negative cycle starting from a vertex affected by it
     */
    private static <V, E> List<V> traceCycle(
            Graph<V, E> graph, V start, Map<V, Double> distances, boolean negate) {
        
        ArrayList<V> vertices = graph.vertices();
        
        // Build predecessor map based on shortest path relaxation
        Map<V, V> predecessor = new HashMap<>();
        for (V u : vertices) {
            if (distances.get(u) == Double.POSITIVE_INFINITY) continue;
            
            for (Edge<V, E> edge : graph.outgoingEdges(u)) {
                V v = edge.getVDest();
                double weight = getEdgeWeight(edge.getWeight());
                if (negate) weight = -weight;
                
                // Check if this edge is on the shortest path to v
                if (Math.abs(distances.get(u) + weight - distances.get(v)) < 1e-9) {
                    predecessor.put(v, u);
                }
            }
        }
        
        // Walk back N steps to ensure we're in the cycle
        V current = start;
        for (int i = 0; i < vertices.size(); i++) {
            if (predecessor.containsKey(current)) {
                current = predecessor.get(current);
            }
        }
        
        // Now current is definitely in the cycle
        // Trace the cycle
        Set<V> visited = new HashSet<>();
        List<V> cycle = new ArrayList<>();
        
        while (!visited.contains(current)) {
            visited.add(current);
            cycle.add(current);
            
            if (predecessor.containsKey(current)) {
                current = predecessor.get(current);
            } else {
                // No predecessor, just return what we have
                break;
            }
        }
        
        // Add the cycle-closing vertex
        if (visited.contains(current) && !cycle.isEmpty()) {
            cycle.add(current);
        }
        
        return cycle;
    }

    /**
     * Gets detailed path information from source to destination
     */
    public static <V, E> PathInfo<V, E> getPathInfo(
            BellmanFordResult<V, E> result, V destination) {
        
        List<V> path = result.getPath(destination);
        if (path == null || path.isEmpty()) {
            return new PathInfo<>(null, null, null, Double.POSITIVE_INFINITY);
        }

        List<Edge<V, E>> edges = new ArrayList<>();
        List<Double> cumulativeCosts = new ArrayList<>();
        double cumulative = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            V current = path.get(i);
            V next = path.get(i + 1);
            
            Edge<V, E> edge = result.getPredecessorEdge(next);
            if (edge != null) {
                edges.add(edge);
                cumulative += getEdgeWeight(edge.getWeight());
                cumulativeCosts.add(cumulative);
            }
        }

        return new PathInfo<>(path, edges, cumulativeCosts, result.getDistance(destination));
    }

    /**
     * Helper method to extract numeric weight from generic edge weight
     */
    private static <E> double getEdgeWeight(E weight) {
        if (weight instanceof Number) {
            return ((Number) weight).doubleValue();
        }
        // Try reflection to call getCost() for RailConnection
        try {
            java.lang.reflect.Method method = weight.getClass().getMethod("getCost");
            Object result = method.invoke(weight);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
        } catch (Exception e) {
            // Ignore and fall through
        }
        throw new IllegalArgumentException("Edge weight must be a Number or have a getCost() method");
    }
}
