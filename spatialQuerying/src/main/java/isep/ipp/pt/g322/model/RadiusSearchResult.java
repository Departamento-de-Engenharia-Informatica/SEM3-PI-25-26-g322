package isep.ipp.pt.g322.model;

import isep.ipp.pt.g322.datastructures.tree.AVL;

import java.util.ArrayList;
import java.util.List;

/**
 * Representative class for the result of radius search with sorted tree and density summary
 */
public class RadiusSearchResult {
    private final AVL<DistanceKey> sortedByDistance;
    private final StationDensitySummary stationDensitySummary;
    private final double radiusKm;
    private final double centerLat;
    private final double centerLon;

    public RadiusSearchResult(List<Station> stations, List<Double> distances,
                              double radiusKm, double centerLat, double centerLon) {
        this.radiusKm = radiusKm;
        this.centerLat = centerLat;
        this.centerLon = centerLon;

        // build AVL tree sorted by distance (asc) and name (desc)
        this.sortedByDistance = new AVL<>();

        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            double distance = distances.get(i);

            double roundedDistance = Math.round(distance * 100.0) / 100.0;

            DistanceKey searchKey = new DistanceKey(roundedDistance);
            DistanceKey existingKey = sortedByDistance.find(searchKey);

            if (existingKey != null) {
                existingKey.addStation(station);
            } else {
                searchKey.addStation(station);
                sortedByDistance.insert(searchKey);
            }
        }

        this.stationDensitySummary = new StationDensitySummary(stations, radiusKm, centerLat, centerLon);
    }

    public AVL<DistanceKey> getSortedByDistance() {
        return sortedByDistance;
    }

    public StationDensitySummary getStationDensitySummary() {
        return stationDensitySummary;
    }

    public double getRadiusKm() {
        return radiusKm;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public double getCenterLon() {
        return centerLon;
    }

    public List<Station> getAllStationsSorted() {
        List<Station> result = new ArrayList<>();
        List<DistanceKey> keys = (List<DistanceKey>) sortedByDistance.inOrder();

        for (DistanceKey key : keys) {
            result.addAll(key.getStations());
        }

        return result;
    }

    public int getTotalStations() {
        return stationDensitySummary.getTotalStations();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Radius Search: %.1f km around (%.4f, %.4f)\n",
                radiusKm, centerLat, centerLon));
        sb.append(String.format("Stations found: %d\n", getTotalStations()));
        sb.append(String.format("Unique distance groups: %d\n", sortedByDistance.size()));
        return sb.toString();
    }
}
