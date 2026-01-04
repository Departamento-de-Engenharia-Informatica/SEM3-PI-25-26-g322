package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.datastructures.graph.BellmanFord;
import isep.ipp.pt.g322.datastructures.graph.Edge;
import isep.ipp.pt.g322.model.RailConnection;
import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for computing risk-aware shortest paths in railway networks.
 * Uses Bellman-Ford algorithm to handle negative edge weights (bonuses/penalties)
 * and detect negative cycles that indicate configuration errors.
 */
public class RiskAwareRouting {
    
    private final RailwayNetwork network;

    public RiskAwareRouting(RailwayNetwork network) {
        this.network = network;
    }

    /**
     * Result of risk-aware routing computation
     */
    public static class RoutingResult {
        private final Station source;
        private final Station destination;
        private final List<RouteSegment> route;
        private final double totalCost;
        private final boolean hasNegativeCycle;
        private final List<Station> negativeCycleStations;
        private final List<RailConnection> negativeCycleEdges;
        private final String complexityAnalysis;

        public RoutingResult(Station source, Station destination,
                           List<RouteSegment> route, double totalCost,
                           boolean hasNegativeCycle,
                           List<Station> negativeCycleStations,
                           List<RailConnection> negativeCycleEdges,
                           String complexityAnalysis) {
            this.source = source;
            this.destination = destination;
            this.route = route != null ? new ArrayList<>(route) : new ArrayList<>();
            this.totalCost = totalCost;
            this.hasNegativeCycle = hasNegativeCycle;
            this.negativeCycleStations = negativeCycleStations;
            this.negativeCycleEdges = negativeCycleEdges;
            this.complexityAnalysis = complexityAnalysis;
        }

        public Station getSource() {
            return source;
        }

        public Station getDestination() {
            return destination;
        }

        public List<RouteSegment> getRoute() {
            return new ArrayList<>(route);
        }

        public double getTotalCost() {
            return totalCost;
        }

        public boolean hasNegativeCycle() {
            return hasNegativeCycle;
        }

        public List<Station> getNegativeCycleStations() {
            return negativeCycleStations;
        }

        public List<RailConnection> getNegativeCycleEdges() {
            return negativeCycleEdges;
        }

        public String getComplexityAnalysis() {
            return complexityAnalysis;
        }

        public boolean hasValidPath() {
            return !hasNegativeCycle && route != null && !route.isEmpty();
        }
    }

    /**
     * Represents a segment of the route
     */
    public static class RouteSegment {
        private final Station station;
        private final RailConnection connection;
        private final double segmentCost;
        private final double cumulativeCost;

        public RouteSegment(Station station, RailConnection connection, 
                          double segmentCost, double cumulativeCost) {
            this.station = station;
            this.connection = connection;
            this.segmentCost = segmentCost;
            this.cumulativeCost = cumulativeCost;
        }

        public Station getStation() {
            return station;
        }

        public RailConnection getConnection() {
            return connection;
        }

        public double getSegmentCost() {
            return segmentCost;
        }

        public double getCumulativeCost() {
            return cumulativeCost;
        }
    }

    /**
     * Computes the risk-aware shortest path between two stations.
     * Edge costs combine distance and penalties/bonuses.
     * @param sourceId source station ID (String)
     * @param destId destination station ID (String)
     * @return RoutingResult with path or negative cycle information
     */
    public RoutingResult computeRiskAwarePath(String sourceId, String destId) {
        Station source = network.getStation(sourceId);
        Station dest = network.getStation(destId);

        if (source == null || dest == null) {
            return new RoutingResult(source, dest, null, Double.POSITIVE_INFINITY,
                false, null, null, "Invalid source or destination");
        }

        // Run Bellman-Ford
        BellmanFord.BellmanFordResult<Station, RailConnection> result = 
            BellmanFord.shortestPath(network.getGraph(), source);

        // Check for negative cycle
        if (result.hasNegativeCycle()) {
            List<Station> cycleStations = result.getNegativeCycle();
            List<RailConnection> cycleEdges = extractCycleEdges(cycleStations);
            
            return new RoutingResult(source, dest, null, Double.POSITIVE_INFINITY,
                true, cycleStations, cycleEdges, result.getComplexityAnalysis());
        }

        // Extract path info
        BellmanFord.PathInfo<Station, RailConnection> pathInfo = 
            BellmanFord.getPathInfo(result, dest);

        if (!pathInfo.hasPath()) {
            return new RoutingResult(source, dest, null, Double.POSITIVE_INFINITY,
                false, null, null, result.getComplexityAnalysis());
        }

        // Build route segments
        List<RouteSegment> route = buildRouteSegments(pathInfo);

        return new RoutingResult(source, dest, route, pathInfo.getTotalCost(),
            false, null, null, result.getComplexityAnalysis());
    }

