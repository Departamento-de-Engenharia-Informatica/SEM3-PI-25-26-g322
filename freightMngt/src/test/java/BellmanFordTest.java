import isep.ipp.pt.g322.datastructures.graph.*;
import isep.ipp.pt.g322.datastructures.graph.map.MapGraph;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BellmanFordTest {

    @Nested
    @DisplayName("Basic Shortest Path Tests")
    class BasicShortestPathTests {

        @Test
        @DisplayName("Should find shortest path in simple graph")
        void testSimpleShortestPath() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");

            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "D", 1.0);
            graph.addEdge("C", "D", 3.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertFalse(result.hasNegativeCycle());
            assertEquals(0.0, result.getDistance("A"));
            assertEquals(4.0, result.getDistance("B"));
            assertEquals(2.0, result.getDistance("C"));
            assertEquals(5.0, result.getDistance("D")); // A->C->D = 2+3
        }

        @Test
        @DisplayName("Should handle single vertex graph")
        void testSingleVertex() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertFalse(result.hasNegativeCycle());
            assertEquals(0.0, result.getDistance("A"));
        }

        @Test
        @DisplayName("Should handle disconnected vertices")
        void testDisconnectedVertices() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            
            graph.addEdge("A", "B", 1.0);
            // C is disconnected

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertEquals(0.0, result.getDistance("A"));
            assertEquals(1.0, result.getDistance("B"));
            assertEquals(Double.POSITIVE_INFINITY, result.getDistance("C"));
        }
    }

    @Nested
    @DisplayName("Negative Weight Tests")
    class NegativeWeightTests {

        @Test
        @DisplayName("Should handle negative weights correctly")
        void testNegativeWeights() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");

            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "D", 1.0);
            graph.addEdge("C", "B", -3.0); // Negative edge
            graph.addEdge("C", "D", 5.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertFalse(result.hasNegativeCycle());
            assertEquals(0.0, result.getDistance("A"));
            assertEquals(2.0, result.getDistance("C"));
            assertEquals(-1.0, result.getDistance("B")); // A->C->B = 2+(-3)
            assertEquals(0.0, result.getDistance("D")); // A->C->B->D = 2+(-3)+1
        }

        @Test
        @DisplayName("Should handle mixed positive and negative weights")
        void testMixedWeights() {
            Graph<Integer, Double> graph = new MapGraph<>(true);
            for (int i = 1; i <= 4; i++) {
                graph.addVertex(i);
            }

            graph.addEdge(1, 2, 5.0);
            graph.addEdge(2, 3, -2.0);
            graph.addEdge(1, 3, 4.0);
            graph.addEdge(3, 4, 1.0);

            BellmanFord.BellmanFordResult<Integer, Double> result = 
                BellmanFord.shortestPath(graph, 1);

            assertFalse(result.hasNegativeCycle());
            assertEquals(3.0, result.getDistance(3)); // 1->2->3 = 5+(-2)
            assertEquals(4.0, result.getDistance(4)); // 1->2->3->4
        }
    }

    @Nested
    @DisplayName("Negative Cycle Detection Tests")
    class NegativeCycleTests {

        @Test
        @DisplayName("Should detect simple negative cycle")
        void testSimpleNegativeCycle() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");

            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", -2.0);
            graph.addEdge("C", "A", -1.0); // Cycle: A->B->C->A = 1+(-2)+(-1) = -2

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertTrue(result.hasNegativeCycle());
            assertNotNull(result.getNegativeCycle());
            assertTrue(result.getNegativeCycle().size() >= 2);
        }

        @Test
        @DisplayName("Should detect negative self-loop")
        void testNegativeSelfLoop() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");

            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "B", -1.0); // Negative self-loop

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertTrue(result.hasNegativeCycle());
        }

        @Test
        @DisplayName("Should detect negative cycle in larger graph")
        void testLargerNegativeCycle() {
            Graph<Integer, Double> graph = new MapGraph<>(true);
            for (int i = 1; i <= 6; i++) {
                graph.addVertex(i);
            }

            graph.addEdge(1, 2, 3.0);
            graph.addEdge(2, 3, 2.0);
            graph.addEdge(3, 4, -5.0);
            graph.addEdge(4, 5, 1.0);
            graph.addEdge(5, 2, -2.0); // Creates negative cycle: 2->3->4->5->2

            BellmanFord.BellmanFordResult<Integer, Double> result = 
                BellmanFord.shortestPath(graph, 1);

            assertTrue(result.hasNegativeCycle());
        }

        @Test
        @DisplayName("Should not detect positive cycle as negative")
        void testPositiveCycle() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");

            graph.addEdge("A", "B", 2.0);
            graph.addEdge("B", "C", 3.0);
            graph.addEdge("C", "A", 1.0); // Positive cycle: total = 6.0

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertFalse(result.hasNegativeCycle());
        }
    }

    @Nested
    @DisplayName("Path Reconstruction Tests")
    class PathReconstructionTests {

        @Test
        @DisplayName("Should reconstruct simple path correctly")
        void testSimplePathReconstruction() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");

            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            List<String> path = result.getPath("C");
            assertNotNull(path);
            assertEquals(Arrays.asList("A", "B", "C"), path);
        }

        @Test
        @DisplayName("Should return null for unreachable destination")
        void testUnreachablePath() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            
            graph.addEdge("A", "B", 1.0);
            // C is unreachable from A

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            List<String> path = result.getPath("C");
            assertNull(path);
        }

        @Test
        @DisplayName("Should handle path to source itself")
        void testPathToSource() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addEdge("A", "B", 1.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            List<String> path = result.getPath("A");
            assertNotNull(path);
            assertEquals(Collections.singletonList("A"), path);
        }
    }

    @Nested
    @DisplayName("PathInfo Tests")
    class PathInfoTests {

        @Test
        @DisplayName("Should provide detailed path information")
        void testPathInfo() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");

            graph.addEdge("A", "B", 2.0);
            graph.addEdge("B", "C", 3.0);
            graph.addEdge("C", "D", 1.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");
            BellmanFord.PathInfo<String, Double> pathInfo = 
                BellmanFord.getPathInfo(result, "D");

            assertTrue(pathInfo.hasPath());
            assertEquals(6.0, pathInfo.getTotalCost());
            assertEquals(Arrays.asList("A", "B", "C", "D"), pathInfo.getPath());
            assertEquals(3, pathInfo.getEdges().size());
            assertEquals(3, pathInfo.getCumulativeCosts().size());
            assertEquals(2.0, pathInfo.getCumulativeCosts().get(0));
            assertEquals(5.0, pathInfo.getCumulativeCosts().get(1));
            assertEquals(6.0, pathInfo.getCumulativeCosts().get(2));
        }

        @Test
        @DisplayName("Should handle path info for unreachable vertex")
        void testPathInfoUnreachable() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");
            BellmanFord.PathInfo<String, Double> pathInfo = 
                BellmanFord.getPathInfo(result, "B");

            assertFalse(pathInfo.hasPath());
            assertEquals(Double.POSITIVE_INFINITY, pathInfo.getTotalCost());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty graph")
        void testEmptyGraph() {
            Graph<String, Double> graph = new MapGraph<>(true);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertFalse(result.hasNegativeCycle());
            assertEquals(0, result.getAllDistances().size());
        }

        @Test
        @DisplayName("Should handle invalid source vertex")
        void testInvalidSource() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "C");

            assertNotNull(result);
            assertEquals(0, result.getAllDistances().size());
        }

        @Test
        @DisplayName("Should handle zero-weight edges")
        void testZeroWeightEdges() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");

            graph.addEdge("A", "B", 0.0);
            graph.addEdge("B", "C", 0.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertFalse(result.hasNegativeCycle());
            assertEquals(0.0, result.getDistance("A"));
            assertEquals(0.0, result.getDistance("B"));
            assertEquals(0.0, result.getDistance("C"));
        }

        @Test
        @DisplayName("Should handle multiple paths with same cost")
        void testMultipleEqualPaths() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");

            graph.addEdge("A", "B", 2.0);
            graph.addEdge("A", "C", 1.0);
            graph.addEdge("B", "D", 1.0);
            graph.addEdge("C", "D", 2.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertEquals(3.0, result.getDistance("D"));
            assertNotNull(result.getPath("D"));
        }
    }

    @Nested
    @DisplayName("Complexity Analysis Tests")
    class ComplexityAnalysisTests {

        @Test
        @DisplayName("Should provide complexity analysis")
        void testComplexityAnalysis() {
            Graph<String, Double> graph = new MapGraph<>(true);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            String analysis = result.getComplexityAnalysis();
            assertNotNull(analysis);
            assertTrue(analysis.contains("O(V * E)"));
            assertTrue(analysis.contains("Complexidade Temporal"));
            assertTrue(analysis.contains("Complexidade Espacial"));
        }
    }

    @Nested
    @DisplayName("Undirected Graph Tests")
    class UndirectedGraphTests {

        @Test
        @DisplayName("Should work with undirected graphs")
        void testUndirectedGraph() {
            Graph<String, Double> graph = new MapGraph<>(false);
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");

            graph.addEdge("A", "B", 2.0);
            graph.addEdge("B", "C", 3.0);

            BellmanFord.BellmanFordResult<String, Double> result = 
                BellmanFord.shortestPath(graph, "A");

            assertEquals(0.0, result.getDistance("A"));
            assertEquals(2.0, result.getDistance("B"));
            assertEquals(5.0, result.getDistance("C"));
        }
    }
}
