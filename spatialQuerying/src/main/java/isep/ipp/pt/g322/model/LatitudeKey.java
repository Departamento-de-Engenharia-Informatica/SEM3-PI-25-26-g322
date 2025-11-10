package isep.ipp.pt.g322.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LatitudeKey implements Comparable<LatitudeKey> {
    private double latitude;
    private List<Station> stations;

    public LatitudeKey(double latitude) {
        this.latitude = latitude;
        this.stations = new ArrayList<>();
    }

    public double getLatitude() {
        return latitude;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void addStation(Station station) {
        int pos = Collections.binarySearch(stations, station);
        if (pos < 0) pos = -(pos + 1);
        stations.add(pos, station);
    }

    @Override
    public int compareTo(LatitudeKey other) {
        return Double.compare(this.latitude, other.latitude);
    }

    @Override
    public String toString() {
        return String.format("Latitude{%.6f, %d stations}", latitude, stations.size());
    }
}