package isep.ipp.pt.g322.model;

import java.util.Objects;

/**
 * Represents a railway station with geographic and layout coordinates.
 * Can represent a station, terminal, or freight yard.
 *
 */
public class Station {
    private final String stationId;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final double x;
    private final double y;

    /**
     * Creates a new Station
     *
     * @param stationId unique identifier
     * @param name station name
     * @param latitude geographic latitude
     * @param longitude geographic longitude
     * @param x X coordinate for visualization
     * @param y Y coordinate for visualization
     */
    public Station(String stationId, String name, double latitude, double longitude, double x, double y) {
        if (stationId == null || stationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }

        this.stationId = stationId.trim();
        this.name = name.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.x = x;
        this.y = y;
    }

    public String getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("Station{id='%s', name='%s', lat=%.6f, lon=%.6f, x=%.2f, y=%.2f}",
                stationId, name, latitude, longitude, x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return stationId.equals(station.stationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationId);
    }
}