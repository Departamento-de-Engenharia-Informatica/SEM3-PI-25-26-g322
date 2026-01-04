package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.datastructures.graph.Edge;
import isep.ipp.pt.g322.model.RailConnection;
import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DOTGraphExporter {

    /**
     * Exports a railway network MST to DOT format with XY coordinates.
     */
    public static void exportMSTtoDOT(List<Edge<Station, RailConnection>> mstEdges,
                                      RailwayNetwork network,
                                      String filename) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("graph MinimalBackbone {\n");
            writer.write("  layout=neato;\n");
            writer.write("  overlap=false;\n");
            writer.write("  splines=true;\n\n");

            // Write all stations (vertices)
            for (Station station : network.getAllStations()) {
                writer.write(String.format("  \"%s\" [label=\"%s\", pos=\"%.2f,%.2f!\"];\n",
                        station.getStationId(),
                        station.getName(),
                        station.getX(),
                        station.getY()
                ));
            }

            writer.write("\n");

            // Write MST edges
            for (Edge<Station, RailConnection> edge : mstEdges) {
                Station from = edge.getVOrig();
                Station to = edge.getVDest();
                RailConnection conn = edge.getWeight();

                writer.write(String.format("  \"%s\" -- \"%s\" [label=\"%.1f km\"];\n",
                        from.getStationId(),
                        to.getStationId(),
                        conn.getDistance()
                ));
            }

            writer.write("}\n");
        }
    }
}