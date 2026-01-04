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

            // US15
            RiskAwareRouting riskRouting = new RiskAwareRouting(directedNetwork);
            RiskAwareRouting.RoutingResult routingResult = riskRouting.demonstrateRiskAwareRouting();
            riskRouting.printRoutingResult(routingResult);

        } catch (IOException e) {
            System.err.println("Error loading network data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}