    /**
     * Extracts edges forming a cycle from a list of stations
     */
    private List<RailConnection> extractCycleEdges(List<Station> cycle) {
        if (cycle == null || cycle.size() < 2) {
            return new ArrayList<>();
        }

        List<RailConnection> edges = new ArrayList<>();
        for (int i = 0; i < cycle.size() - 1; i++) {
            Station from = cycle.get(i);
            Station to = cycle.get(i + 1);
            
            // Find edge between these stations
            for (Edge<Station, RailConnection> edge : network.getGraph().outgoingEdges(from)) {
                if (edge.getVDest().equals(to)) {
                    edges.add(edge.getWeight());
                    break;
                }
            }
        }

        return edges;
    }

    /**
     * Builds route segments from path information
     */
    private List<RouteSegment> buildRouteSegments(
            BellmanFord.PathInfo<Station, RailConnection> pathInfo) {
        
        List<RouteSegment> segments = new ArrayList<>();
        List<Station> path = pathInfo.getPath();
        List<Edge<Station, RailConnection>> edges = pathInfo.getEdges();
        List<Double> cumulativeCosts = pathInfo.getCumulativeCosts();

        if (path.isEmpty()) {
            return segments;
        }

        // First station (source)
        segments.add(new RouteSegment(path.get(0), null, 0.0, 0.0));

        // Intermediate and destination stations
        for (int i = 0; i < edges.size(); i++) {
            Edge<Station, RailConnection> edge = edges.get(i);
            Station station = path.get(i + 1);
            RailConnection connection = edge.getWeight();
            double segmentCost = connection.getCost();
            double cumulativeCost = cumulativeCosts.get(i);

            segments.add(new RouteSegment(station, connection, 
                segmentCost, cumulativeCost));
        }

        return segments;
    }

    /**
     * Prints the routing result in the expected format
     */
    public void printRoutingResult(RoutingResult result) {
        System.out.println("=== RISK-AWARE ROUTING RESULT ===\n");

        if (result.hasNegativeCycle()) {
            System.out.println("⚠️  NEGATIVE CYCLE DETECTED!");
            System.out.println("Configuration error: The network contains a negative cycle.");
            System.out.println("\nStations involved:");
            
            List<Station> cycleStations = result.getNegativeCycleStations();
            if (cycleStations != null) {
                for (int i = 0; i < cycleStations.size(); i++) {
                    Station s = cycleStations.get(i);
                    System.out.printf("  %d. Station %s: %s%n", i + 1, s.getStationId(), s.getName());
                }
            }

            System.out.println("\nEdges involved:");
            List<RailConnection> cycleEdges = result.getNegativeCycleEdges();
            if (cycleEdges != null) {
                for (int i = 0; i < cycleEdges.size(); i++) {
                    RailConnection conn = cycleEdges.get(i);
                    System.out.printf("  %d. Distance: %.2f, Capacity: %d, Cost: %.2f%n",
                        i + 1, conn.getDistance(), conn.getCapacity(), conn.getCost());
                }
            }

            System.out.println("\n" + result.getComplexityAnalysis());
            return;
        }

        if (!result.hasValidPath()) {
            System.out.println("No path exists from Station " + result.getSource().getStationId() +
                " to Station " + result.getDestination().getStationId());
            return;
        }

        // Print path
        System.out.println("Source: Station " + result.getSource().getStationId() + 
            " (" + result.getSource().getName() + ")");
        System.out.println("Destination: Station " + result.getDestination().getStationId() + 
            " (" + result.getDestination().getName() + ")");
        System.out.printf("Total Cost: %.4f%n", result.getTotalCost());
        System.out.println("\nRoute:");
        System.out.println("-".repeat(100));

        List<RouteSegment> route = result.getRoute();
        for (int i = 0; i < route.size(); i++) {
            RouteSegment segment = route.get(i);
            Station station = segment.getStation();

            if (i == 0) {
                // Source
                System.out.printf("START: Station %s (%s)%n", 
                    station.getStationId(), station.getName());
            } else {
                // Intermediate or destination
                RailConnection conn = segment.getConnection();
                System.out.printf("  ↓ [Distance: %.2f, Capacity: %d, Cost: %.4f]%n",
                    conn.getDistance(), conn.getCapacity(), segment.getSegmentCost());
                System.out.printf("Station %s (%s) - Cumulative Cost: %.4f%n",
                    station.getStationId(), station.getName(), segment.getCumulativeCost());
            }
        }

        System.out.println("-".repeat(100));
        System.out.printf("\nPath Summary: %s → %s (Total Cost: %.4f)%n",
            result.getSource().getName(),
            result.getDestination().getName(),
            result.getTotalCost());

        System.out.println("\n" + result.getComplexityAnalysis());
    }

