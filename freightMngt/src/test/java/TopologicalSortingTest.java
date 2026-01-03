import isep.ipp.pt.g322.datastructures.graph.Graph;
import isep.ipp.pt.g322.datastructures.graph.TopologicalSorting;


import isep.ipp.pt.g322.datastructures.graph.map.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortingTest {

    private Graph<String, Integer> directedGraph;
    private Graph<String, Integer> undirectedGraph;

    @BeforeEach
    void setUp() {
        directedGraph = new MapGraph<>(true);
        undirectedGraph = new MapGraph<>(false);
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when graph is null")
        void testNullGraph() {
            assertThrows(IllegalArgumentException.class, () -> {
                TopologicalSorting.topologicalSort(null);
            });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for undirected graph")
        void testUndirectedGraph() {
            undirectedGraph.addVertex("A");
            undirectedGraph.addVertex("B");
            undirectedGraph.addEdge("A", "B", 1);

            assertThrows(IllegalArgumentException.class, () -> {
                TopologicalSorting.topologicalSort(undirectedGraph);
            });
        }
    }

    @Nested
    @DisplayName("Empty Graph Tests")
    class EmptyGraphTests {

        @Test
        @DisplayName("Should handle empty graph")
        void testEmptyGraph() {
            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertNotNull(result.getOrderedVertices());
            assertTrue(result.getOrderedVertices().isEmpty());
            assertTrue(result.getCycleVertices().isEmpty());
            assertTrue(result.getCycleEdges().isEmpty());
            assertTrue(result.getComputationTimeMillis() >= 0);
        }
    }

    @Nested
    @DisplayName("Single Vertex Tests")
    class SingleVertexTests {

        @Test
        @DisplayName("Should handle single vertex with no edges")
        void testSingleVertex() {
            directedGraph.addVertex("A");

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(1, result.getOrderedVertices().size());
            assertEquals("A", result.getOrderedVertices().get(0));
            assertTrue(result.getCycleVertices().isEmpty());
        }

        @Test
        @DisplayName("Should detect self-loop as cycle")
        void testSelfLoop() {
            directedGraph.addVertex("A");
            directedGraph.addEdge("A", "A", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());
            assertTrue(result.getCycleVertices().contains("A"));
            assertEquals(1, result.getCycleEdges().size());
        }
    }

    @Nested
    @DisplayName("Acyclic Graph Tests")
    class AcyclicGraphTests {

        @Test
        @DisplayName("Should sort simple linear graph: A -> B -> C")
        void testSimpleLinearGraph() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("B", "C", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(3, result.getOrderedVertices().size());

            List<String> order = result.getOrderedVertices();
            int indexA = order.indexOf("A");
            int indexB = order.indexOf("B");
            int indexC = order.indexOf("C");

            assertTrue(indexA < indexB, "A should come before B");
            assertTrue(indexB < indexC, "B should come before C");
        }

        @Test
        @DisplayName("Should sort diamond-shaped DAG")
        void testDiamondGraph() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addVertex("D");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("A", "C", 1);
            directedGraph.addEdge("B", "D", 1);
            directedGraph.addEdge("C", "D", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(4, result.getOrderedVertices().size());

            List<String> order = result.getOrderedVertices();
            int indexA = order.indexOf("A");
            int indexB = order.indexOf("B");
            int indexC = order.indexOf("C");
            int indexD = order.indexOf("D");

            assertTrue(indexA < indexB, "A should come before B");
            assertTrue(indexA < indexC, "A should come before C");
            assertTrue(indexB < indexD, "B should come before D");
            assertTrue(indexC < indexD, "C should come before D");
        }

        @Test
        @DisplayName("Should sort graph with multiple source nodes")
        void testMultipleSources() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addEdge("A", "C", 1);
            directedGraph.addEdge("B", "C", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(3, result.getOrderedVertices().size());

            List<String> order = result.getOrderedVertices();
            int indexC = order.indexOf("C");

            assertTrue(indexC == 2, "C should be last (after both A and B)");
        }

        @Test
        @DisplayName("Should handle disconnected DAG components")
        void testDisconnectedComponents() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addVertex("D");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("C", "D", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(4, result.getOrderedVertices().size());

            List<String> order = result.getOrderedVertices();
            int indexA = order.indexOf("A");
            int indexB = order.indexOf("B");
            int indexC = order.indexOf("C");
            int indexD = order.indexOf("D");

            assertTrue(indexA < indexB, "A should come before B");
            assertTrue(indexC < indexD, "C should come before D");
        }

        @Test
        @DisplayName("Should sort complex DAG with multiple paths")
        void testComplexDAG() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addVertex("D");
            directedGraph.addVertex("E");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("B", "C", 1);
            directedGraph.addEdge("C", "E", 1);
            directedGraph.addEdge("A", "D", 1);
            directedGraph.addEdge("D", "E", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(5, result.getOrderedVertices().size());

            List<String> order = result.getOrderedVertices();
            assertEquals("A", order.get(0), "A should be first");
            assertEquals("E", order.get(4), "E should be last");
        }
    }

    @Nested
    @DisplayName("Cyclic Graph Tests")
    class CyclicGraphTests {

        @Test
        @DisplayName("Should detect simple two-vertex cycle: A -> B -> A")
        void testSimpleCycle() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("B", "A", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());
            assertEquals(2, result.getCycleVertices().size());
            assertTrue(result.getCycleVertices().contains("A"));
            assertTrue(result.getCycleVertices().contains("B"));
            assertFalse(result.getCycleEdges().isEmpty());
        }

        @Test
        @DisplayName("Should detect three-vertex cycle: A -> B -> C -> A")
        void testThreeVertexCycle() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("B", "C", 1);
            directedGraph.addEdge("C", "A", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());
            assertEquals(3, result.getCycleVertices().size());
            assertTrue(result.getCycleVertices().contains("A"));
            assertTrue(result.getCycleVertices().contains("B"));
            assertTrue(result.getCycleVertices().contains("C"));
        }

        @Test
        @DisplayName("Should detect cycle in mixed graph with acyclic and cyclic parts")
        void testMixedGraphWithCycle() {
            // D -> A -> B -> C -> A (cycle)
            directedGraph.addVertex("D");
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addEdge("D", "A", 1);
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("B", "C", 1);
            directedGraph.addEdge("C", "A", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());

            assertFalse(result.getCycleVertices().contains("D"));

            assertTrue(result.getCycleVertices().contains("A"));
            assertTrue(result.getCycleVertices().contains("B"));
            assertTrue(result.getCycleVertices().contains("C"));
        }

        @Test
        @DisplayName("Should detect multiple separate cycles")
        void testMultipleCycles() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addVertex("D");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("B", "A", 1);
            directedGraph.addEdge("C", "D", 1);
            directedGraph.addEdge("D", "C", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());
            assertEquals(4, result.getCycleVertices().size());
        }
    }

    @Nested
    @DisplayName("TopologicalResult Tests")
    class TopologicalResultTests {

        @Test
        @DisplayName("Should correctly identify presence of cycles")
        void testHasCycles() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addEdge("A", "B", 1);

            TopologicalSorting.TopologicalResult<String> result1 =
                    TopologicalSorting.topologicalSort(directedGraph);
            assertFalse(result1.hasCycles());

            Graph<String, Integer> cyclicGraph = new MapGraph<>(true);
            cyclicGraph.addVertex("A");
            cyclicGraph.addVertex("B");
            cyclicGraph.addEdge("A", "B", 1);
            cyclicGraph.addEdge("B", "A", 1);

            TopologicalSorting.TopologicalResult<String> result2 =
                    TopologicalSorting.topologicalSort(cyclicGraph);
            assertTrue(result2.hasCycles());
        }

        @Test
        @DisplayName("Should return empty lists for acyclic graph cycle information")
        void testAcyclicGraphCycleInfo() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addEdge("A", "B", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result.getCycleVertices());
            assertTrue(result.getCycleVertices().isEmpty());
            assertNotNull(result.getCycleEdges());
            assertTrue(result.getCycleEdges().isEmpty());
        }

        @Test
        @DisplayName("Should return null ordered vertices for cyclic graph")
        void testCyclicGraphOrderedVertices() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("B", "A", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNull(result.getOrderedVertices());
        }

        @Test
        @DisplayName("Should measure computation time")
        void testComputationTime() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");
            directedGraph.addEdge("A", "B", 1);
            directedGraph.addEdge("B", "C", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertTrue(result.getComputationTimeMillis() >= 0);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle graph with only isolated vertices")
        void testIsolatedVertices() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(3, result.getOrderedVertices().size());
        }

        @Test
        @DisplayName("Should handle graph with numeric vertex types")
        void testNumericVertices() {
            Graph<Integer, String> numGraph = new MapGraph<>(true);
            numGraph.addVertex(1);
            numGraph.addVertex(2);
            numGraph.addVertex(3);
            numGraph.addEdge(1, 2, "edge1");
            numGraph.addEdge(2, 3, "edge2");

            TopologicalSorting.TopologicalResult<Integer> result =
                    TopologicalSorting.topologicalSort(numGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(3, result.getOrderedVertices().size());
        }

        @Test
        @DisplayName("Should handle large chain graph")
        void testLargeChain() {
            String[] vertices = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

            for (String vertex : vertices) {
                directedGraph.addVertex(vertex);
            }

            for (int i = 0; i < vertices.length - 1; i++) {
                directedGraph.addEdge(vertices[i], vertices[i + 1], i);
            }

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(10, result.getOrderedVertices().size());

            List<String> order = result.getOrderedVertices();
            for (int i = 0; i < vertices.length - 1; i++) {
                int currentIndex = order.indexOf(vertices[i]);
                int nextIndex = order.indexOf(vertices[i + 1]);
                assertTrue(currentIndex < nextIndex);
            }
        }
    }

    @Nested
    @DisplayName("Real-World Scenario Tests")
    class RealWorldScenarioTests {

        @Test
        @DisplayName("Should handle course prerequisite graph")
        void testCoursePrerequisites() {
            directedGraph.addVertex("Math101");
            directedGraph.addVertex("Math201");
            directedGraph.addVertex("Math301");
            directedGraph.addVertex("CS101");
            directedGraph.addVertex("CS201");

            directedGraph.addEdge("Math101", "Math201", 1);
            directedGraph.addEdge("Math201", "Math301", 1);
            directedGraph.addEdge("CS101", "Math201", 1);
            directedGraph.addEdge("CS101", "CS201", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(5, result.getOrderedVertices().size());

            List<String> order = result.getOrderedVertices();
            assertTrue(order.indexOf("Math101") < order.indexOf("Math201"));
            assertTrue(order.indexOf("Math201") < order.indexOf("Math301"));
            assertTrue(order.indexOf("CS101") < order.indexOf("CS201"));
        }

        @Test
        @DisplayName("Should detect impossible course schedule with prerequisites")
        void testCircularPrerequisites() {
            directedGraph.addVertex("CourseA");
            directedGraph.addVertex("CourseB");
            directedGraph.addVertex("CourseC");

            directedGraph.addEdge("CourseA", "CourseB", 1);
            directedGraph.addEdge("CourseB", "CourseC", 1);
            directedGraph.addEdge("CourseC", "CourseA", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertEquals(3, result.getCycleVertices().size());
        }

        @Test
        @DisplayName("Should handle build dependency graph")
        void testBuildDependencies() {
            directedGraph.addVertex("Core");
            directedGraph.addVertex("Lib1");
            directedGraph.addVertex("Lib2");
            directedGraph.addVertex("App");

            directedGraph.addEdge("Core", "Lib2", 1);
            directedGraph.addEdge("Lib1", "App", 1);
            directedGraph.addEdge("Lib2", "App", 1);

            TopologicalSorting.TopologicalResult<String> result =
                    TopologicalSorting.topologicalSort(directedGraph);

            assertNotNull(result);
            assertFalse(result.hasCycles());

            List<String> order = result.getOrderedVertices();
            assertEquals("App", order.get(order.size() - 1));
            assertTrue(order.indexOf("Core") < order.indexOf("Lib2"));
        }
    }
}