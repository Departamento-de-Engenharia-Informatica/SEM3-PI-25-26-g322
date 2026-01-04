package isep.ipp.pt.g322.model;

import isep.ipp.pt.g322.datastructures.graph.Algorithms;
import isep.ipp.pt.g322.datastructures.graph.map.*;

import java.util.*;

/**
 * Represents a network of railway stations interconnected by rail lines.
 * Manages the Belgian rail network with stations and connections.
 *
 */
public class RailwayNetwork {

    private final MapGraph<Station, RailConnection> network;
    private final Map<String, Station> stationMap;

    /**
     * Creates a new railway network.
     *
     * @param directed true if the network is directed, false if undirected
     */
    public RailwayNetwork(boolean directed) {
        this.network = new MapGraph<>(directed);
        this.stationMap = new HashMap<>();
    }

    /**
     * Adds a station to the network.
     *
     * @param station the station to add
     * @return true if the station was added, false if it already exists
     */
    public boolean addStation(Station station) {
        if (station == null) {
            throw new IllegalArgumentException("Station cannot be null");
        }

        if (stationMap.containsKey(station.getStationId())) {
            return false;
        }

        boolean added = network.addVertex(station);
        if (added) {
            stationMap.put(station.getStationId(), station);
        }
        return added;
    }

    /**
     * Adds a station to the network with individual parameters.
     * Convenience method for creating and adding a station.
     *
     * @param stationId unique station identifier
     * @param name station name
     * @param latitude geographic latitude
     * @param longitude geographic longitude
     * @return true if the station was added, false if it already exists
     */
    public boolean addStation(String stationId, String name, double latitude, double longitude) {
        return addStation(new Station(stationId, name, latitude, longitude, 0.0, 0.0));
    }

    /**
     * Adds a rail connection between two stations.
     * If the stations don't exist, they are NOT added automatically.
     *
     * @param stationId1 the first station ID
     * @param stationId2 the second station ID
     * @param connection the rail connection
     * @return true if the connection was added, false otherwise
     */
    public boolean addConnection(String stationId1, String stationId2, RailConnection connection) {
        if (stationId1 == null || stationId2 == null || connection == null) {
            throw new IllegalArgumentException("Station IDs and connection cannot be null");
        }

        Station station1 = stationMap.get(stationId1);
        Station station2 = stationMap.get(stationId2);

        if (station1 == null || station2 == null) {
            return false;  // Stations must exist before adding connections
        }

        return network.addEdge(station1, station2, connection);
    }

    /**
     * Adds a rail connection between two stations with individual parameters.
     * Convenience method that creates a RailConnection.
     *
     * @param stationId1 the first station ID
     * @param stationId2 the second station ID
     * @param distance distance in kilometers
     * @param capacity maximum flow of trains per day
     * @param cost combined metric (can be negative for bonuses)
     * @return true if the connection was added, false otherwise
     */
    public boolean addConnection(String stationId1, String stationId2, 
                                 double distance, int capacity, double cost) {
        return addConnection(stationId1, stationId2, new RailConnection(distance, capacity, cost));
    }

    /**
     * Gets a station by its ID.
     *
     * @param stationId the station ID
     * @return the Station object, or null if not found
     */
    public Station getStation(String stationId) {
        return stationMap.get(stationId);
    }

    /**
     * Checks if a station exists in the network.
     *
     * @param stationId the station ID
     * @return true if the station exists, false otherwise
     */
    public boolean hasStation(String stationId) {
        return stationMap.containsKey(stationId);
    }

    /**
     * Returns the underlying graph.
     *
     * @return the MapGraph representing the railway network
     */
    public MapGraph<Station, RailConnection> getGraph() {
        return network;
    }

    /**
     * Returns all stations in the network.
     *
     * @return collection of all stations
     */
    public Collection<Station> getAllStations() {
        return new ArrayList<>(stationMap.values());
    }

    /**
     * Returns the total number of stations.
     *
     * @return number of stations
     */
    public int getNumStations() {
        return network.numVertices();
    }

    /**
     * Returns the total number of connections.
     *
     * @return number of rail connections
     */
    public int getNumConnections() {
        return network.numEdges();
    }


    /**
     * US14: Calcular fluxo máximo entre duas estações.
     */
    public String calculateMaxFlow(Station source, Station sink) {
        double flow = Algorithms.edmondsKarp(this.network, source, sink, rc -> (double) rc.getCapacity());


        return String.format(java.util.Locale.US, "source_stid: %s, target_stid: %s, maxFlowValue: %.2f",
                source.getStationId(), sink.getStationId(), flow);
    }


    @Override
    public String toString() {
        return String.format("RailwayNetwork{stations=%d, connections=%d, directed=%b}",
                getNumStations(), getNumConnections(), network.isDirected());
    }
}