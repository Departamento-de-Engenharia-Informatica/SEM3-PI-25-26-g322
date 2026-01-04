package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.datastructures.graph.Edge;
import isep.ipp.pt.g322.datastructures.graph.Graph;
import isep.ipp.pt.g322.datastructures.graph.MinimumSpanningTree;
import isep.ipp.pt.g322.datastructures.graph.map.MapGraph;
import isep.ipp.pt.g322.model.RailConnection;
import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;

import java.io.IOException;
import java.util.Comparator;

public class MinimalBackboneNetwork {

    private final RailwayNetwork network;

    public MinimalBackboneNetwork(RailwayNetwork network) {
        if (network == null) {
            throw new IllegalArgumentException("Network cannot be null");
        }
        this.network = network;
    }

    /**
     * Computes the minimal backbone network (MST).
     */
    public MinimumSpanningTree.MSTResult<Station, RailConnection> computeBackbone() {
        // to use undirected version of graph
        Graph<Station, RailConnection> graph = network.getGraph();

        Comparator<RailConnection> distanceComparator =
                Comparator.comparingDouble(RailConnection::getDistance);

        return MinimumSpanningTree.kruskal(graph, distanceComparator);
    }

    /**
     * Exports backbone to DOT file.
     */
    public void exportToDOT(MinimumSpanningTree.MSTResult<Station, RailConnection> mstResult,
                            String filename) throws IOException {
        DOTGraphExporter.exportMSTtoDOT(mstResult.getMstEdges(), network, filename);
        System.out.println("DOT file created: " + filename);
    }

    /**
     * Generates SVG from DOT file using Graphviz neato.
     */
    public void generateSVG(String dotFilename, String svgFilename) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("neato", "-Tsvg", dotFilename, "-o", svgFilename);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("SVG file created: " + svgFilename);
        } else {
            throw new IOException("Graphviz neato failed with exit code: " + exitCode);
        }
    }

    /**
     * Prints backbone network results.
     */
    public void printBackboneResults(MinimumSpanningTree.MSTResult<Station, RailConnection> result) {
        System.out.println("\n=== Minimal Backbone Network ===\n");

        if (result.isConnected()) {
            System.out.println("✓ Network is fully connected\n");
        } else {
            System.out.println("⚠ Network has " + result.getNumComponents() + " disconnected components\n");
        }

        System.out.println("Backbone Network Statistics:");
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.printf("Total edges in MST: %d%n", result.getMstEdges().size());
        System.out.printf("Total track length: %.2f km%n", result.getTotalWeight());
        System.out.printf("Total stations: %d%n", network.getNumStations());

        System.out.println("\nMST Edges:");
        System.out.println("─────────────────────────────────────────────────────────");

        for (Edge<Station, RailConnection> edge : result.getMstEdges()) {
            System.out.printf("  [%s] %s  —  [%s] %s  (%.2f km)%n",
                    edge.getVOrig().getStationId(),
                    edge.getVOrig().getName(),
                    edge.getVDest().getStationId(),
                    edge.getVDest().getName(),
                    edge.getWeight().getDistance()
            );
        }

        System.out.println("\nTemporal Analysis:");
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.printf("Computation time: %.3f ms%n", result.getComputationTimeMillis());
        System.out.printf("Time complexity: O(E log E) where E=%d%n", network.getNumConnections());
        System.out.println("Space complexity: O(V + E)");
    }

}