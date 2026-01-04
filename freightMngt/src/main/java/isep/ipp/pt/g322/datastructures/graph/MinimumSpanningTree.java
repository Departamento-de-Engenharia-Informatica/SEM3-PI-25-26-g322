package isep.ipp.pt.g322.datastructures.graph;

import java.util.*;

/**
 * using Kruskal's algorithm as per slide 99 and next ones
 */
public class MinimumSpanningTree {
    public static class MSTResult<V, E> {
        private final List<Edge<V, E>> mstEdges;
        private final double totalWeight;
        private final long computationTimeNanos;
        private final int numComponents;

        public MSTResult(List<Edge<V, E>> mstEdges, double totalWeight,
                         long computationTimeNanos, int numComponents) {
            this.mstEdges = mstEdges;
            this.totalWeight = totalWeight;
            this.computationTimeNanos = computationTimeNanos;
            this.numComponents = numComponents;
        }

        public List<Edge<V, E>> getMstEdges() {
            return mstEdges;
        }

        public double getTotalWeight() {
            return totalWeight;
        }

        public double getComputationTimeMillis() { // to compare across several runs of the program with other files and identify bottlenecks if any. remove later
            return computationTimeNanos / 1_000_000.0;
        }

        public int getNumComponents() {
            return numComponents;
        }

        public boolean isConnected() {
            return numComponents == 1;
        }
    }

    /**
     * Computes MST using Kruskal's algorithm.
     * Time: O(E log E), Space: O(V)
     */
    public static <V, E> MSTResult<V, E> kruskal(Graph<V, E> graph,
                                                 Comparator<E> weightComparator) {
        if (graph == null || weightComparator == null) {
            throw new IllegalArgumentException("Graph and comparator cannot be null");
        }

        long startTime = System.nanoTime();

        List<Edge<V, E>> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;

        if (graph.numVertices() == 0) {
            return new MSTResult<>(mstEdges, totalWeight,
                    System.nanoTime() - startTime, 0);
        }

        // sorting all edges by weight
        List<Edge<V, E>> allEdges = new ArrayList<>(graph.edges());
        allEdges.sort((e1, e2) -> weightComparator.compare(e1.getWeight(), e2.getWeight()));

        // disjoint set to prevent cycles
        Map<V, V> parent = new HashMap<>();
        Map<V, Integer> rank = new HashMap<>();

        for (V vertex : graph.vertices()) {
            parent.put(vertex, vertex);
            rank.put(vertex, 0);
        }

        // kruskal's algorithm
        for (Edge<V, E> edge : allEdges) {
            V root1 = find(parent, edge.getVOrig());
            V root2 = find(parent, edge.getVDest());

            if (!root1.equals(root2)) {
                mstEdges.add(edge);
                union(parent, rank, root1, root2);

                if (edge.getWeight() instanceof Number) {
                    totalWeight += ((Number) edge.getWeight()).doubleValue();
                }
            }
        }

        Set<V> roots = new HashSet<>();
        for (V vertex : graph.vertices()) {
            roots.add(find(parent, vertex));
        }
        int numComponents = roots.size();

        long endTime = System.nanoTime();
        return new MSTResult<>(mstEdges, totalWeight, endTime - startTime, numComponents);
    }

    private static <V> V find(Map<V, V> parent, V vertex) {
        if (!parent.get(vertex).equals(vertex)) {
            parent.put(vertex, find(parent, parent.get(vertex)));
        }
        return parent.get(vertex);
    }

    private static <V> void union(Map<V, V> parent, Map<V, Integer> rank, V root1, V root2) {
        int rank1 = rank.get(root1);
        int rank2 = rank.get(root2);

        if (rank1 < rank2) {
            parent.put(root1, root2);
        } else if (rank1 > rank2) {
            parent.put(root2, root1);
        } else {
            parent.put(root2, root1);
            rank.put(root1, rank1 + 1);
        }
    }
}