    /**
     * Formats routing result as expected in the specification
     */
    public String formatAsSpecification(RoutingResult result) {
        if (result.hasNegativeCycle()) {
            StringBuilder sb = new StringBuilder();
            sb.append("NEGATIVE CYCLE DETECTED\n");
            sb.append("Stations: ");
            List<Station> stations = result.getNegativeCycleStations();
            if (stations != null) {
                for (int i = 0; i < stations.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(stations.get(i).getStationId());
                }
            }
            sb.append("\nEdges: ");
            List<RailConnection> edges = result.getNegativeCycleEdges();
            if (edges != null) {
                for (int i = 0; i < edges.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(String.format("cost=%.2f", edges.get(i).getCost()));
                }
            }
            return sb.toString();
        }

        if (!result.hasValidPath()) {
            return "NO PATH FOUND";
        }

        StringBuilder sb = new StringBuilder();
        List<RouteSegment> route = result.getRoute();
        
        for (int i = 0; i < route.size(); i++) {
            RouteSegment segment = route.get(i);
            if (i > 0) sb.append(", ");
            
            if (i == 0) {
                sb.append(String.format("%s", segment.getStation().getStationId()));
            } else {
                sb.append(String.format("cost=%.4f, %s", 
                    segment.getSegmentCost(), segment.getStation().getStationId()));
            }
        }
        
        sb.append(String.format(", total_cost=%.4f", result.getTotalCost()));
        return sb.toString();
    }

    /**
     * Demonstrates risk-aware routing by finding a path between connected stations
     */
    public RoutingResult demonstrateRiskAwareRouting() {
        // Find two connected stations
        List<Station> stations = new ArrayList<>(network.getAllStations());
        Station source = null;
        Station dest = null;
        
        // Find a station with outgoing edges
        for (Station s : stations) {
            if (!network.getGraph().outgoingEdges(s).isEmpty()) {
                source = s;
                // Find a destination 2 hops away
                for (Edge<Station, RailConnection> edge : network.getGraph().outgoingEdges(s)) {
                    Station neighbor = edge.getVDest();
                    if (!network.getGraph().outgoingEdges(neighbor).isEmpty()) {
                        for (Edge<Station, RailConnection> edge2 : network.getGraph().outgoingEdges(neighbor)) {
                            dest = edge2.getVDest();
                            break;
                        }
                    }
                    if (dest != null) break;
                }
                if (dest != null) break;
            }
        }
        
        if (source != null && dest != null) {
            return computeRiskAwarePath(source.getStationId(), dest.getStationId());
        }
        
        // Return empty result if no path found
        return new RoutingResult(null, null, new ArrayList<>(), 0, false, null, null, "No connected stations found");
    }
}

