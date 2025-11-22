package isep.ipp.pt.g322.model;

import isep.ipp.pt.g322.datastructures.tree.AVL;
import isep.ipp.pt.g322.datastructures.tree.KdTree;

import java.io.*;
import java.util.*;

public class StationManager {
    private AVL<LatitudeKey> latitudeIndex;
    private AVL<LongitudeKey> longitudeIndex;
    private AVL<TimezoneCountryKey> timezoneCountryIndex;

    private int totalStations;
    private int validStations;
    private int invalidStations;
    private List<String> validationErrors;

    public StationManager() {
        this.latitudeIndex = new AVL<>();
        this.longitudeIndex = new AVL<>();
        this.timezoneCountryIndex = new AVL<>();
        this.validationErrors = new ArrayList<>();
        this.totalStations = 0;
        this.validStations = 0;
        this.invalidStations = 0;
    }

    public int loadStationsFromCSV(String csvFilePath) {
        String line;
        boolean isFirstLine = true;

        try (InputStream inputStream = getClass().getResourceAsStream(csvFilePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                System.err.println("Resource not found: " + csvFilePath);
                return 0;
            }

            while ((line = br.readLine()) != null) {
                // to skip header line of csv
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                totalStations++;
                String[] values = line.split(",");

                try {
                    Station station = parseStation(values);

                    if (station.isValid()) {
                        addStationToIndices(station);
                        validStations++;
                    } else {
                        invalidStations++;
                        validationErrors.add(String.format("Line %d: Invalid data - %s",
                                totalStations, station.getStation()));
                    }
                } catch (Exception e) {
                    invalidStations++;
                    validationErrors.add(String.format("Line %d: Parse error - %s",
                            totalStations, e.getMessage()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return validStations;
    }

    private Station parseStation(String[] values) {
        String country = values[0].trim();
        String timezone = values[1].trim().replace("('", "").replace("'", "");
        String timezoneGroup = values[3].trim();
        String stationName = values[4].trim();
        double latitude = parseDouble(values[5]);
        double longitude = parseDouble(values[6]);
        boolean isCity = parseBoolean(values[7]);
        boolean isMainStation = parseBoolean(values[8]);
        boolean isAirport = parseBoolean(values[9]);

        return new Station(stationName, latitude, longitude, country,
                timezone, timezoneGroup, isCity, isMainStation, isAirport);
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private boolean parseBoolean(String value) {
        return "TRUE".equalsIgnoreCase(value.trim());
    }

    private void addStationToIndices(Station station) {
        addToLatitudeIndex(station);

        addToLongitudeIndex(station);

        addToTimeZoneCountryIndex(station);
    }

    private void addToLatitudeIndex(Station station) {
        LatitudeKey searchKey = new LatitudeKey(station.getLatitude());
        LatitudeKey existingKey = findLatitudeKey(searchKey);

        if (existingKey != null) {
            existingKey.addStation(station);
        } else {
            searchKey.addStation(station);
            latitudeIndex.insert(searchKey);
        }
    }

    private void addToLongitudeIndex(Station station) {
        LongitudeKey searchKey = new LongitudeKey(station.getLongitude());
        LongitudeKey existingKey = findLongitudeKey(searchKey);

        if (existingKey != null) {
            existingKey.addStation(station);
        } else {
            searchKey.addStation(station);
            longitudeIndex.insert(searchKey);
        }
    }

    private void addToTimeZoneCountryIndex(Station station) {
        TimezoneCountryKey searchKey = new TimezoneCountryKey(
                station.getTimeZoneGroup(), station.getCountry());
        TimezoneCountryKey existingKey = findTimeZoneCountryKey(searchKey);

        if (existingKey != null) {
            existingKey.addStation(station);
        } else {
            searchKey.addStation(station);
            timezoneCountryIndex.insert(searchKey);
        }
    }

    private LatitudeKey findLatitudeKey(LatitudeKey key) {
        return latitudeIndex.find(key);
    }

    private LongitudeKey findLongitudeKey(LongitudeKey key) {
        return longitudeIndex.find(key);
    }

    private TimezoneCountryKey findTimeZoneCountryKey(TimezoneCountryKey key) {
        return timezoneCountryIndex.find(key);
    }

    public List<Station> getStationsByTimeZoneGroup(String timeZoneGroup) {
        List<Station> result = new ArrayList<>();

        TimezoneCountryKey minKey = new TimezoneCountryKey(timeZoneGroup, "");
        TimezoneCountryKey maxKey = new TimezoneCountryKey(timeZoneGroup, "\uffff");

        List<TimezoneCountryKey> matchingKeys = timezoneCountryIndex.findRange(minKey, maxKey);

        for (TimezoneCountryKey key : matchingKeys) {
            result.addAll(key.getStations());
        }

        result.sort(Comparator.comparing(Station::getStation));

        return result;
    }

    public List<Station> getStationsByTimeZoneWindow(String[] timeZoneGroups) {
        List<Station> result = new ArrayList<>();

        for (String tzGroup : timeZoneGroups) {
            TimezoneCountryKey minKey = new TimezoneCountryKey(tzGroup, "");
            TimezoneCountryKey maxKey = new TimezoneCountryKey(tzGroup, "\uffff");

            List<TimezoneCountryKey> keys = timezoneCountryIndex.findRange(minKey, maxKey);
            for (TimezoneCountryKey key : keys) {
                result.addAll(key.getStations());
            }
        }

        return result;
    }

    public List<Station> getStationsByLatitudeRange(double minLat, double maxLat) {
        List<Station> result = new ArrayList<>();

        LatitudeKey minKey = new LatitudeKey(minLat);
        LatitudeKey maxKey = new LatitudeKey(maxLat);

        List<LatitudeKey> keys = latitudeIndex.findRange(minKey, maxKey);

        for (LatitudeKey key : keys) {
            result.addAll(key.getStations());
        }

        return result;
    }

    public List<Station> getStationsByLongitudeRange(double minLon, double maxLon) {
        LongitudeKey minKey = new LongitudeKey(minLon);
        LongitudeKey maxKey = new LongitudeKey(maxLon);

        List<LongitudeKey> keys = longitudeIndex.findRange(minKey, maxKey);

        List<Station> result = new ArrayList<>();
        for (LongitudeKey key : keys) {
            result.addAll(key.getStations());
        }

        return result;
    }

    public int getTotalStations() {
        return totalStations;
    }

    public int getValidStations() {
        return validStations;
    }

    public int getInvalidStations() {
        return invalidStations;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public int getLatitudeIndexSize() {
        return latitudeIndex.size();
    }

    public int getLongitudeIndexSize() {
        return longitudeIndex.size();
    }

    public int getTimeZoneCountryIndexSize() {
        return timezoneCountryIndex.size();
    }

    public void printStatistics() {
        System.out.println("=== Station Index Statistics ===");
        System.out.println("Total stations processed: " + totalStations);
        System.out.println("Valid stations loaded: " + validStations);
        System.out.println("Invalid stations rejected: " + invalidStations);
        System.out.println("\nIndex Sizes:");
        System.out.println("  Latitude index: " + getLatitudeIndexSize() + " unique latitudes");
        System.out.println("  Longitude index: " + getLongitudeIndexSize() + " unique longitudes");
        System.out.println("  TimeZone-Country index: " + getTimeZoneCountryIndexSize() + " unique combinations");

        if (!validationErrors.isEmpty() && validationErrors.size() <= 10) {
            System.out.println("\nValidation Errors:");
            for (String error : validationErrors) {
                System.out.println("  " + error);
            }
        } else if (validationErrors.size() > 10) {
            System.out.println("\nShowing first 10 validation errors:");
            for (int i = 0; i < 10; i++) {
                System.out.println("  " + validationErrors.get(i));
            }
            System.out.println("  ... and " + (validationErrors.size() - 10) + " more errors");
        }
    }


    public String getComplexityAnalysis() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Temporal Complexity Analysis ===\n");
        sb.append("\n1. Loading stations from CSV:\n");
        sb.append("   - Time: O(n * log n) where n is the number of stations\n");
        sb.append("   - Each insertion into AVL tree: O(log n)\n");
        sb.append("   - Space: O(n) for storing all stations\n");

        sb.append("\n2. Query by Time Zone Group:\n");
        sb.append("   - Time: O(m * log k) where m is number of matches, k is tree size\n");
        sb.append("   - In-order traversal: O(k)\n");
        sb.append("   - Filtering: O(m)\n");

        sb.append("\n3. Query by Time Zone Window:\n");
        sb.append("   - Time: O(m * log k) similar to single timezone query\n");
        sb.append("   - Additional overhead for set lookup: O(1) per comparison\n");

        sb.append("\n4. Range queries (latitude/longitude):\n");
        sb.append("   - Time: O(k) for in-order traversal\n");
        sb.append("   - Could be optimized with range search to O(log k + m)\n");

        sb.append("\n5. US08 - Geographical Rectangle Search (KD-Tree):\n");
        sb.append("   - Construction: O(n log n) where n is the number of stations\n");
        sb.append("   - Search (average case): O(log n + k) where k is the number of results\n");
        sb.append("   - Search (worst case): O(n) if all nodes are visited\n");
        sb.append("   - Pruning optimization: Avoids scanning subtrees outside region\n");
        sb.append("   - Space: O(n) for storing the KD-tree structure\n");

        return sb.toString();
    }

    public KdTree buildKdTreeFromIndices() {
        List<Station> allStations = getStationsByLatitudeRange(-90.0, 90.0);

        return new KdTree(allStations);
    }

    /**
     * Metodido para carregar estações diretamente para uma KD-Tree a partir de um ficheiro CSV.
     * Usar apenas para spacial queries (US08) sem necessidade dos índices AVL.
     */
    public KdTree loadStationsDirectlyToKdTree(String csvFilePath) {
        List<Station> stations = new ArrayList<>();
        String line;
        boolean isFirstLine = true;

        try (InputStream inputStream = getClass().getResourceAsStream(csvFilePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                System.err.println("Resource not found: " + csvFilePath);
                return null;
            }

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");
                try {
                    Station station = parseStation(values);
                    if (station.isValid()) {
                        stations.add(station);
                        validStations++;
                    }
                } catch (Exception e) {
                    // Skip invalid stations
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            return null;
        }

        return new KdTree(stations);
    }
}