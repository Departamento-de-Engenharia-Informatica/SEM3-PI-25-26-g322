import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;
import isep.ipp.pt.g322.service.RiskAwareRouting;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class RiskAwareRoutingTest {

    @Nested
    @DisplayName("Basic Routing Tests")
    class BasicRoutingTests {

        @Test
        @DisplayName("Should find shortest path in simple network")
        void testSimpleRoute() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "Station A", 0.0, 0.0);
            network.addStation("2", "Station B", 1.0, 1.0);
            network.addStation("3", "Station C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 5.0);
            network.addConnection("2", "3", 15.0, 80, 7.0);
            network.addConnection("1", "3", 30.0, 50, 20.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "3");

            assertFalse(result.hasNegativeCycle());
            assertTrue(result.hasValidPath());
            assertEquals(12.0, result.getTotalCost()); // 5 + 7
            assertEquals(3, result.getRoute().size()); // A, B, C
        }

        @Test
        @DisplayName("Should handle direct connection")
        void testDirectConnection() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);

            network.addConnection("1", "2", 10.0, 100, 5.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "2");

            assertTrue(result.hasValidPath());
            assertEquals(5.0, result.getTotalCost());
            assertEquals(2, result.getRoute().size());
        }

        @Test
        @DisplayName("Should handle no path scenario")
        void testNoPath() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 5.0);
            // No connection to C

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "3");

            assertFalse(result.hasNegativeCycle());
            assertFalse(result.hasValidPath());
            assertEquals(Double.POSITIVE_INFINITY, result.getTotalCost());
        }
    }

    @Nested
    @DisplayName("Negative Weight Tests")
    class NegativeWeightTests {

        @Test
        @DisplayName("Should handle negative costs (bonuses)")
        void testNegativeCosts() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 5.0);
            network.addConnection("2", "3", 15.0, 80, -3.0); // Bonus route
            network.addConnection("1", "3", 30.0, 50, 1.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "3");

            assertTrue(result.hasValidPath());
            assertEquals(1.0, result.getTotalCost()); // Direct route 1->3 = 1 (cheaper than 1->2->3 which is 5-3=2)
        }

        @Test
        @DisplayName("Should prefer route with bonus over direct route")
        void testBonusPreference() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);
            network.addStation("4", "D", 3.0, 3.0);

            network.addConnection("1", "4", 20.0, 100, 10.0); // Direct
            network.addConnection("1", "2", 5.0, 100, 3.0);
            network.addConnection("2", "3", 5.0, 100, -1.0); // Bonus
            network.addConnection("3", "4", 5.0, 100, 2.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "4");

            assertTrue(result.hasValidPath());
            assertEquals(4.0, result.getTotalCost()); // 3 + (-1) + 2 = 4
            assertEquals(4, result.getRoute().size()); // A -> B -> C -> D
        }
    }

    @Nested
    @DisplayName("Negative Cycle Detection Tests")
    class NegativeCycleTests {

        @Test
        @DisplayName("Should detect simple negative cycle")
        void testSimpleNegativeCycle() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 2.0);
            network.addConnection("2", "3", 10.0, 100, -3.0);
            network.addConnection("3", "1", 10.0, 100, -1.0); // Cycle: 2 + (-3) + (-1) = -2

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "2");

            assertTrue(result.hasNegativeCycle());
            assertNotNull(result.getNegativeCycleStations());
            assertNotNull(result.getNegativeCycleEdges());
        }

        @Test
        @DisplayName("Should detect negative cycle in complex network")
        void testComplexNegativeCycle() {
            RailwayNetwork network = new RailwayNetwork(true);
            for (int i = 1; i <= 6; i++) {
                network.addStation(String.valueOf(i), "Station " + i, i * 1.0, i * 1.0);
            }

            network.addConnection("1", "2", 10.0, 100, 3.0);
            network.addConnection("2", "3", 10.0, 100, 2.0);
            network.addConnection("3", "4", 10.0, 100, -5.0);
            network.addConnection("4", "5", 10.0, 100, 1.0);
            network.addConnection("5", "2", 10.0, 100, -2.0); // Creates negative cycle
            network.addConnection("1", "6", 10.0, 100, 10.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "6");

            assertTrue(result.hasNegativeCycle());
        }

        @Test
        @DisplayName("Should not flag positive cycle as negative")
        void testPositiveCycleNotFlagged() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 2.0);
            network.addConnection("2", "3", 10.0, 100, 3.0);
            network.addConnection("3", "1", 10.0, 100, 1.0); // Positive cycle

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "2");

            assertFalse(result.hasNegativeCycle());
        }
    }

    @Nested
    @DisplayName("Route Segment Tests")
    class RouteSegmentTests {

        @Test
        @DisplayName("Should provide detailed route segments")
        void testRouteSegments() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 5.0);
            network.addConnection("2", "3", 15.0, 80, 7.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "3");

            var segments = result.getRoute();
            assertEquals(3, segments.size());

            // First segment (source)
            assertEquals("1", segments.get(0).getStation().getStationId());
            assertNull(segments.get(0).getConnection());
            assertEquals(0.0, segments.get(0).getCumulativeCost());

            // Second segment
            assertEquals("2", segments.get(1).getStation().getStationId());
            assertNotNull(segments.get(1).getConnection());
            assertEquals(5.0, segments.get(1).getSegmentCost());
            assertEquals(5.0, segments.get(1).getCumulativeCost());

            // Third segment
            assertEquals("3", segments.get(2).getStation().getStationId());
            assertEquals(7.0, segments.get(2).getSegmentCost());
            assertEquals(12.0, segments.get(2).getCumulativeCost());
        }

        @Test
        @DisplayName("Should handle single station route")
        void testSingleStationRoute() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "1");

            assertTrue(result.hasValidPath());
            assertEquals(0.0, result.getTotalCost());
            assertEquals(1, result.getRoute().size());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle invalid source")
        void testInvalidSource() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("999", "2");

            assertFalse(result.hasValidPath());
            assertEquals(Double.POSITIVE_INFINITY, result.getTotalCost());
        }

        @Test
        @DisplayName("Should handle invalid destination")
        void testInvalidDestination() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "999");

            assertFalse(result.hasValidPath());
        }

        @Test
        @DisplayName("Should handle zero cost edges")
        void testZeroCostEdges() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 0.0);
            network.addConnection("2", "3", 10.0, 100, 0.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "3");

            assertTrue(result.hasValidPath());
            assertEquals(0.0, result.getTotalCost());
        }
    }

    @Nested
    @DisplayName("Format Tests")
    class FormatTests {

        @Test
        @DisplayName("Should format valid route correctly")
        void testValidRouteFormat() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 5.0);
            network.addConnection("2", "3", 15.0, 80, 7.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "3");

            String formatted = routing.formatAsSpecification(result);
            assertNotNull(formatted);
            assertTrue(formatted.contains("1"));
            assertTrue(formatted.contains("2"));
            assertTrue(formatted.contains("3"));
            assertTrue(formatted.contains("total_cost"));
        }

        @Test
        @DisplayName("Should format negative cycle correctly")
        void testNegativeCycleFormat() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addStation("3", "C", 2.0, 2.0);

            network.addConnection("1", "2", 10.0, 100, 2.0);
            network.addConnection("2", "3", 10.0, 100, -3.0);
            network.addConnection("3", "1", 10.0, 100, -1.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "2");

            String formatted = routing.formatAsSpecification(result);
            assertTrue(formatted.contains("NEGATIVE CYCLE"));
        }

        @Test
        @DisplayName("Should format no path correctly")
        void testNoPathFormat() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "2");

            String formatted = routing.formatAsSpecification(result);
            assertEquals("NO PATH FOUND", formatted);
        }
    }

    @Nested
    @DisplayName("Complexity Analysis Tests")
    class ComplexityAnalysisTests {

        @Test
        @DisplayName("Should provide complexity analysis")
        void testComplexityAnalysis() {
            RailwayNetwork network = new RailwayNetwork(true);
            network.addStation("1", "A", 0.0, 0.0);
            network.addStation("2", "B", 1.0, 1.0);
            network.addConnection("1", "2", 10.0, 100, 5.0);

            RiskAwareRouting routing = new RiskAwareRouting(network);
            RiskAwareRouting.RoutingResult result = routing.computeRiskAwarePath("1", "2");

            String analysis = result.getComplexityAnalysis();
            assertNotNull(analysis);
            assertTrue(analysis.contains("Complexidade"));
        }
    }
}
