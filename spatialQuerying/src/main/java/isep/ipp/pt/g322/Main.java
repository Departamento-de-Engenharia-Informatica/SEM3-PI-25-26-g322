package isep.ipp.pt.g322;


import isep.ipp.pt.g322.model.Station;
import isep.ipp.pt.g322.model.StationManager;

import java.util.List;

public class Main {
    static void main() {
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

        runSampleQueries(indexManager);

        System.out.println("\n" + "=".repeat(60) + "\n");
        System.out.println(indexManager.getComplexityAnalysis());
    }

    private static void runSampleQueries(StationManager indexManager) { // refactor later on to possibly define the query parameters by user input
        System.out.println("=== SAMPLE QUERIES ===\n");

        // All stations in CET time zone, ordered by country
        System.out.println("Query 1: All stations in CET time zone (ordered by country)");
        System.out.println("-".repeat(60));
        List<Station> cetStations = indexManager.getStationsByTimeZoneGroup("CET");
        printQueryResults(cetStations, 10);

        System.out.println("Query 1: All stations in CET time zone (ordered by country)");
        System.out.println("-".repeat(60));
        List<Station> wetStations = indexManager.getStationsByTimeZoneGroup("WET/GMT");
        printQueryResults(wetStations, 10);

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
}
