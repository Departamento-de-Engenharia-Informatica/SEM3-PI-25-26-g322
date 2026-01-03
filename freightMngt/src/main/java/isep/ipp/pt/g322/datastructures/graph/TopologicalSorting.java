package isep.ipp.pt.g322.datastructures.graph;

import java.util.*;

public class TopologicalSorting {

    /**
     * Result/representation of topological sorting operation.
     */
    public static class TopologicalResult<V> {
        private final List<V> orderedVertices;
        private final List<V> cycleVertices;
        private final List<Edge<V, ?>> cycleEdges;
        private final long computationTimeNanos;

        public TopologicalResult(List<V> orderedVertices, List<V> cycleVertices,
                                 List<Edge<V, ?>> cycleEdges, long computationTimeNanos) {
            this.orderedVertices = orderedVertices;
            this.cycleVertices = cycleVertices;
            this.cycleEdges = cycleEdges;
            this.computationTimeNanos = computationTimeNanos;
        }

        /**
         * Returns true if the graph has no cycles.
         */
        public boolean hasCycles() {
            return cycleVertices != null && !cycleVertices.isEmpty();
        }

        public List<V> getOrderedVertices() {
            return orderedVertices;
        }

        /**
         * Returns vertices involved in cycles.
         * Returns empty list if no cycles.
         */
        public List<V> getCycleVertices() {
            return cycleVertices != null ? cycleVertices : Collections.emptyList();
        }

        /**
         * Returns edges involved in cycles.
         * Returns empty list if no cycles.
         */
        public List<Edge<V, ?>> getCycleEdges() {
            return cycleEdges != null ? cycleEdges : Collections.emptyList();
        }

        /**
         * Returns computation time in milliseconds.
         */
        public double getComputationTimeMillis() {
            return computationTimeNanos / 1_000_000.0;
        }
    }

    /**
     * Performs topological sorting using Kahn's algorithm (BFS-based).
     * Detects cycles if they exist.
     * Time Complexity: O(V + E) where V is vertices and E is edges
     * Space Complexity: O(V)
     *
     * @param graph the directed graph to sort
     * @param <V> vertex type
     * @param <E> edge type
     * @return TopologicalResult containing ordering or cycle information
     */
    public static <V, E> TopologicalResult<V> topologicalSort(Graph<V, E> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }

        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires a directed graph");
        }

        long startTime = System.nanoTime();

        int numVertices = graph.numVertices();
        if (numVertices == 0) {
            return new TopologicalResult<>(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    System.nanoTime() - startTime
            );
        }

        // calculate in degrees for all vertices
        Map<V, Integer> inDegree = new HashMap<>();
        for (V vertex : graph.vertices()) {
            inDegree.put(vertex, graph.inDegree(vertex));
        }

        // queue for vertices with in degree 0
        Queue<V> queue = new LinkedList<>();
        for (V vertex : graph.vertices()) {
            if (inDegree.get(vertex) == 0) {
                queue.offer(vertex);
            }
        }

        List<V> topologicalOrder = new ArrayList<>();

        while (!queue.isEmpty()) {
            V current = queue.poll();
            topologicalOrder.add(current);

            // reduce in degree for all adjacent vertices
            for (V adjacent : graph.adjVertices(current)) {
                int newInDegree = inDegree.get(adjacent) - 1;
                inDegree.put(adjacent, newInDegree);

                if (newInDegree == 0) {
                    queue.offer(adjacent);
                }
            }
        }

        long endTime = System.nanoTime();

        // check if all vertices were processed
        if (topologicalOrder.size() == numVertices) {
            // if no cycles - successful topological sort
            return new TopologicalResult<>(
                    topologicalOrder,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    endTime - startTime
            );
        } else {
            // if cycles detected - find vertices and edges in cycles
            Set<V> processedVertices = new HashSet<>(topologicalOrder);
            List<V> cycleVertices = new ArrayList<>();
            List<Edge<V, ?>> cycleEdges = new ArrayList<>();

            for (V vertex : graph.vertices()) {
                if (!processedVertices.contains(vertex)) {
                    cycleVertices.add(vertex);
                }
            }

            // find edges that are part of cycles
            for (V vertex : cycleVertices) {
                for (Edge<V, E> edge : graph.outgoingEdges(vertex)) {
                    if (cycleVertices.contains(edge.getVDest())) {
                        cycleEdges.add(edge);
                    }
                }
            }

            return new TopologicalResult<>(
                    null,  // if no valid ordering
                    cycleVertices,
                    cycleEdges,
                    endTime - startTime
            );
        }
    }
}