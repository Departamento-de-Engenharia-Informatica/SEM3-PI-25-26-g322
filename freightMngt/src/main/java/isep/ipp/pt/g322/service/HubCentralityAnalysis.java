package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.datastructures.graph.CentralityMetrics;
import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class HubCentralityAnalysis {

    private final RailwayNetwork network;

    public HubCentralityAnalysis(RailwayNetwork network) {
        if (network == null) {
            throw new IllegalArgumentException("Network cannot be null");
        }
        this.network = network;
    }

    /**
     * Computes centrality metrics for all stations.
     */
    public CentralityMetrics.CentralityResult<Station> analyzeCentrality() {
        return CentralityMetrics.computeCentralities(network.getGraph());
    }

    /**
     * Exports centrality results to CSV.
     */
    public void exportToCSV(CentralityMetrics.CentralityResult<Station> result,
                            String filename) throws IOException { // helper method to analyze better with several files used for the graphs and compare. delete later

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("stid,stname,degree,strength,betweenness,harmonic_closeness,hubscore\n");

            List<CentralityMetrics.VertexCentrality<Station>> sorted =
                    result.getCentralities().stream()
                            .sorted(Comparator.comparingDouble(
                                    CentralityMetrics.VertexCentrality<Station>::getHubScore).reversed())
                            .toList();

            for (CentralityMetrics.VertexCentrality<Station> vc : sorted) {
                Station s = vc.getVertex();
                writer.write(String.format("%s,%s,%d,%.2f,%.6f,%.6f,%.6f\n",
                        s.getStationId(),
                        s.getName(),
                        vc.getDegree(),
                        vc.getStrength(),
                        vc.getBetweenness(),
                        vc.getHarmonicCloseness(),
                        vc.getHubScore()
                ));
            }
        }

        System.out.println("Centrality data exported to: " + filename);
    }

    /**
     * Prints centrality analysis results.
     */
    public void printCentralityResults(CentralityMetrics.CentralityResult<Station> result) {
        System.out.println("\n=== Rail Hub Centrality Analysis ===\n");

        List<CentralityMetrics.VertexCentrality<Station>> sorted =
                result.getCentralities().stream()
                        .sorted(Comparator.comparingDouble(
                                CentralityMetrics.VertexCentrality<Station>::getHubScore).reversed())
                        .toList();

        System.out.println("Top Hub Stations (by Hub Score):");
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.printf("%-6s %-25s %6s %8s %12s %12s %10s%n",
                "StID", "Station Name", "Degree", "Strength", "Betweenness", "Harmonic", "HubScore");
        System.out.println("─────────────────────────────────────────────────────────");

        int topN = Math.min(15, sorted.size());
        for (int i = 0; i < topN; i++) {
            CentralityMetrics.VertexCentrality<Station> vc = sorted.get(i);
            Station s = vc.getVertex();

            System.out.printf("%-6s %-25s %6d %8.2f %12.6f %12.6f %10.6f%n",
                    s.getStationId(),
                    truncate(s.getName(), 25),
                    vc.getDegree(),
                    vc.getStrength(),
                    vc.getBetweenness(),
                    vc.getHarmonicCloseness(),
                    vc.getHubScore()
            );
        }

        System.out.println("\nHub Score Formula:");
        System.out.println("  hubscore = 0.35 × betweenness + 0.35 × harmonic_closeness + 0.30 × strength");
        System.out.println("  (All metrics normalized to [0,1])");

        System.out.println("\nTemporal Analysis:");
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.printf("Computation time: %.3f ms%n", result.getComputationTimeMillis());
        System.out.printf("Time complexity: O(V × (V + E)) where V=%d, E=%d%n",
                network.getNumStations(), network.getNumConnections());
        System.out.println("Space complexity: O(V²)");
    }

    private String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }
}
