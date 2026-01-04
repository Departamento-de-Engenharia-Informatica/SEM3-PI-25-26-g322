package isep.ipp.pt.g322.datastructures.graph;

import java.util.*;

public class CentralityMetrics {
    public static class VertexCentrality<V> {
        private final V vertex;
        private final int degree;
        private final double strength;
        private final double betweenness;
        private final double harmonicCloseness;
        private final double hubScore;

        public VertexCentrality(V vertex, int degree, double strength,
                                double betweenness, double harmonicCloseness, double hubScore) {
            this.vertex = vertex;
            this.degree = degree;
            this.strength = strength;
            this.betweenness = betweenness;
            this.harmonicCloseness = harmonicCloseness;
            this.hubScore = hubScore;
        }

        public V getVertex() { return vertex; }
        public int getDegree() { return degree; }
        public double getStrength() { return strength; }
        public double getBetweenness() { return betweenness; }
        public double getHarmonicCloseness() { return harmonicCloseness; }
        public double getHubScore() { return hubScore; }
    }

    public static class CentralityResult<V> {
        private final List<VertexCentrality<V>> centralities;
        private final long computationTimeNanos;

        public CentralityResult(List<VertexCentrality<V>> centralities, long computationTimeNanos) {
            this.centralities = centralities;
            this.computationTimeNanos = computationTimeNanos;
        }

        public List<VertexCentrality<V>> getCentralities() {
            return centralities;
        }

        public long getComputationTimeNanos() {
            return computationTimeNanos;
        }

        public double getComputationTimeMillis() {
            return computationTimeNanos / 1_000_000.0;
        }
    }

    /**
     * Computes all centrality metrics for the graph.
     * Time: O(V * (V + E)) for betweenness
     */
    public static <V, E> CentralityResult<V> computeCentralities(Graph<V, E> graph) {
        long startTime = System.nanoTime();

        List<V> vertices = new ArrayList<>(graph.vertices());
        int n = vertices.size();

        if (n == 0) {
            return new CentralityResult<>(new ArrayList<>(), System.nanoTime() - startTime);
        }

        Map<V, Integer> degreeMap = new HashMap<>();
        Map<V, Double> strengthMap = new HashMap<>();

        for (V v : vertices) {
            int deg = graph.isDirected() ? graph.outDegree(v) : graph.adjVertices(v).size();
            degreeMap.put(v, deg);

            double str = 0.0;
            for (Edge<V, E> edge : graph.outgoingEdges(v)) {
                if (edge.getWeight() instanceof Number) {
                    str += ((Number) edge.getWeight()).doubleValue();
                }
            }
            strengthMap.put(v, str);
        }

        // betweenness centrality
        Map<V, Double> betweennessMap = computeBetweenness(graph, vertices);

        // harmonic closeness
        Map<V, Double> harmonicClosenessMap = computeHarmonicCloseness(graph, vertices);

        // normalize metrics to [0,1]
        // normalization's goal is to weight in actual importance of each metric and giving them proportionality
        // otherwise betweenness would dominate -> slide 12
        // helps in transport systems, as this one is
        Map<V, Double> normalizedBetweenness = normalize(betweennessMap);
        Map<V, Double> normalizedHarmonic = normalize(harmonicClosenessMap);
        Map<V, Double> normalizedStrength = normalize(strengthMap);

        // to compute hub scores
        List<VertexCentrality<V>> centralities = new ArrayList<>();

        for (V v : vertices) {
            double betw = normalizedBetweenness.getOrDefault(v, 0.0);
            double harm = normalizedHarmonic.getOrDefault(v, 0.0);
            double str = normalizedStrength.getOrDefault(v, 0.0);

            double hubScore = 0.35 * betw + 0.35 * harm + 0.30 * str;

            centralities.add(new VertexCentrality<>(
                    v,
                    degreeMap.get(v),
                    strengthMap.get(v),
                    betweennessMap.get(v),
                    harmonicClosenessMap.get(v),
                    hubScore
            ));
        }

        long endTime = System.nanoTime();
        return new CentralityResult<>(centralities, endTime - startTime);
    }

