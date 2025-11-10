package isep.ipp.pt.g322.model;

import java.util.Objects;

public class Station implements Comparable<Station> {
    private String station;
    private double latitude;
    private double longitude;
    private String country;
    private String timeZone;
    private String timeZoneGroup;
    private boolean isCity;
    private boolean isMainStation;
    private boolean isAirport;

    public Station(String station, double latitude, double longitude, String country,
                   String timeZone, String timeZoneGroup, boolean isCity,
                   boolean isMainStation, boolean isAirport) {
        this.station = station;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.timeZone = timeZone;
        this.timeZoneGroup = timeZoneGroup;
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.isAirport = isAirport;
    }

    public String getStation() {
        return station;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCountry() {
        return country;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getTimeZoneGroup() {
        return timeZoneGroup;
    }

    public boolean isCity() {
        return isCity;
    }

    public boolean isMainStation() {
        return isMainStation;
    }

    public boolean isAirport() {
        return isAirport;
    }

    public boolean isValid() {
        if (station == null || station.trim().isEmpty()) return false;
        if (country == null || country.trim().isEmpty()) return false;
        if (timeZoneGroup == null || timeZoneGroup.trim().isEmpty()) return false;

        if (latitude < -90.0 || latitude > 90.0) return false;

        if (longitude < -180.0 || longitude > 180.0) return false;

        return true;
    }

    @Override
    public int compareTo(Station other) {
        return this.station.compareTo(other.station);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station1 = (Station) o;
        return Double.compare(station1.latitude, latitude) == 0 &&
                Double.compare(station1.longitude, longitude) == 0 &&
                Objects.equals(station, station1.station);
    }

    @Override
    public int hashCode() {
        return Objects.hash(station, latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format("Station{name='%s', lat=%.6f, lon=%.6f, country='%s', tz='%s', city=%b, main=%b, airport=%b}",
                station, latitude, longitude, country, timeZoneGroup, isCity, isMainStation, isAirport);
    }
}