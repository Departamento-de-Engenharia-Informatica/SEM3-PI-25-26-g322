package isep.ipp.pt.g322;

import isep.ipp.pt.g322.datastructures.graph.TopologicalSorting;
import isep.ipp.pt.g322.model.DirectedUpgradePlan;
import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;
import isep.ipp.pt.g322.service.StationCSVLoader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            RailwayNetwork network = new RailwayNetwork(true);

            StationCSVLoader loader = new StationCSVLoader(network);
            int stations = loader.loadStationsFromCSV("stations.csv");
            int connections = loader.loadConnectionsFromCSV("lines.csv");

            System.out.println("Network loaded: " + stations + " stations, " +
                    connections + " connections");

            //US 11
            DirectedUpgradePlan upgradePlan = new DirectedUpgradePlan(network);

            TopologicalSorting.TopologicalResult<Station> result = upgradePlan.computeUpgradePlan();

            upgradePlan.printUpgradePlan(result);
        } catch (IOException e) {
            System.err.println("Error loading network data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}