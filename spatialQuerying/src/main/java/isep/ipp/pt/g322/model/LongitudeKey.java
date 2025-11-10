package isep.ipp.pt.g322.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LongitudeKey implements Comparable<LongitudeKey> {
    private double longitude;
    private List<Station> stations;

    public LongitudeKey(double longitude) {
        this.longitude = longitude;
        this.stations = new ArrayList<>();
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void addStation(Station station) {
        stations.add(station);
        Collections.sort(stations); // this is to sort by station name
    }

    @Override
    public int compareTo(LongitudeKey other) {
        return Double.compare(this.longitude, other.longitude);
    }

    @Override
    public String toString() {
        return String.format("Longitude{%.6f, %d stations}", longitude, stations.size());
    }
}