package isep.ipp.pt.g322;

import isep.ipp.pt.g322.datastructures.graph.CentralityMetrics;
import isep.ipp.pt.g322.datastructures.graph.MinimumSpanningTree;
import isep.ipp.pt.g322.datastructures.graph.TopologicalSorting;
import isep.ipp.pt.g322.model.DirectedUpgradePlan;
import isep.ipp.pt.g322.model.RailConnection;
import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;
import isep.ipp.pt.g322.service.HubCentralityAnalysis;
import isep.ipp.pt.g322.service.MinimalBackboneNetwork;
import isep.ipp.pt.g322.service.RiskAwareRouting;
import isep.ipp.pt.g322.service.StationCSVLoader;

import java.io.IOException;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            // US11 - directed graph
            RailwayNetwork directedNetwork = new RailwayNetwork(true);
            StationCSVLoader loader = new StationCSVLoader(directedNetwork);
            int stations1 = loader.loadStationsFromCSV("stations.csv");
            int connections1 = loader.loadConnectionsFromCSV("lines.csv");

            System.out.println("Network loaded: " + stations1 + " stations, " +
                    connections1 + " connections\n");

            DirectedUpgradePlan upgradePlan = new DirectedUpgradePlan(directedNetwork);
            TopologicalSorting.TopologicalResult<Station> result = upgradePlan.computeUpgradePlan();
            upgradePlan.printUpgradePlan(result);

            // US12 - undirected graph as per requirements
            RailwayNetwork undirectedNetwork = new RailwayNetwork(false);
            StationCSVLoader loader2 = new StationCSVLoader(undirectedNetwork);
            int stations2 = loader2.loadStationsFromCSV("stations.csv");
            int connections2 = loader2.loadConnectionsFromCSV("lines.csv");

            System.out.println("Network loaded: " + stations2 + " stations, " +
                    connections2 + " connections\n");

            MinimalBackboneNetwork backbone = new MinimalBackboneNetwork(undirectedNetwork);
            MinimumSpanningTree.MSTResult<Station, RailConnection> mstResult = backbone.computeBackbone();
            backbone.printBackboneResults(mstResult);

            backbone.exportToDOT(mstResult, "backbone.dot");
            try {
                backbone.generateSVG("backbone.dot", "backbone.svg");
            } catch (IOException | InterruptedException e) {
                System.err.println("Error generating svg");
            }


            // US13
            HubCentralityAnalysis hubAnalysis = new HubCentralityAnalysis(undirectedNetwork);
            CentralityMetrics.CentralityResult<Station> centralityResult = hubAnalysis.analyzeCentrality();
            hubAnalysis.printCentralityResults(centralityResult);

            hubAnalysis.exportToCSV(centralityResult, "hub_centrality.csv");


            // --- US14: Maximum Flow ---
            System.out.println("\n--- US14: Maximum Flow Analysis (Demonstração com 5 Pares) ---");

            if (undirectedNetwork != null && undirectedNetwork.getNumStations() > 10) {
                // Converter a coleção de estações numa lista para acesso por índice
                var allStations = new java.util.ArrayList<>(undirectedNetwork.getAllStations());

                System.out.println("[Exemplo 1: Ligação Direta]");
                testMaxFlow(undirectedNetwork, "920", "908"); // Obourg -> Nimy

                System.out.println("\n[Exemplos 2-5: Pares Aleatorios/Distantes]");
                // Usamos índices fixos mas espaçados para apanhar zonas diferentes da Bélgica
                if (allStations.size() > 50) {
                    testMaxFlow(undirectedNetwork, allStations.get(0).getStationId(), allStations.get(5).getStationId());
                    testMaxFlow(undirectedNetwork, allStations.get(10).getStationId(), allStations.get(20).getStationId());
                    testMaxFlow(undirectedNetwork, allStations.get(30).getStationId(), allStations.get(35).getStationId());
                    testMaxFlow(undirectedNetwork, allStations.get(allStations.size()-1).getStationId(), allStations.get(0).getStationId());
                } else {

                    for (int i = 0; i < 4 && i < allStations.size() - 1; i++) {
                        testMaxFlow(undirectedNetwork, allStations.get(i).getStationId(), allStations.get(i+1).getStationId());
                    }
                }

                System.out.println("\nAnalise Temporal:");
                System.out.println("---------------------------------------------------------");
                System.out.printf("Complexidade temporal: O(V x E^2) = O(%d x %d^2) = O(%d)%n",
                        undirectedNetwork.getNumStations(),
                        undirectedNetwork.getNumConnections(),
                        undirectedNetwork.getNumStations() * 
                        undirectedNetwork.getNumConnections() * 
                        undirectedNetwork.getNumConnections());
                System.out.println("  Explicacao: Edmonds-Karp encontra caminhos de aumento usando BFS (O(E) por caminho).");
                System.out.println("  Maximo VxE/2 caminhos de aumento existem, resultando em O(VxE^2) total.");
                System.out.printf("Complexidade espacial: O(V + E) = O(%d + %d) = O(%d)%n",
                        undirectedNetwork.getNumStations(),
                        undirectedNetwork.getNumConnections(),
                        undirectedNetwork.getNumStations() + undirectedNetwork.getNumConnections());
                System.out.println("  Explicacao: Armazena grafo residual e estruturas de dados do BFS.");
                System.out.println("Algoritmo: Edmonds-Karp (Ford-Fulkerson com BFS)");
            } else {
                System.out.println("Rede insuficiente para gerar 5 exemplos.");
            }
            System.out.println();

            // US15
            System.out.println("\n--- US15: Risk-Aware Shortest Path ---");
            RiskAwareRouting riskRouting = new RiskAwareRouting(directedNetwork);
            RiskAwareRouting.RoutingResult routingResult = riskRouting.demonstrateRiskAwareRouting();
            
            // Print specification format
            System.out.println("\nSpecification Format:");
            System.out.println(riskRouting.formatAsSpecification(routingResult));
            
            // Print detailed analysis
            System.out.println("\nDetailed Analysis:");
            riskRouting.printRoutingResult(routingResult);

        } catch (IOException e) {
            System.err.println("Error loading network data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }


    }
    private static void testMaxFlow(RailwayNetwork network, String sourceId, String sinkId) {
        Station source = network.getStation(sourceId);
        Station sink = network.getStation(sinkId);

        if (source != null && sink != null) {
            String result = network.calculateMaxFlow(source, sink);
            System.out.println(result);
        } else {
            System.out.println("Skip: Estações " + sourceId + " ou " + sinkId + " não encontradas.");
        }
    }
}