    /**
     * Computes betweenness centrality using Brandes' algorithm. Alternative would be naive approach
     * References: https://en.wikipedia.org/wiki/Brandes%27_algorithm, https://jgrapht.org/javadoc-1.3.1/org/jgrapht/alg/scoring/BetweennessCentrality.html and https://stackoverflow.com/questions/23312300/betweenness-centrality-brandes-algorithm (especially point 2 of the answer)
     * Time: O(V * (V + E))
     */
    private static <V, E> Map<V, Double> computeBetweenness(Graph<V, E> graph, List<V> vertices) {
        Map<V, Double> betweenness = new HashMap<>();

        for (V v : vertices) {
            betweenness.put(v, 0.0);
        }

        for (V s : vertices) {
            Stack<V> stack = new Stack<>();
            Map<V, List<V>> predecessors = new HashMap<>();
            Map<V, Integer> sigma = new HashMap<>();
            Map<V, Integer> distance = new HashMap<>();

            for (V v : vertices) {
                predecessors.put(v, new ArrayList<>());
                sigma.put(v, 0);
                distance.put(v, -1);
            }

            sigma.put(s, 1);
            distance.put(s, 0);

            Queue<V> queue = new LinkedList<>();
            queue.offer(s);

            while (!queue.isEmpty()) {
                V v = queue.poll();
                stack.push(v);

                for (V w : graph.adjVertices(v)) {
                    if (distance.get(w) < 0) {
                        queue.offer(w);
                        distance.put(w, distance.get(v) + 1);
                    }

                    if (distance.get(w) == distance.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        predecessors.get(w).add(v);
                    }
                }
            }

            Map<V, Double> delta = new HashMap<>();
            for (V v : vertices) {
                delta.put(v, 0.0);
            }

            while (!stack.isEmpty()) {
                V w = stack.pop();
                for (V v : predecessors.get(w)) {
                    double c = (sigma.get(v) / (double) sigma.get(w)) * (1.0 + delta.get(w));
                    delta.put(v, delta.get(v) + c);
                }
                if (!w.equals(s)) {
                    betweenness.put(w, betweenness.get(w) + delta.get(w));
                }
            }
        }

        // normalize for undirected graphs
        if (!graph.isDirected()) {
            for (V v : vertices) {
                betweenness.put(v, betweenness.get(v) / 2.0);
            }
        }

        return betweenness;
    }

    /**
     * Computes harmonic closeness centrality.
     * Harmonic closeness = sum(1/distance) for all reachable vertices.
     */
    private static <V, E> Map<V, Double> computeHarmonicCloseness(Graph<V, E> graph, List<V> vertices) {
        Map<V, Double> harmonicCloseness = new HashMap<>();

        for (V source : vertices) {
            double sum = 0.0;

            Map<V, Integer> distances = bfsDistances(graph, source);

            for (V target : vertices) {
                if (!source.equals(target) && distances.containsKey(target)) {
                    int dist = distances.get(target);
                    if (dist > 0) {
                        sum += 1.0 / dist;
                    }
                }
            }

            harmonicCloseness.put(source, sum);
        }

        return harmonicCloseness;
    }

    /**
     * BFS to compute distances from source to all reachable vertices.
     */
    private static <V, E> Map<V, Integer> bfsDistances(Graph<V, E> graph, V source) {
        Map<V, Integer> distances = new HashMap<>();
        Queue<V> queue = new LinkedList<>();

        distances.put(source, 0);
        queue.offer(source);

        while (!queue.isEmpty()) {
            V current = queue.poll();
            int currentDist = distances.get(current);

            for (V neighbor : graph.adjVertices(current)) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    queue.offer(neighbor);
                }
            }
        }

        return distances;
    }

    /**
     * Normalizes values to [0, 1] range.
     */
    private static <V> Map<V, Double> normalize(Map<V, Double> values) {
        double max = values.values().stream().max(Double::compare).orElse(1.0);
        double min = values.values().stream().min(Double::compare).orElse(0.0);

        Map<V, Double> normalized = new HashMap<>();

        if (max == min) {
            for (V v : values.keySet()) {
                normalized.put(v, 0.0);
            }
        } else {
            for (Map.Entry<V, Double> entry : values.entrySet()) {
                double norm = (entry.getValue() - min) / (max - min);
                normalized.put(entry.getKey(), norm);
            }
        }

        return normalized;
    }
}