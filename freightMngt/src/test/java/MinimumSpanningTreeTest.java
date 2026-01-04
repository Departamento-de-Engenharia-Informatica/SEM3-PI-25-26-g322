import isep.ipp.pt.g322.datastructures.graph.Edge;
import isep.ipp.pt.g322.datastructures.graph.Graph;
import isep.ipp.pt.g322.datastructures.graph.MinimumSpanningTree;

import isep.ipp.pt.g322.datastructures.graph.map.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MinimumSpanningTreeTest {

    private Comparator<Double> doubleComparator;
    private Comparator<Integer> intComparator;

    @BeforeEach
    void setUp() {
        doubleComparator = Double::compare;
        intComparator = Integer::compare;
    }
    private <V, E> Graph<V, E> createGraph(boolean isDirected) {
        return new MapGraph<>(isDirected);
    }

    @Nested
    @DisplayName("MSTResult Tests")
    class MSTResultTests {

        @Test
        @DisplayName("Should create MSTResult with correct values")
        void testMSTResultCreation() {
            List<Edge<String, Double>> edges = new ArrayList<>();
            double totalWeight = 15.5;
            long computationTime = 1_000_000L;
            int numComponents = 1;

            MinimumSpanningTree.MSTResult<String, Double> result =
                    new MinimumSpanningTree.MSTResult<>(edges, totalWeight, computationTime, numComponents);

            assertEquals(edges, result.getMstEdges());
            assertEquals(totalWeight, result.getTotalWeight());
            assertEquals(1.0, result.getComputationTimeMillis(), 0.001);
            assertEquals(numComponents, result.getNumComponents());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should identify disconnected graph")
        void testDisconnectedGraph() {
            MinimumSpanningTree.MSTResult<String, Double> result =
                    new MinimumSpanningTree.MSTResult<>(new ArrayList<>(), 0.0, 0L, 3);

            assertFalse(result.isConnected());
            assertEquals(3, result.getNumComponents());
        }

        @Test
        @DisplayName("Should convert nanoseconds to milliseconds correctly")
        void testTimeConversion() {
            long nanos = 2_500_000L; // 2.5ms
            MinimumSpanningTree.MSTResult<String, Double> result =
                    new MinimumSpanningTree.MSTResult<>(new ArrayList<>(), 0.0, nanos, 1);

            assertEquals(2.5, result.getComputationTimeMillis(), 0.001);
        }
    }

    @Nested
    @DisplayName("Kruskal Algorithm - Basic Cases")
    class KruskalBasicTests {

        @Test
        @DisplayName("Should throw exception for null graph")
        void testNullGraph() {
            assertThrows(IllegalArgumentException.class, () ->
                    MinimumSpanningTree.kruskal(null, doubleComparator)
            );
        }

        @Test
        @DisplayName("Should throw exception for null comparator")
        void testNullComparator() {
            Graph<String, Double> graph = createGraph(true);
            assertThrows(IllegalArgumentException.class, () ->
                    MinimumSpanningTree.kruskal(graph, null)
            );
        }

        @Test
        @DisplayName("Should handle empty graph")
        void testEmptyGraph() {
            Graph<String, Double> graph = createGraph(false);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertTrue(result.getMstEdges().isEmpty());
            assertEquals(0.0, result.getTotalWeight());
            assertEquals(0, result.getNumComponents());
        }

        @Test
        @DisplayName("Should handle single vertex graph")
        void testSingleVertex() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertTrue(result.getMstEdges().isEmpty());
            assertEquals(0.0, result.getTotalWeight());
            assertEquals(1, result.getNumComponents());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should handle two vertices with one edge")
        void testTwoVerticesOneEdge() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addEdge("A", "B", 5.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(1, result.getMstEdges().size());
            assertEquals(5.0, result.getTotalWeight());
            assertEquals(1, result.getNumComponents());
            assertTrue(result.isConnected());
        }
    }

    @Nested
    @DisplayName("Kruskal Algorithm - Standard MST Cases")
    class KruskalStandardTests {

        @Test
        @DisplayName("Should compute MST for simple triangle graph")
        void testTriangleGraph() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 3.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(2, result.getMstEdges().size());
            assertEquals(3.0, result.getTotalWeight()); // 1.0 + 2.0
            assertEquals(1, result.getNumComponents());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should compute MST for square graph")
        void testSquareGraph() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("C", "D", 3.0);
            graph.addEdge("D", "A", 4.0);
            graph.addEdge("A", "C", 5.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(3, result.getMstEdges().size());
            assertEquals(6.0, result.getTotalWeight()); // 1.0 + 2.0 + 3.0
            assertEquals(1, result.getNumComponents());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should compute MST for classic example from slides")
        void testClassicMSTExample() {
            Graph<String, Integer> graph = createGraph(false);
            String[] vertices = {"A", "B", "C", "D", "E", "F", "G"};
            for (String v : vertices) {
                graph.addVertex(v);
            }

            graph.addEdge("A", "B", 7);
            graph.addEdge("A", "D", 5);
            graph.addEdge("B", "C", 8);
            graph.addEdge("B", "D", 9);
            graph.addEdge("B", "E", 7);
            graph.addEdge("C", "E", 5);
            graph.addEdge("D", "E", 15);
            graph.addEdge("D", "F", 6);
            graph.addEdge("E", "F", 8);
            graph.addEdge("E", "G", 9);
            graph.addEdge("F", "G", 11);

            MinimumSpanningTree.MSTResult<String, Integer> result =
                    MinimumSpanningTree.kruskal(graph, intComparator);

            assertEquals(6, result.getMstEdges().size());
            assertEquals(39.0, result.getTotalWeight());
            assertEquals(1, result.getNumComponents());
            assertTrue(result.isConnected());
        }
    }

    @Nested
    @DisplayName("Kruskal Algorithm - Edge Cases")
    class KruskalEdgeCases {

        @Test
        @DisplayName("Should handle disconnected graph")
        void testDisconnectedGraph() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addEdge("A", "B", 1.0);

            graph.addVertex("C");
            graph.addVertex("D");
            graph.addEdge("C", "D", 2.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(2, result.getMstEdges().size());
            assertEquals(3.0, result.getTotalWeight());
            assertEquals(2, result.getNumComponents());
            assertFalse(result.isConnected());
        }

        @Test
        @DisplayName("Should handle graph with isolated vertices")
        void testIsolatedVertices() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addEdge("A", "B", 1.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(1, result.getMstEdges().size());
            assertEquals(1.0, result.getTotalWeight());
            assertEquals(2, result.getNumComponents());
            assertFalse(result.isConnected());
        }

        @Test
        @DisplayName("Should handle edges with equal weights")
        void testEqualWeights() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("C", "D", 1.0);
            graph.addEdge("A", "D", 1.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(3, result.getMstEdges().size());
            assertEquals(3.0, result.getTotalWeight());
            assertEquals(1, result.getNumComponents());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should handle negative weights")
        void testNegativeWeights() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addEdge("A", "B", -5.0);
            graph.addEdge("B", "C", 3.0);
            graph.addEdge("A", "C", 2.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(2, result.getMstEdges().size());
            assertEquals(-3.0, result.getTotalWeight());
            assertEquals(1, result.getNumComponents());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should handle zero weight edges")
        void testZeroWeights() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addEdge("A", "B", 0.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("A", "C", 2.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(2, result.getMstEdges().size());
            assertEquals(1.0, result.getTotalWeight());
            assertTrue(result.isConnected());
        }
    }

    @Nested
    @DisplayName("Kruskal Algorithm - Different Data Types")
    class KruskalDataTypeTests {

        @Test
        @DisplayName("Should work with Integer weights")
        void testIntegerWeights() {
            Graph<String, Integer> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addEdge("A", "B", 10);
            graph.addEdge("B", "C", 20);
            graph.addEdge("A", "C", 30);

            MinimumSpanningTree.MSTResult<String, Integer> result =
                    MinimumSpanningTree.kruskal(graph, intComparator);

            assertEquals(2, result.getMstEdges().size());
            assertEquals(30.0, result.getTotalWeight());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should work with Integer vertices")
        void testIntegerVertices() {
            Graph<Integer, Double> graph = createGraph(false);
            graph.addVertex(1);
            graph.addVertex(2);
            graph.addVertex(3);
            graph.addEdge(1, 2, 1.5);
            graph.addEdge(2, 3, 2.5);
            graph.addEdge(1, 3, 3.5);

            MinimumSpanningTree.MSTResult<Integer, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(2, result.getMstEdges().size());
            assertEquals(4.0, result.getTotalWeight());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should work with non-Number edge weights using custom comparator")
        void testNonNumberWeights() {
            Graph<String, String> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addEdge("A", "B", "low");
            graph.addEdge("B", "C", "medium");
            graph.addEdge("A", "C", "high");

            Comparator<String> customComparator = (s1, s2) -> {
                Map<String, Integer> priority = Map.of("low", 1, "medium", 2, "high", 3);
                return priority.get(s1).compareTo(priority.get(s2));
            };

            MinimumSpanningTree.MSTResult<String, String> result =
                    MinimumSpanningTree.kruskal(graph, customComparator);

            assertEquals(2, result.getMstEdges().size());
            assertEquals(0.0, result.getTotalWeight());
            assertTrue(result.isConnected());
        }
    }

    @Nested
    @DisplayName("Kruskal Algorithm - Cycle Prevention")
    class KruskalCycleTests {

        @Test
        @DisplayName("Should prevent simple cycles")
        void testSimpleCyclePrevention() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 100.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(2, result.getMstEdges().size());
            assertEquals(3.0, result.getTotalWeight());

            boolean hasExpensiveEdge = result.getMstEdges().stream()
                    .anyMatch(e -> e.getWeight() == 100.0);
            assertFalse(hasExpensiveEdge);
        }

        @Test
        @DisplayName("Should prevent complex cycles")
        void testComplexCyclePrevention() {
            Graph<String, Integer> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");
            graph.addVertex("E");

            graph.addEdge("A", "B", 1);
            graph.addEdge("B", "C", 2);
            graph.addEdge("C", "D", 3);
            graph.addEdge("D", "E", 4);
            graph.addEdge("E", "A", 5);

            graph.addEdge("A", "C", 10);
            graph.addEdge("B", "D", 11);

            MinimumSpanningTree.MSTResult<String, Integer> result =
                    MinimumSpanningTree.kruskal(graph, intComparator);

            assertEquals(4, result.getMstEdges().size()); // n-1 for n=5
            assertEquals(10.0, result.getTotalWeight()); // 1+2+3+4
            assertTrue(result.isConnected());
        }
    }

    @Nested
    @DisplayName("Kruskal Algorithm - Performance and Properties")
    class KruskalPerformanceTests {

        @Test
        @DisplayName("Should record computation time")
        void testComputationTimeRecorded() {
            Graph<String, Double> graph = createGraph(false);
            for (int i = 0; i < 100; i++) {
                graph.addVertex("V" + i);
            }
            for (int i = 0; i < 99; i++) {
                graph.addEdge("V" + i, "V" + (i + 1), (double) i);
            }

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertTrue(result.getComputationTimeMillis() >= 0);
        }

        @Test
        @DisplayName("MST should have exactly n-1 edges for connected graph")
        void testMSTEdgeCount() {
            Graph<String, Double> graph = createGraph(false);
            int n = 10;
            for (int i = 0; i < n; i++) {
                graph.addVertex("V" + i);
            }

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    graph.addEdge("V" + i, "V" + j, Math.random() * 100);
                }
            }

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(n - 1, result.getMstEdges().size());
            assertTrue(result.isConnected());
        }

        @Test
        @DisplayName("Should handle large graph efficiently")
        void testLargeGraph() {
            Graph<Integer, Double> graph = createGraph(false);
            int n = 1000;

            for (int i = 0; i < n; i++) {
                graph.addVertex(i);
            }

            for (int i = 0; i < n - 1; i++) {
                graph.addEdge(i, i + 1, (double) i);
            }

            MinimumSpanningTree.MSTResult<Integer, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(n - 1, result.getMstEdges().size());
            assertTrue(result.isConnected());
            assertTrue(result.getComputationTimeMillis() < 1000);
        }
    }

    @Nested
    @DisplayName("Kruskal Algorithm - Edge Selection Verification")
    class KruskalEdgeSelectionTests {

        @Test
        @DisplayName("Should select minimum weight edges")
        void testMinimumEdgeSelection() {
            Graph<String, Integer> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");

            graph.addEdge("A", "B", 1);
            graph.addEdge("B", "C", 2);
            graph.addEdge("C", "D", 3);
            graph.addEdge("A", "C", 10);
            graph.addEdge("B", "D", 20);
            graph.addEdge("A", "D", 30);

            MinimumSpanningTree.MSTResult<String, Integer> result =
                    MinimumSpanningTree.kruskal(graph, intComparator);

            List<Integer> weights = result.getMstEdges().stream()
                    .map(e -> (Integer) e.getWeight())
                    .sorted()
                    .toList();

            assertEquals(Arrays.asList(1, 2, 3), weights);
            assertEquals(6.0, result.getTotalWeight());
        }

        @Test
        @DisplayName("Should produce valid spanning tree")
        void testValidSpanningTree() {
            Graph<String, Double> graph = createGraph(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("C", "D", 3.0);
            graph.addEdge("D", "A", 4.0);

            MinimumSpanningTree.MSTResult<String, Double> result =
                    MinimumSpanningTree.kruskal(graph, doubleComparator);

            assertEquals(3, result.getMstEdges().size());
            assertTrue(result.isConnected());

            Set<String> coveredVertices = new HashSet<>();
            for (Edge<String, Double> edge : result.getMstEdges()) {
                coveredVertices.add(edge.getVOrig());
                coveredVertices.add(edge.getVDest());
            }
            assertEquals(4, coveredVertices.size());
        }
    }
}