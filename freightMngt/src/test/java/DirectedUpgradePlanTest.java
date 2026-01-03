import isep.ipp.pt.g322.model.DirectedUpgradePlan;

import isep.ipp.pt.g322.datastructures.graph.TopologicalSorting;
import isep.ipp.pt.g322.model.RailConnection;
import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectedUpgradePlanTest {

    private RailwayNetwork directedNetwork;
    private RailwayNetwork undirectedNetwork;
    private Station station1;
    private Station station2;
    private Station station3;
    private Station station4;
    private RailConnection connection1;
    private RailConnection connection2;
    private RailConnection connection3;

    @BeforeEach
    void setUp() {
        directedNetwork = new RailwayNetwork(true);
        undirectedNetwork = new RailwayNetwork(false);

        station1 = new Station("S001", "Brussels Central", 50.8450, 4.3570, 100.0, 200.0);
        station2 = new Station("S002", "Antwerp Central", 51.2172, 4.4211, 150.0, 250.0);
        station3 = new Station("S003", "Ghent-Sint-Pieters", 51.0357, 3.7103, 200.0, 300.0);
        station4 = new Station("S004", "Bruges", 51.2093, 3.2247, 250.0, 350.0);

        connection1 = new RailConnection(45.5, 120, 1000.0);
        connection2 = new RailConnection(58.2, 100, 1200.0);
        connection3 = new RailConnection(42.1, 110, 1100.0);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create upgrade plan with directed network")
        void testCreateWithDirectedNetwork() {
            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            assertNotNull(plan);
        }

        @Test
        @DisplayName("Should throw exception when network is null")
        void testCreateWithNullNetwork() {
            assertThrows(IllegalArgumentException.class, () -> {
                new DirectedUpgradePlan(null);
            });
        }

        @Test
        @DisplayName("Should throw exception when network is undirected")
        void testCreateWithUndirectedNetwork() {
            assertThrows(IllegalArgumentException.class, () -> {
                new DirectedUpgradePlan(undirectedNetwork);
            });
        }
    }

    @Nested
    @DisplayName("Acyclic Network Tests")
    class AcyclicNetworkTests {

        @Test
        @DisplayName("Should compute upgrade plan for empty network")
        void testEmptyNetwork() {
            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertNotNull(result.getOrderedVertices());
            assertTrue(result.getOrderedVertices().isEmpty());
            assertTrue(result.getCycleVertices().isEmpty());
            assertTrue(result.getCycleEdges().isEmpty());
        }

        @Test
        @DisplayName("Should compute upgrade plan for single station")
        void testSingleStation() {
            directedNetwork.addStation(station1);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(1, result.getOrderedVertices().size());
            assertEquals(station1, result.getOrderedVertices().get(0));
        }

        @Test
        @DisplayName("Should compute upgrade plan for linear dependency chain")
        void testLinearDependencyChain() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S003", connection2);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());

            List<Station> order = result.getOrderedVertices();
            assertEquals(3, order.size());

            int idx1 = order.indexOf(station1);
            int idx2 = order.indexOf(station2);
            int idx3 = order.indexOf(station3);

            assertTrue(idx1 < idx2, "S1 should come before S2");
            assertTrue(idx2 < idx3, "S2 should come before S3");
        }

        @Test
        @DisplayName("Should compute upgrade plan for diamond dependency")
        void testDiamondDependency() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addStation(station4);

            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S001", "S003", connection2);
            directedNetwork.addConnection("S002", "S004", connection3);
            directedNetwork.addConnection("S003", "S004", new RailConnection(30.0, 100, 1300.0));

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());

            List<Station> order = result.getOrderedVertices();
            assertEquals(4, order.size());

            assertEquals(station1, order.get(0));

            assertEquals(station4, order.get(3));

            int idx2 = order.indexOf(station2);
            int idx3 = order.indexOf(station3);
            int idx4 = order.indexOf(station4);

            assertTrue(idx2 < idx4);
            assertTrue(idx3 < idx4);
        }

        @Test
        @DisplayName("Should compute upgrade plan for multiple source nodes")
        void testMultipleSources() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);

            directedNetwork.addConnection("S001", "S003", connection1);
            directedNetwork.addConnection("S002", "S003", connection2);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());

            List<Station> order = result.getOrderedVertices();
            assertEquals(3, order.size());

            assertEquals(station3, order.get(2));
        }

        @Test
        @DisplayName("Should compute upgrade plan for disconnected components")
        void testDisconnectedComponents() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addStation(station4);

            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S003", "S004", connection2);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());

            List<Station> order = result.getOrderedVertices();
            assertEquals(4, order.size());

            int idx1 = order.indexOf(station1);
            int idx2 = order.indexOf(station2);
            int idx3 = order.indexOf(station3);
            int idx4 = order.indexOf(station4);

            assertTrue(idx1 < idx2, "S1 should come before S2");
            assertTrue(idx3 < idx4, "S3 should come before S4");
        }

        @Test
        @DisplayName("Should compute upgrade plan for isolated stations")
        void testIsolatedStations() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(3, result.getOrderedVertices().size());
        }
    }

    @Nested
    @DisplayName("Cyclic Network Tests")
    class CyclicNetworkTests {

        @Test
        @DisplayName("Should detect simple two-station cycle")
        void testSimpleTwoStationCycle() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S001", connection2);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());

            List<Station> cycleStations = result.getCycleVertices();
            assertEquals(2, cycleStations.size());
            assertTrue(cycleStations.contains(station1));
            assertTrue(cycleStations.contains(station2));

            assertFalse(result.getCycleEdges().isEmpty());
        }

        @Test
        @DisplayName("Should detect three-station cycle")
        void testThreeStationCycle() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S003", connection2);
            directedNetwork.addConnection("S003", "S001", connection3);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());

            List<Station> cycleStations = result.getCycleVertices();
            assertEquals(3, cycleStations.size());
            assertTrue(cycleStations.contains(station1));
            assertTrue(cycleStations.contains(station2));
            assertTrue(cycleStations.contains(station3));
        }

        @Test
        @DisplayName("Should detect self-loop as cycle")
        void testSelfLoop() {
            directedNetwork.addStation(station1);
            directedNetwork.addConnection("S001", "S001", connection1);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());
            assertTrue(result.getCycleVertices().contains(station1));
        }

        @Test
        @DisplayName("Should detect cycle in mixed network")
        void testMixedNetworkWithCycle() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addStation(station4);

            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S003", connection2);
            directedNetwork.addConnection("S003", "S002", connection3);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());

            List<Station> cycleStations = result.getCycleVertices();
            assertTrue(cycleStations.contains(station2));
            assertTrue(cycleStations.contains(station3));

            assertFalse(cycleStations.contains(station1));
            assertFalse(cycleStations.contains(station4));
        }

        @Test
        @DisplayName("Should detect multiple separate cycles")
        void testMultipleSeparateCycles() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addStation(station4);

            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S001", connection2);
            directedNetwork.addConnection("S003", "S004", connection3);
            directedNetwork.addConnection("S004", "S003", new RailConnection(40.0, 100, 1300.0));

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());

            List<Station> cycleStations = result.getCycleVertices();
            assertEquals(4, cycleStations.size());
            assertTrue(cycleStations.contains(station1));
            assertTrue(cycleStations.contains(station2));
            assertTrue(cycleStations.contains(station3));
            assertTrue(cycleStations.contains(station4));
        }
    }

    @Nested
    @DisplayName("Result Validation Tests")
    class ResultValidationTests {

        @Test
        @DisplayName("Should return valid result for acyclic network")
        void testValidResultAcyclic() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addConnection("S001", "S002", connection1);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertNotNull(result.getOrderedVertices());
            assertEquals(2, result.getOrderedVertices().size());
            assertTrue(result.getCycleVertices().isEmpty());
            assertTrue(result.getCycleEdges().isEmpty());
        }

        @Test
        @DisplayName("Should return valid result for cyclic network")
        void testValidResultCyclic() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S001", connection2);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());
            assertFalse(result.getCycleVertices().isEmpty());
            assertFalse(result.getCycleEdges().isEmpty());
        }

        @Test
        @DisplayName("Should have positive computation time")
        void testComputationTime() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addConnection("S001", "S002", connection1);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertTrue(result.getComputationTimeMillis() >= 0);
        }
    }

    @Nested
    @DisplayName("Real-World Scenario Tests")
    class RealWorldScenarioTests {

        @Test
        @DisplayName("Should handle main line with branch lines (tree structure)")
        void testMainLineWithBranches() {
            // Main line: S1 -> S2 -> S3
            // Branch from S2: S2 -> S4
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addStation(station4);

            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S003", connection2);
            directedNetwork.addConnection("S002", "S004", connection3);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());

            List<Station> order = result.getOrderedVertices();
            assertEquals(4, order.size());

            assertEquals(station1, order.getFirst());

            int idx2 = order.indexOf(station2);
            int idx3 = order.indexOf(station3);
            int idx4 = order.indexOf(station4);

            assertTrue(idx2 < idx3);
            assertTrue(idx2 < idx4);
        }

        @Test
        @DisplayName("Should detect impossible upgrade scenario with circular dependency")
        void testImpossibleUpgradeCircular() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S001", connection2);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertTrue(result.hasCycles());
            assertNull(result.getOrderedVertices());
        }

        @Test
        @DisplayName("Should handle complex network with multiple dependencies")
        void testComplexNetworkMultipleDependencies() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addStation(station4);

            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S001", "S003", connection2);
            directedNetwork.addConnection("S002", "S004", connection3);
            directedNetwork.addConnection("S003", "S004", new RailConnection(35.0, 100, 1300.0));

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());

            List<Station> order = result.getOrderedVertices();
            assertEquals(4, order.size());

            assertEquals(station1, order.get(0));
            assertEquals(station4, order.get(3));
        }

        @Test
        @DisplayName("Should handle sequential upgrade phases")
        void testSequentialUpgradePhases() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
            directedNetwork.addStation(station4);

            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S003", connection2);
            directedNetwork.addConnection("S003", "S004", connection3);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());

            List<Station> order = result.getOrderedVertices();
            assertEquals(4, order.size());

            assertEquals(station1, order.get(0));
            assertEquals(station2, order.get(1));
            assertEquals(station3, order.get(2));
            assertEquals(station4, order.get(3));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle network with only one station")
        void testSingleStationNetwork() {
            directedNetwork.addStation(station1);

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(1, result.getOrderedVertices().size());
            assertEquals(station1, result.getOrderedVertices().get(0));
        }

        @Test
        @DisplayName("Should handle large chain of dependencies")
        void testLargeChain() {
            Station[] stations = new Station[10];
            for (int i = 0; i < 10; i++) {
                stations[i] = new Station(
                        String.format("S%03d", i + 1),
                        "Station " + (i + 1),
                        50.0 + i * 0.1,
                        4.0 + i * 0.1,
                        100.0 + i * 10.0,
                        200.0 + i * 10.0
                );
                directedNetwork.addStation(stations[i]);
            }

            for (int i = 0; i < 9; i++) {
                directedNetwork.addConnection(
                        stations[i].getStationId(),
                        stations[i + 1].getStationId(),
                        new RailConnection(50.0, 100, 1000.0 + i * 100.0)
                );
            }

            DirectedUpgradePlan plan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = plan.computeUpgradePlan();

            assertNotNull(result);
            assertFalse(result.hasCycles());
            assertEquals(10, result.getOrderedVertices().size());

            List<Station> order = result.getOrderedVertices();
            for (int i = 0; i < 9; i++) {
                int currentIdx = order.indexOf(stations[i]);
                int nextIdx = order.indexOf(stations[i + 1]);
                assertTrue(currentIdx < nextIdx);
            }
        }
    }
}