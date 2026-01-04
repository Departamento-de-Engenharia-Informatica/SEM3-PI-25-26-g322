import isep.ipp.pt.g322.datastructures.graph.Algorithms;
import isep.ipp.pt.g322.datastructures.graph.map.MapGraph;
import isep.ipp.pt.g322.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class RailwayNetworkTest {

    private RailwayNetwork directedNetwork;
    private RailwayNetwork undirectedNetwork;
    private Station station1;
    private Station station2;
    private Station station3;
    private RailConnection connection1;
    private RailConnection connection2;

    @BeforeEach
    void setUp() {
        directedNetwork = new RailwayNetwork(true);
        undirectedNetwork = new RailwayNetwork(false);

        station1 = new Station("S001", "Brussels Central", 50.8450, 4.3570, 100.0, 200.0);
        station2 = new Station("S002", "Antwerp Central", 51.2172, 4.4211, 150.0, 250.0);
        station3 = new Station("S003", "Ghent-Sint-Pieters", 51.0357, 3.7103, 200.0, 300.0);

        connection1 = new RailConnection(45.5, 120, 1000.0);
        connection2 = new RailConnection(58.2, 100, 1200.0);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create directed network")
        void testCreateDirectedNetwork() {
            RailwayNetwork network = new RailwayNetwork(true);
            assertNotNull(network);
            assertNotNull(network.getGraph());
            assertTrue(network.getGraph().isDirected());
            assertEquals(0, network.getNumStations());
            assertEquals(0, network.getNumConnections());
        }

        @Test
        @DisplayName("Should create undirected network")
        void testCreateUndirectedNetwork() {
            RailwayNetwork network = new RailwayNetwork(false);
            assertNotNull(network);
            assertNotNull(network.getGraph());
            assertFalse(network.getGraph().isDirected());
            assertEquals(0, network.getNumStations());
            assertEquals(0, network.getNumConnections());
        }
    }

    @Nested
    @DisplayName("Add Station Tests")
    class AddStationTests {

        @Test
        @DisplayName("Should add a single station successfully")
        void testAddSingleStation() {
            assertTrue(directedNetwork.addStation(station1));
            assertEquals(1, directedNetwork.getNumStations());
            assertTrue(directedNetwork.hasStation("S001"));
        }

        @Test
        @DisplayName("Should add multiple stations successfully")
        void testAddMultipleStations() {
            assertTrue(directedNetwork.addStation(station1));
            assertTrue(directedNetwork.addStation(station2));
            assertTrue(directedNetwork.addStation(station3));

            assertEquals(3, directedNetwork.getNumStations());
            assertTrue(directedNetwork.hasStation("S001"));
            assertTrue(directedNetwork.hasStation("S002"));
            assertTrue(directedNetwork.hasStation("S003"));
        }

        @Test
        @DisplayName("Should not add duplicate station")
        void testAddDuplicateStation() {
            assertTrue(directedNetwork.addStation(station1));
            assertFalse(directedNetwork.addStation(station1));
            assertEquals(1, directedNetwork.getNumStations());
        }

        @Test
        @DisplayName("Should not add station with duplicate ID")
        void testAddStationWithDuplicateId() {
            Station duplicateIdStation = new Station("S001", "Another Station", 50.0, 4.0, 120.0, 220.0);

            assertTrue(directedNetwork.addStation(station1));
            assertFalse(directedNetwork.addStation(duplicateIdStation));
            assertEquals(1, directedNetwork.getNumStations());
        }

        @Test
        @DisplayName("Should throw exception when adding null station")
        void testAddNullStation() {
            assertThrows(IllegalArgumentException.class, () -> {
                directedNetwork.addStation(null);
            });
        }
    }

    @Nested
    @DisplayName("Add Connection Tests")
    class AddConnectionTests {

        @BeforeEach
        void setUpStations() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);
        }

        @Test
        @DisplayName("Should add connection between existing stations")
        void testAddConnection() {
            assertTrue(directedNetwork.addConnection("S001", "S002", connection1));
            assertEquals(1, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should add multiple connections")
        void testAddMultipleConnections() {
            assertTrue(directedNetwork.addConnection("S001", "S002", connection1));
            assertTrue(directedNetwork.addConnection("S002", "S003", connection2));
            assertEquals(2, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should not add connection if source station doesn't exist")
        void testAddConnectionNonExistentSource() {
            assertFalse(directedNetwork.addConnection("S999", "S002", connection1));
            assertEquals(0, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should not add connection if destination station doesn't exist")
        void testAddConnectionNonExistentDestination() {
            assertFalse(directedNetwork.addConnection("S001", "S999", connection1));
            assertEquals(0, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should not add connection if both stations don't exist")
        void testAddConnectionBothNonExistent() {
            assertFalse(directedNetwork.addConnection("S888", "S999", connection1));
            assertEquals(0, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should throw exception when station ID is null")
        void testAddConnectionNullStationId() {
            assertThrows(IllegalArgumentException.class, () -> {
                directedNetwork.addConnection(null, "S002", connection1);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                directedNetwork.addConnection("S001", null, connection1);
            });
        }

        @Test
        @DisplayName("Should throw exception when connection is null")
        void testAddConnectionNullConnection() {
            assertThrows(IllegalArgumentException.class, () -> {
                directedNetwork.addConnection("S001", "S002", null);
            });
        }

        @Test
        @DisplayName("Should handle self-loop connection")
        void testAddSelfLoopConnection() {
            assertTrue(directedNetwork.addConnection("S001", "S001", connection1));
            assertEquals(1, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should not add duplicate connection in directed network")
        void testAddDuplicateConnectionDirected() {
            assertTrue(directedNetwork.addConnection("S001", "S002", connection1));
            assertFalse(directedNetwork.addConnection("S001", "S002", connection2));
            assertEquals(1, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should add bidirectional connections in directed network")
        void testAddBidirectionalInDirected() {
            assertTrue(directedNetwork.addConnection("S001", "S002", connection1));
            assertTrue(directedNetwork.addConnection("S002", "S001", connection2));
            assertEquals(2, directedNetwork.getNumConnections());
        }
    }

    @Nested
    @DisplayName("Undirected Network Tests")
    class UndirectedNetworkTests {

        @BeforeEach
        void setUpStations() {
            undirectedNetwork.addStation(station1);
            undirectedNetwork.addStation(station2);
        }

        @Test
        @DisplayName("Should create two edges for undirected connection")
        void testUndirectedConnectionCreatesDoubleEdges() {
            assertTrue(undirectedNetwork.addConnection("S001", "S002", connection1));
            // In undirected graph, one connection creates 2 edges
            assertEquals(2, undirectedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should not add duplicate in undirected network")
        void testNoDuplicateUndirected() {
            assertTrue(undirectedNetwork.addConnection("S001", "S002", connection1));
            assertFalse(undirectedNetwork.addConnection("S001", "S002", connection2));
            assertEquals(2, undirectedNetwork.getNumConnections());
        }
    }

    @Nested
    @DisplayName("Get Station Tests")
    class GetStationTests {

        @BeforeEach
        void setUpStations() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
        }

        @Test
        @DisplayName("Should retrieve existing station by ID")
        void testGetExistingStation() {
            Station retrieved = directedNetwork.getStation("S001");
            assertNotNull(retrieved);
            assertEquals("S001", retrieved.getStationId());
            assertEquals("Brussels Central", retrieved.getName());
        }

        @Test
        @DisplayName("Should return null for non-existent station")
        void testGetNonExistentStation() {
            Station retrieved = directedNetwork.getStation("S999");
            assertNull(retrieved);
        }

        @Test
        @DisplayName("Should return null for null station ID")
        void testGetNullStationId() {
            Station retrieved = directedNetwork.getStation(null);
            assertNull(retrieved);
        }
    }

    @Nested
    @DisplayName("Has Station Tests")
    class HasStationTests {

        @BeforeEach
        void setUpStations() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
        }

        @Test
        @DisplayName("Should return true for existing station")
        void testHasExistingStation() {
            assertTrue(directedNetwork.hasStation("S001"));
            assertTrue(directedNetwork.hasStation("S002"));
        }

        @Test
        @DisplayName("Should return false for non-existent station")
        void testHasNonExistentStation() {
            assertFalse(directedNetwork.hasStation("S999"));
        }

        @Test
        @DisplayName("Should return false for null station ID")
        void testHasNullStationId() {
            assertFalse(directedNetwork.hasStation(null));
        }
    }

    @Nested
    @DisplayName("Get All Stations Tests")
    class GetAllStationsTests {

        @Test
        @DisplayName("Should return empty collection for empty network")
        void testGetAllStationsEmpty() {
            Collection<Station> stations = directedNetwork.getAllStations();
            assertNotNull(stations);
            assertTrue(stations.isEmpty());
        }

        @Test
        @DisplayName("Should return all stations")
        void testGetAllStations() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);

            Collection<Station> stations = directedNetwork.getAllStations();
            assertNotNull(stations);
            assertEquals(3, stations.size());
            assertTrue(stations.contains(station1));
            assertTrue(stations.contains(station2));
            assertTrue(stations.contains(station3));
        }
    }

    @Nested
    @DisplayName("Network Statistics Tests")
    class NetworkStatisticsTests {

        @Test
        @DisplayName("Should return correct number of stations")
        void testGetNumStations() {
            assertEquals(0, directedNetwork.getNumStations());

            directedNetwork.addStation(station1);
            assertEquals(1, directedNetwork.getNumStations());

            directedNetwork.addStation(station2);
            assertEquals(2, directedNetwork.getNumStations());
        }

        @Test
        @DisplayName("Should return correct number of connections in directed network")
        void testGetNumConnectionsDirected() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);

            assertEquals(0, directedNetwork.getNumConnections());

            directedNetwork.addConnection("S001", "S002", connection1);
            assertEquals(1, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should return correct number of connections in undirected network")
        void testGetNumConnectionsUndirected() {
            undirectedNetwork.addStation(station1);
            undirectedNetwork.addStation(station2);

            assertEquals(0, undirectedNetwork.getNumConnections());

            undirectedNetwork.addConnection("S001", "S002", connection1);
            assertEquals(2, undirectedNetwork.getNumConnections());
        }
    }

    @Nested
    @DisplayName("Get Graph Tests")
    class GetGraphTests {

        @Test
        @DisplayName("Should return the underlying graph")
        void testGetGraph() {
            assertNotNull(directedNetwork.getGraph());
            assertTrue(directedNetwork.getGraph().isDirected());
        }

        @Test
        @DisplayName("Returned graph should reflect network state")
        void testGetGraphReflectsState() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addConnection("S001", "S002", connection1);

            assertEquals(2, directedNetwork.getGraph().numVertices());
            assertEquals(1, directedNetwork.getGraph().numEdges());
        }
    }

    @Nested
    @DisplayName("Complex Scenario Tests")
    class ComplexScenarioTests {

        @Test
        @DisplayName("Should handle linear railway line")
        void testLinearRailwayLine() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);

            directedNetwork.addConnection("S001", "S002", connection1);
            directedNetwork.addConnection("S002", "S003", connection2);

            assertEquals(3, directedNetwork.getNumStations());
            assertEquals(2, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should handle hub-and-spoke network")
        void testHubAndSpokeNetwork() {
            Station hub = station1;
            Station spoke1 = station2;
            Station spoke2 = station3;
            Station spoke3 = new Station("S004", "Bruges", 51.2093, 3.2247, 250.0, 350.0);

            directedNetwork.addStation(hub);
            directedNetwork.addStation(spoke1);
            directedNetwork.addStation(spoke2);
            directedNetwork.addStation(spoke3);

            directedNetwork.addConnection("S001", "S002", new RailConnection(40.0, 100, 1000.0));
            directedNetwork.addConnection("S001", "S003", new RailConnection(50.0, 100, 1100.0));
            directedNetwork.addConnection("S001", "S004", new RailConnection(60.0, 100, 1200.0));

            assertEquals(4, directedNetwork.getNumStations());
            assertEquals(3, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should handle circular route")
        void testCircularRoute() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);

            directedNetwork.addConnection("S001", "S002", new RailConnection(40.0, 100, 1000.0));
            directedNetwork.addConnection("S002", "S003", new RailConnection(50.0, 100, 1100.0));
            directedNetwork.addConnection("S003", "S001", new RailConnection(60.0, 100, 1200.0));

            assertEquals(3, directedNetwork.getNumStations());
            assertEquals(3, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should handle disconnected network components")
        void testDisconnectedComponents() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);

            directedNetwork.addConnection("S001", "S002", connection1);

            assertEquals(3, directedNetwork.getNumStations());
            assertEquals(1, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should handle large network")
        void testLargeNetwork() {
            for (int i = 1; i <= 10; i++) {
                Station s = new Station(
                        String.format("S%03d", i),
                        "Station " + i,
                        50.0 + i * 0.1,
                        4.0 + i * 0.1,
                        100.0 + i * 10.0,
                        200.0 + i * 10.0
                );
                directedNetwork.addStation(s);
            }

            for (int i = 1; i < 10; i++) {
                directedNetwork.addConnection(
                        String.format("S%03d", i),
                        String.format("S%03d", i + 1),
                        new RailConnection(50.0, 100, 1000.0 + i * 100.0)
                );
            }

            assertEquals(10, directedNetwork.getNumStations());
            assertEquals(9, directedNetwork.getNumConnections());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty network")
        void testEmptyNetwork() {
            assertEquals(0, directedNetwork.getNumStations());
            assertEquals(0, directedNetwork.getNumConnections());
            assertTrue(directedNetwork.getAllStations().isEmpty());
        }

        @Test
        @DisplayName("Should handle network with stations but no connections")
        void testStationsWithoutConnections() {
            directedNetwork.addStation(station1);
            directedNetwork.addStation(station2);
            directedNetwork.addStation(station3);

            assertEquals(3, directedNetwork.getNumStations());
            assertEquals(0, directedNetwork.getNumConnections());
        }

        @Test
        @DisplayName("Should handle adding connection before adding stations")
        void testConnectionBeforeStations() {
            assertFalse(directedNetwork.addConnection("S001", "S002", connection1));
            assertEquals(0, directedNetwork.getNumStations());
            assertEquals(0, directedNetwork.getNumConnections());
        }
    }

    @Nested
    @DisplayName("Max Flow Test")
    class MaxFlowTest {

        private RailwayNetwork network;
        private Station s1, s2, s3, s4, source, sink;

        @BeforeEach
        void setUp() {
            // Criar uma rede direcionada (true)
            network = new RailwayNetwork(true);

            // Criar Estações (Vértices)
            // (id, name, lat, lon, x, y)
            source = new Station("S", "Source", 0, 0, 0, 0);
            sink = new Station("T", "Sink", 0, 0, 0, 0);
            s1 = new Station("A", "Station A", 0, 0, 0, 0);
            s2 = new Station("B", "Station B", 0, 0, 0, 0);
            s3 = new Station("C", "Station C", 0, 0, 0, 0);
            s4 = new Station("D", "Station D", 0, 0, 0, 0);
        }

        /**
         * Auxiliar para criar conexões rapidamente.
         * RailConnection(distance, capacity, cost)
         * Apenas a capacidade interessa para este teste.
         */
        private void addLink(Station from, Station to, int capacity) {
            // Distância e custo são irrelevantes para o Max Flow, usamos valores dummy (10.0)
            network.addConnection(from.getStationId(), to.getStationId(),
                    new RailConnection(10.0, capacity, 10.0));
        }

        @Test
        @DisplayName("Teste Básico: Ligação Direta")
        void testDirectConnection() {
            network.addStation(source);
            network.addStation(sink);

            // S -> T (Capacidade 100)
            addLink(source, sink, 100);

            String result = network.calculateMaxFlow(source, sink);

            // Verifica se a string contém o valor esperado
            // Formato esperado: "source: S, target: T, maxFlowValue: 100.00"
            assertTrue(result.contains("maxFlowValue: 100.00"),
                    "O fluxo deve ser igual à capacidade da única ligação (100)");
        }

        @Test
        @DisplayName("Múltiplos Caminhos")
        void testComplexFlow() {


            network.addStation(source);
            network.addStation(sink);
            network.addStation(s1); // A
            network.addStation(s2); // B
            network.addStation(s4); // D

            addLink(source, s1, 10); // S->A
            addLink(source, s2, 10); // S->B
            addLink(s1, s2, 2);      // A->B
            addLink(s1, sink, 4);    // A->T
            addLink(s2, sink, 8);    // B->T


            addLink(s4, sink, 5);    // D->T



            String result = network.calculateMaxFlow(source, sink);

            assertTrue(result.contains("maxFlowValue: 12.00"),
                    "O fluxo máximo deve ser 12. Recebido: " + result);
        }

        @Test
        @DisplayName("Teste Grafo Desconexo (Fluxo Zero)")
        void testDisconnectedGraph() {
            network.addStation(source);
            network.addStation(sink);
            network.addStation(s1);

            addLink(source, s1, 100);

            String result = network.calculateMaxFlow(source, sink);

            assertTrue(result.contains("maxFlowValue: 0.00"),
                    "Não existe caminho entre S e T, fluxo deve ser 0");
        }

        @Test
        @DisplayName("Teste com Ciclos")
        void testGraphWithCycles() {
            network.addStation(source);
            network.addStation(sink);
            network.addStation(s1);
            network.addStation(s2);



            addLink(source, s1, 10);
            addLink(s1, s2, 10);
            addLink(s2, s1, 10);
            addLink(s2, sink, 10);

            // O algoritmo Edmonds-Karp usa BFS (caminho mais curto em arestas),


            String result = network.calculateMaxFlow(source, sink);

            assertTrue(result.contains("maxFlowValue: 10.00"),
                    "O algoritmo deve ignorar ciclos e encontrar o fluxo de 10");
        }
    }
}