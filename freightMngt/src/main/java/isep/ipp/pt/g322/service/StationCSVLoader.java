package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.RailConnection;
import isep.ipp.pt.g322.model.RailwayNetwork;
import isep.ipp.pt.g322.model.Station;

import java.io.*;

public class StationCSVLoader {
    private final RailwayNetwork network;

    public StationCSVLoader(RailwayNetwork network) {
        if (network == null) {
            throw new IllegalArgumentException("Network cannot be null");
        }
        this.network = network;
    }

    public int loadStationsFromCSV(String filename) throws IOException {
        int loadedCount = 0;
        int lineNumber = 0;

        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new IOException("File not found in resources: " + filename);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;

            line = br.readLine();
            lineNumber++;

            if (line == null) {
                System.err.println("Warning: Empty stations file");
                return 0;
            }

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                try {
                    Station station = parseStationFromCSV(line);
                    if (station != null && network.addStation(station)) {
                        loadedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Error parsing station at line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        System.out.println("Loaded " + loadedCount + " stations from " + filename);
        return loadedCount;
    }

    private Station parseStationFromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }

        String[] parts = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (parts.length < 6) {
            System.err.println("Warning: Insufficient columns in line: " + csvLine);
            return null;
        }

        try {
            String stationId = parts[0].trim().replace("\"", "");
            String name = parts[1].trim().replace("\"", "");

            if (stationId.isEmpty() || name.isEmpty()) {
                return null;
            }

            double latitude = Double.parseDouble(parts[2].trim());
            double longitude = Double.parseDouble(parts[3].trim());
            double x = Double.parseDouble(parts[4].trim());
            double y = Double.parseDouble(parts[5].trim());

            return new Station(stationId, name, latitude, longitude, x, y);

        } catch (NumberFormatException e) {
            System.err.println("Warning: Invalid numeric data in line: " + csvLine);
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Invalid station data: " + e.getMessage());
            return null;
        }
    }

    public int loadConnectionsFromCSV(String filename) throws IOException {
        int loadedCount = 0;
        int lineNumber = 0;

        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new IOException("File not found in resources: " + filename);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;

            line = br.readLine();
            lineNumber++;

            if (line == null) {
                System.err.println("Warning: Empty connections file");
                return 0;
            }

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                try {
                    if (parseAndAddConnectionFromCSV(line)) {
                        loadedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Error parsing connection at line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        System.out.println("Loaded " + loadedCount + " connections from " + filename);
        return loadedCount;
    }

    private boolean parseAndAddConnectionFromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return false;
        }

        String[] parts = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (parts.length < 5) {
            System.err.println("Warning: Insufficient columns in line: " + csvLine);
            return false;
        }

        try {
            String fromStationId = parts[0].trim().replace("\"", "");
            String toStationId = parts[1].trim().replace("\"", "");

            if (fromStationId.isEmpty() || toStationId.isEmpty()) {
                return false;
            }

            if (!network.hasStation(fromStationId) || !network.hasStation(toStationId)) {
                System.err.println("Warning: Station(s) not found for connection: " + fromStationId + " -> " + toStationId);
                return false;
            }

            double distance = Double.parseDouble(parts[2].trim());
            int capacity = Integer.parseInt(parts[3].trim());
            double cost = Double.parseDouble(parts[4].trim());

            RailConnection connection = new RailConnection(distance, capacity, cost);
            return network.addConnection(fromStationId, toStationId, connection);

        } catch (NumberFormatException e) {
            System.err.println("Warning: Invalid numeric data in line: " + csvLine);
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Invalid connection data: " + e.getMessage());
            return false;
        }
    }

}
