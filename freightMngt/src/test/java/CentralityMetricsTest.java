import isep.ipp.pt.g322.datastructures.graph.CentralityMetrics;

import isep.ipp.pt.g322.datastructures.graph.Graph;

import isep.ipp.pt.g322.datastructures.graph.map.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class CentralityMetricsTest {

    private Graph<String, Double> undirectedGraph;
    private Graph<String, Double> directedGraph;
    private Graph<String, Double> emptyGraph;
    private Graph<String, Double> singleVertexGraph;

    @BeforeEach
    void setUp() {
        undirectedGraph = new MapGraph<>(false);
        directedGraph = new MapGraph<>(true);
        emptyGraph = new MapGraph<>(false);
        singleVertexGraph = new MapGraph<>(false);
    }

    @Nested
    @DisplayName("Empty and Single Vertex Graph Tests")
    class EmptyAndSingleVertexTests {

        @Test
        @DisplayName("Should handle empty graph")
        void testEmptyGraph() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(emptyGraph);

            assertNotNull(result);
            assertTrue(result.getCentralities().isEmpty());
            assertTrue(result.getComputationTimeNanos() >= 0);
        }

        @Test
        @DisplayName("Should handle single vertex graph")
        void testSingleVertexGraph() {
            singleVertexGraph.addVertex("A");

            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(singleVertexGraph);

            assertNotNull(result);
            assertEquals(1, result.getCentralities().size());

            CentralityMetrics.VertexCentrality<String> centrality = result.getCentralities().get(0);
            assertEquals("A", centrality.getVertex());
            assertEquals(0, centrality.getDegree());
            assertEquals(0.0, centrality.getStrength());
            assertEquals(0.0, centrality.getBetweenness());
            assertEquals(0.0, centrality.getHarmonicCloseness());
            assertEquals(0.0, centrality.getHubScore());
        }
    }

    @Nested
    @DisplayName("Simple Undirected Graph Tests")
    class UndirectedGraphTests {

        @BeforeEach
        void setUpUndirectedGraph() {
            undirectedGraph.addVertex("A");
            undirectedGraph.addVertex("B");
            undirectedGraph.addVertex("C");

            undirectedGraph.addEdge("A", "B", 1.0);
            undirectedGraph.addEdge("B", "C", 2.0);
            undirectedGraph.addEdge("C", "A", 1.5);
        }

        @Test
        @DisplayName("Should compute degree correctly for triangle")
        void testDegreeInTriangle() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                assertEquals(2, vc.getDegree(), "Each vertex in triangle should have degree 2");
            }
        }

        @Test
        @DisplayName("Should compute strength correctly for triangle")
        void testStrengthInTriangle() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            Map<String, Double> expectedStrength = new HashMap<>();
            expectedStrength.put("A", 2.5);
            expectedStrength.put("B", 3.0);
            expectedStrength.put("C", 3.5);

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                assertEquals(expectedStrength.get(vc.getVertex()), vc.getStrength(), 0.001);
            }
        }

        @Test
        @DisplayName("Should compute betweenness correctly for triangle")
        void testBetweennessInTriangle() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                assertEquals(0.0, vc.getBetweenness(), 0.001,
                        "Vertices in triangle should have 0 betweenness");
            }
        }

        @Test
        @DisplayName("Should compute harmonic closeness for triangle")
        void testHarmonicClosenessInTriangle() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                assertEquals(2.0, vc.getHarmonicCloseness(), 0.001);
            }
        }
    }

    @Nested
    @DisplayName("Line Graph Tests")
    class LineGraphTests {

        @BeforeEach
        void setUpLineGraph() {
            undirectedGraph.addVertex("A");
            undirectedGraph.addVertex("B");
            undirectedGraph.addVertex("C");
            undirectedGraph.addVertex("D");

            undirectedGraph.addEdge("A", "B", 1.0);
            undirectedGraph.addEdge("B", "C", 1.0);
            undirectedGraph.addEdge("C", "D", 1.0);
        }

        @Test
        @DisplayName("Should compute betweenness for line graph")
        void testBetweennessInLineGraph() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            Map<String, Double> betweennessMap = new HashMap<>();
            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                betweennessMap.put(vc.getVertex(), vc.getBetweenness());
            }

            assertEquals(0.0, betweennessMap.get("A"), 0.001);
            assertEquals(0.0, betweennessMap.get("D"), 0.001);

            assertTrue(betweennessMap.get("B") > 0);
            assertTrue(betweennessMap.get("C") > 0);

            assertEquals(betweennessMap.get("B"), betweennessMap.get("C"), 0.001);
        }

        @Test
        @DisplayName("Should compute harmonic closeness for line graph")
        void testHarmonicClosenessInLineGraph() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            Map<String, Double> harmonicMap = new HashMap<>();
            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                harmonicMap.put(vc.getVertex(), vc.getHarmonicCloseness());
            }

            assertTrue(harmonicMap.get("B") > harmonicMap.get("A"));
            assertTrue(harmonicMap.get("C") > harmonicMap.get("A"));
            assertTrue(harmonicMap.get("B") > harmonicMap.get("D"));
            assertTrue(harmonicMap.get("C") > harmonicMap.get("D"));

            assertEquals(harmonicMap.get("A"), harmonicMap.get("D"), 0.001);
            assertEquals(harmonicMap.get("B"), harmonicMap.get("C"), 0.001);
        }
    }
    @Nested
    @DisplayName("Directed Graph Tests")
    class DirectedGraphTests {

        @BeforeEach
        void setUpDirectedGraph() {
            directedGraph.addVertex("A");
            directedGraph.addVertex("B");
            directedGraph.addVertex("C");

            directedGraph.addEdge("A", "B", 1.0);
            directedGraph.addEdge("B", "C", 2.0);
        }

        @Test
        @DisplayName("Should compute out-degree for directed graph")
        void testOutDegreeInDirectedGraph() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(directedGraph);

            Map<String, Integer> degreeMap = new HashMap<>();
            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                degreeMap.put(vc.getVertex(), vc.getDegree());
            }

            assertEquals(1, degreeMap.get("A"));
            assertEquals(1, degreeMap.get("B"));
            assertEquals(0, degreeMap.get("C"));
        }

        @Test
        @DisplayName("Should compute strength for directed graph")
        void testStrengthInDirectedGraph() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(directedGraph);

            Map<String, Double> strengthMap = new HashMap<>();
            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                strengthMap.put(vc.getVertex(), vc.getStrength());
            }

            assertEquals(1.0, strengthMap.get("A"), 0.001);
            assertEquals(2.0, strengthMap.get("B"), 0.001);
            assertEquals(0.0, strengthMap.get("C"), 0.001);
        }
    }

    @Nested
    @DisplayName("Disconnected Graph Tests")
    class DisconnectedGraphTests {

        @BeforeEach
        void setUpDisconnectedGraph() {
            undirectedGraph.addVertex("A");
            undirectedGraph.addVertex("B");
            undirectedGraph.addVertex("C");
            undirectedGraph.addVertex("D");

            undirectedGraph.addEdge("A", "B", 1.0);
            undirectedGraph.addEdge("C", "D", 1.0);
        }

        @Test
        @DisplayName("Should handle disconnected components")
        void testDisconnectedComponents() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            assertNotNull(result);
            assertEquals(4, result.getCentralities().size());

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                assertEquals(1, vc.getDegree());
            }
        }

        @Test
        @DisplayName("Should compute harmonic closeness for disconnected graph")
        void testHarmonicClosenessInDisconnectedGraph() {
            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            Map<String, Double> harmonicMap = new HashMap<>();
            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                harmonicMap.put(vc.getVertex(), vc.getHarmonicCloseness());
            }

            assertEquals(harmonicMap.get("A"), harmonicMap.get("B"), 0.001);
            assertEquals(harmonicMap.get("C"), harmonicMap.get("D"), 0.001);
            assertEquals(1.0, harmonicMap.get("A"), 0.001);
        }
    }

    @Nested
    @DisplayName("CentralityResult Tests")
    class CentralityResultTests {
        @Test
        @DisplayName("Should return list of centralities")
        void testGetCentralities() {
            undirectedGraph.addVertex("A");
            undirectedGraph.addVertex("B");
            undirectedGraph.addEdge("A", "B", 1.0);

            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            List<CentralityMetrics.VertexCentrality<String>> centralities =
                    result.getCentralities();

            assertNotNull(centralities);
            assertEquals(2, centralities.size());
        }
    }

    @Nested
    @DisplayName("VertexCentrality Tests")
    class VertexCentralityTests {

        @Test
        @DisplayName("Should store all centrality metrics")
        void testVertexCentralityGetters() {
            CentralityMetrics.VertexCentrality<String> vc =
                    new CentralityMetrics.VertexCentrality<>(
                            "TestVertex",
                            5,
                            10.5,
                            0.75,
                            3.2,
                            0.85
                    );

            assertEquals("TestVertex", vc.getVertex());
            assertEquals(5, vc.getDegree());
            assertEquals(10.5, vc.getStrength(), 0.001);
            assertEquals(0.75, vc.getBetweenness(), 0.001);
            assertEquals(3.2, vc.getHarmonicCloseness(), 0.001);
            assertEquals(0.85, vc.getHubScore(), 0.001);
        }
    }

    @Nested
    @DisplayName("Hub Score Calculation Tests")
    class HubScoreTests {

        @Test
        @DisplayName("Should calculate hub score with correct weights")
        void testHubScoreWeights() {
            undirectedGraph.addVertex("A");
            undirectedGraph.addVertex("B");
            undirectedGraph.addEdge("A", "B", 1.0);

            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                double hubScore = vc.getHubScore();
                assertTrue(hubScore >= 0.0 && hubScore <= 1.0,
                        "Hub score should be in [0, 1] range after normalization");
            }
        }

        @Test
        @DisplayName("Should have hub score of 0 for isolated vertex")
        void testHubScoreForIsolatedVertex() {
            undirectedGraph.addVertex("A");
            undirectedGraph.addVertex("B");

            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                assertEquals(0.0, vc.getHubScore(), 0.001);
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle non-numeric edge weights")
        void testNonNumericWeights() {
            Graph<String, String> stringWeightGraph = new MapGraph<>(false);
            stringWeightGraph.addVertex("A");
            stringWeightGraph.addVertex("B");
            stringWeightGraph.addEdge("A", "B", "weight");

            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(stringWeightGraph);

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                assertEquals(0.0, vc.getStrength(), 0.001);
            }
        }

        @Test
        @DisplayName("Should normalize metrics to [0,1] range")
        void testNormalization() {
            undirectedGraph.addVertex("A");
            undirectedGraph.addVertex("B");
            undirectedGraph.addVertex("C");
            undirectedGraph.addVertex("D");

            undirectedGraph.addEdge("A", "B", 1.0);
            undirectedGraph.addEdge("B", "C", 2.0);
            undirectedGraph.addEdge("B", "D", 3.0);

            CentralityMetrics.CentralityResult<String> result =
                    CentralityMetrics.computeCentralities(undirectedGraph);

            for (CentralityMetrics.VertexCentrality<String> vc : result.getCentralities()) {
                double hubScore = vc.getHubScore();
                assertTrue(hubScore >= 0.0, "Hub score should be >= 0");
                assertTrue(hubScore <= 1.0, "Hub score should be <= 1");
            }
        }
    }
}