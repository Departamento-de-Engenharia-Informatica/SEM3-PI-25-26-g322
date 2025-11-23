package isep.ipp.pt.g322.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DistanceKey implements Comparable<DistanceKey> {
    private final double distanceKm;
    private final List<Station> stations;

    public DistanceKey(double distanceKm) {
        this.distanceKm = distanceKm;
        this.stations = new ArrayList<>();
    }

    public void addStation(Station station) {
        stations.add(station);
        // sort by name desc as per US10 requirement
        stations.sort(Comparator.comparing(Station::getStation).reversed());
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public List<Station> getStations() {
        return new ArrayList<>(stations);
    }

    @Override
    public int compareTo(DistanceKey other) {
        // sort by distance asc
        return Double.compare(this.distanceKm, other.distanceKm);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DistanceKey that = (DistanceKey) obj;
        return Math.abs(this.distanceKm - that.distanceKm) < 0.0001;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(distanceKm);
    }

    @Override
    public String toString() {
        return String.format("%.2f km (%d stations)", distanceKm, stations.size());
    }
}
