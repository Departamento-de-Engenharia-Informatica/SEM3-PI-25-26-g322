package isep.ipp.pt.g322;


import isep.ipp.pt.g322.model.*;
import isep.ipp.pt.g322.datastructures.tree.KDTree2;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        StationManager indexManager = new StationManager();

        String csvPath = "/train_stations_europe.csv";
        System.out.println("Loading stations from CSV...\n");
        int loadedStations = indexManager.loadStationsFromCSV(csvPath);

        if (loadedStations == 0) {
            System.err.println("No stations loaded. Please check the CSV file path.");
            return;
        }

        indexManager.printStatistics();
        System.out.println("\n" + "=".repeat(60) + "\n");

        // US 06
        runSampleQueries(indexManager);
        System.out.println("\n" + "=".repeat(60) + "\n");


        //US 07
        runUS07Demonstration(indexManager);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // US09
        runUS09ProximitySearch(indexManager);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // US10
        runUS10RadiusSearchWithSummary(indexManager);

        System.out.println("\n" + "=".repeat(60) + "\n");
        System.out.println(indexManager.getComplexityAnalysis());
    }

    // US 06
    private static void runSampleQueries(StationManager indexManager) { // refactor later on to possibly define the query parameters by user input
        System.out.println("=== SAMPLE QUERIES ===\n");

        // All stations in CET time zone, ordered by country
        System.out.println("Query 1: All stations in CET time zone (ordered by country)");
        System.out.println("-".repeat(60));
        List<Station> cetStations = indexManager.getStationsByTimeZoneGroup("CET");
        printQueryResults(cetStations, 10);

        //  All stations in a window of time zones [CET, WET/GMT]
        System.out.println("\nQuery 2: Stations in time zone window [CET, WET/GMT]");
        System.out.println("-".repeat(60));
        String[] timeZoneWindow = {"CET", "WET/GMT"};
        List<Station> windowStations = indexManager.getStationsByTimeZoneWindow(timeZoneWindow);
        printQueryResults(windowStations, 10);

        // Stations within latitude range (Portugal region: 37°-42°N)
        System.out.println("\nQuery 3: Stations in latitude range [37.0, 42.0] (Portugal region)");
        System.out.println("-".repeat(60));
        List<Station> latRangeStations = indexManager.getStationsByLatitudeRange(37.0, 42.0);
        printQueryResults(latRangeStations, 10);

        //  Stations within longitude range (Iberian Peninsula: -10° to 5°E)
        System.out.println("\nQuery 4: Stations in longitude range [-10.0, 5.0] (Iberian Peninsula)");
        System.out.println("-".repeat(60));
        List<Station> lonRangeStations = indexManager.getStationsByLongitudeRange(-10.0, 5.0);
        printQueryResults(lonRangeStations, 10);

        //  Stations in EET time zone (Eastern European Time)
        System.out.println("\nQuery 5: All stations in EET time zone");
        System.out.println("-".repeat(60));
        List<Station> eetStations = indexManager.getStationsByTimeZoneGroup("EET");
        printQueryResults(eetStations, 10);
    }

    private static void printQueryResults(List<Station> stations, int maxDisplay) {
        if (stations.isEmpty()) {
            System.out.println("No stations found.");
            return;
        }

        System.out.println("Total results: " + stations.size());
        System.out.println("Displaying first " + Math.min(maxDisplay, stations.size()) + " results:\n");

        int count = 0;
        for (Station station : stations) {
            if (count >= maxDisplay) break;
            System.out.printf("%3d. %-40s | Country: %-3s | TZ: %-15s | Lat: %8.4f | Lon: %8.4f | City: %-5s | Main: %-5s%n",
                    (count + 1),
                    truncate(station.getStation(), 40),
                    station.getCountry(),
                    station.getTimeZoneGroup(),
                    station.getLatitude(),
                    station.getLongitude(),
                    station.isCity(),
                    station.isMainStation());
            count++;
        }

        if (stations.size() > maxDisplay) {
            System.out.println("... and " + (stations.size() - maxDisplay) + " more results");
        }
    }

    private static String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    private static void runUS07Demonstration(StationManager indexManager) {
        System.out.println("=== US07 - 2D-TREE CONSTRUCTION ===\n");

        System.out.println("Building balanced 2D-Tree from AVL indices...");
        System.out.println("Strategy: Bulk build using pre-sorted latitude and longitude lists");
        System.out.println();

        indexManager.buildSpatialIndex();

        System.out.println();

        indexManager.printSpatialIndexStatistics();
        KDTree2Stats stats = indexManager.getSpatialIndexStatistics();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("-".repeat(60));
        System.out.println("✓ Tree size (unique coordinate points): " + stats.size);
        System.out.println("✓ Tree height: " + stats.height);

        System.out.println("\n✓ Distinct bucket sizes (stations per coordinate):");
        int totalPoints = 0;
        int totalStations = 0;
        for (var entry : stats.bucketDistribution.entrySet()) {
            totalPoints += entry.getValue();
            totalStations += entry.getKey() * entry.getValue();
        }
        System.out.println("  - Total coordinate points: " + totalPoints);
        System.out.println("  - Total stations: " + totalStations);
        System.out.println("  - Points with multiple stations: " +
                stats.bucketDistribution.entrySet().stream()
                        .filter(e -> e.getKey() > 1)
                        .mapToInt(e -> e.getValue())
                        .sum());

        System.out.println("\n✓ Coordinate ties handling:");
        System.out.println("  Stations sharing coordinates are grouped and sorted by name");
        System.out.println("  Example: Lisbon Santa Apolónia & Lisbon Oriente (38.71387, -9.122271)");
    }


    // US 09
    private static void runUS09ProximitySearch(StationManager indexManager) {
        System.out.println("=== US09 - PROXIMITY SEARCH WITH FILTERS ===\n");

        // query 1: 10 nearest stations without filter
        System.out.println("Query 1: 10 nearest stations to Lisbon (no filter)");
        System.out.println("-".repeat(60));
        System.out.println("Query point: Lisbon (38.7223°N, 9.1393°W)");
        List<KDTree2.StationDistance> nearest10 =
                indexManager.kNearestStations(38.7223, -9.1393, 10);
        System.out.println("Stations found: " + nearest10.size());
        printDistanceResults(nearest10, 10);

        // Query 2: 10 nearest stations in CET timezone
        System.out.println("\nQuery 2: 10 nearest CET stations to Lisbon");
        System.out.println("-".repeat(60));
        System.out.println("Query point: Lisbon (38.7223°N, 9.1393°W)");
        System.out.println("Filter: Timezone = CET");
        List<KDTree2.StationDistance> nearest10CET =
                indexManager.kNearestStationsWithTimezone(38.7223, -9.1393, 10, "CET");
        System.out.println("Stations found: " + nearest10CET.size());
        printDistanceResults(nearest10CET, 10);

        // Query 3: 10 nearest WET/GMT stations to Porto
        System.out.println("\nQuery 3: 10 nearest WET/GMT stations to Porto");
        System.out.println("-".repeat(60));
        System.out.println("Query point: Porto (41.1579°N, 8.6291°W)");
        System.out.println("Filter: Timezone = WET/GMT");
        List<KDTree2.StationDistance> nearest10WET =
                indexManager.kNearestStationsWithTimezone(41.1579, -8.6291, 10, "WET/GMT");
        System.out.println("Stations found: " + nearest10WET.size());
        printDistanceResults(nearest10WET, 10);

        // Query 4: 5 nearest main stations in CET to Brussels
        System.out.println("\nQuery 4: 5 nearest main stations in CET to Brussels");
        System.out.println("-".repeat(60));
        System.out.println("Query point: Brussels (50.8503°N, 4.3517°E)");
        KDTree2.StationFilterCriteria criteria1 = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET")
                .mainStationOnly(true);
        System.out.println("Filters: " + criteria1);
        List<KDTree2.StationDistance> mainStations =
                indexManager.kNearestStationsWithCriteria(50.8503, 4.3517, 5, criteria1);
        System.out.println("Stations found: " + mainStations.size());
        printDistanceResults(mainStations, 5);

        // Query 5: 10 nearest city stations to Paris
        System.out.println("\nQuery 5: 10 nearest city stations to Paris");
        System.out.println("-".repeat(60));
        System.out.println("Query point: Paris (48.8566°N, 2.3522°E)");
        KDTree2.StationFilterCriteria criteria2 = new KDTree2.StationFilterCriteria()
                .cityOnly(true);
        System.out.println("Filters: " + criteria2);
        List<KDTree2.StationDistance> cityStations =
                indexManager.kNearestStationsWithCriteria(48.8566, 2.3522, 10, criteria2);
        System.out.println("Stations found: " + cityStations.size());
        printDistanceResults(cityStations, 10);

        // Query 6: 5 nearest airports in Spain
        System.out.println("\nQuery 6: 5 nearest airport stations in Spain to Madrid");
        System.out.println("-".repeat(60));
        System.out.println("Query point: Madrid (40.4168°N, 3.7038°W)");
        KDTree2.StationFilterCriteria criteria3 = new KDTree2.StationFilterCriteria()
                .country("ES")
                .airportOnly(true);
        System.out.println("Filters: " + criteria3);
        List<KDTree2.StationDistance> airports =
                indexManager.kNearestStationsWithCriteria(40.4168, -3.7038, 5, criteria3);
        System.out.println("Stations found: " + airports.size());
        printDistanceResults(airports, 5);

        // Query 7: Compare filtered vs unfiltered
        System.out.println("\nQuery 7: Efficiency comparison - filtered vs unfiltered");
        System.out.println("-".repeat(60));
        System.out.println("Query point: Berlin (52.5200°N, 13.4050°E)");

        long start1 = System.nanoTime();
        List<KDTree2.StationDistance> unfiltered =
                indexManager.kNearestStations(52.5200, 13.4050, 20);
        long time1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        List<KDTree2.StationDistance> filtered =
                indexManager.kNearestStationsWithTimezone(52.5200, 13.4050, 20, "CET");
        long time2 = System.nanoTime() - start2;

        System.out.printf("Unfiltered (20 nearest): %d results in %.3f ms%n",
                unfiltered.size(), time1 / 1_000_000.0);
        System.out.printf("Filtered (20 CET only): %d results in %.3f ms%n",
                filtered.size(), time2 / 1_000_000.0);
        System.out.println("\nNote: Filtered search may visit more nodes to find k matches");
    }

    private static void printDistanceResults(List<KDTree2.StationDistance> results, int maxDisplay) {
        if (results.isEmpty()) {
            System.out.println("No stations found.");
            return;
        }

        System.out.println();
        int count = 0;
        for (KDTree2.StationDistance sd : results) {
            if (count >= maxDisplay) break;
            System.out.printf("%3d. %-45s | %-3s | %.2f km | %.4f, %.4f%n",
                    (count + 1),
                    truncate(sd.station.getStation(), 45),
                    sd.station.getCountry(),
                    sd.distanceKm,
                    sd.station.getLatitude(),
                    sd.station.getLongitude());
            count++;
        }

        if (results.size() > maxDisplay) {
            System.out.println("... and " + (results.size() - maxDisplay) + " more results");
        }
    }

    // US10
    private static void runUS10RadiusSearchWithSummary(StationManager indexManager) {
        System.out.println("=== US10 - RADIUS SEARCH WITH DENSITY SUMMARY ===\n");

        // Query 1: 100km radius around Lisbon
        System.out.println("Query 1: All stations within 100km of Lisbon");
        System.out.println("-".repeat(60));
        RadiusSearchResult result1 = indexManager.radiusSearchWithSummary(38.7223, -9.1393, 100.0);

        System.out.println(result1);
        System.out.println(result1.getStationDensitySummary());

        System.out.println("First 10 stations (sorted by distance ASC, name DESC):");
        printRadiusSearchResults(result1, 10);

        // Query 2: 50km radius around Porto
        System.out.println("\nQuery 2: All stations within 50km of Porto");
        System.out.println("-".repeat(60));
        RadiusSearchResult result2 = indexManager.radiusSearchWithSummary(41.1579, -8.6291, 50.0);

        System.out.println(result2);
        System.out.println(result2.getStationDensitySummary());

        System.out.println("First 10 stations:");
        printRadiusSearchResults(result2, 10);

        // Query 3: 200km radius around Paris
        System.out.println("\nQuery 3: All stations within 200km of Paris");
        System.out.println("-".repeat(60));
        RadiusSearchResult result3 = indexManager.radiusSearchWithSummary(48.8566, 2.3522, 200.0);

        System.out.println(result3);
        System.out.println(result3.getStationDensitySummary().toCompactString());

        System.out.println("\nTop 5 countries by station count:");
        List<Map.Entry<String, Integer>> topCountries = result3.getStationDensitySummary().getTopCountries(5);
        for (int i = 0; i < topCountries.size(); i++) {
            Map.Entry<String, Integer> entry = topCountries.get(i);
            System.out.printf("%d. %s: %d stations (%.1f%%)%n",
                    i + 1, entry.getKey(), entry.getValue(),
                    100.0 * entry.getValue() / result3.getTotalStations());
        }

        System.out.println("\nQuery 4: City stations only within 150km of Madrid");
        System.out.println("-".repeat(60));
        KDTree2.StationFilterCriteria cityCriteria = new KDTree2.StationFilterCriteria()
                .cityOnly(true);
        RadiusSearchResult result4 = indexManager.radiusSearchWithSummaryFiltered(
                40.4168, -3.7038, 150.0, cityCriteria);

        System.out.println(result4);
        System.out.println(result4.getStationDensitySummary().toCompactString());

        long start = System.nanoTime();
        RadiusSearchResult result5 = indexManager.radiusSearchWithSummary(52.5200, 13.4050, 300.0);
        long elapsed = System.nanoTime() - start;

        System.out.printf("Search completed in %.3f ms%n", elapsed / 1_000_000.0);
        System.out.printf("Stations found: %d%n", result5.getTotalStations());
        System.out.printf("Distance groups in AVL: %d%n", result5.getSortedByDistance().size());
        System.out.printf("AVL tree height: %d%n", result5.getSortedByDistance().height());
        System.out.println("\n" + result5.getStationDensitySummary().toCompactString());
    }

    private static void printRadiusSearchResults(RadiusSearchResult result, int maxDisplay) {
        List<DistanceKey> distanceKeys = (List<DistanceKey>) result.getSortedByDistance().inOrder();

        System.out.println();
        int count = 0;

        for (DistanceKey key : distanceKeys) {
            if (count >= maxDisplay) break;

            for (Station station : key.getStations()) {
                if (count >= maxDisplay) break;

                System.out.printf("%3d. %-45s | %-3s | %.2f km | City: %-5s%n",
                        (count + 1),
                        truncate(station.getStation(), 45),
                        station.getCountry(),
                        key.getDistanceKm(),
                        station.isCity());
                count++;
            }
        }

        if (result.getTotalStations() > maxDisplay) {
            System.out.println("... and " + (result.getTotalStations() - maxDisplay) + " more stations");
        }
    }
}
