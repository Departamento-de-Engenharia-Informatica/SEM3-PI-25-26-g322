package isep.ipp.pt.g322.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TimezoneCountryKey implements Comparable<TimezoneCountryKey> {
    private String timezoneGroup;
    private String country;
    private List<Station> stations;

    public TimezoneCountryKey(String timeZoneGroup, String country) {
        this.timezoneGroup = timeZoneGroup;
        this.country = country;
        this.stations = new ArrayList<>();
    }

    public String getTimezoneGroup() {
        return timezoneGroup;
    }

    public String getCountry() {
        return country;
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
    public int compareTo(TimezoneCountryKey other) {
        int timezoneCompareTerm = this.timezoneGroup.compareTo(other.timezoneGroup);
        if (timezoneCompareTerm != 0) {
            return timezoneCompareTerm;
        }
        return this.country.compareTo(other.country);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimezoneCountryKey that = (TimezoneCountryKey) o;
        return Objects.equals(timezoneGroup, that.timezoneGroup) &&
                Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timezoneGroup, country);
    }

    @Override
    public String toString() {
        return String.format("TZ-Country{%s, %s, %d stations}", timezoneGroup, country, stations.size());
    }
}
