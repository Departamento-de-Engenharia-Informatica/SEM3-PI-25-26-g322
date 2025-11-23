package isep.ipp.pt.g322.model;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StationDensitySummary {
        private final Map<String, Integer> countByCountry;
        private final int cityStations;
        private final int nonCityStations;
        private final int totalStations;
        private final double radiusKm;
        private final double centerLat;
        private final double centerLon;

        public StationDensitySummary(List<Station> stations, double radiusKm, double centerLat, double centerLon) {
            this.radiusKm = radiusKm;
            this.centerLat = centerLat;
            this.centerLon = centerLon;
            this.countByCountry = new TreeMap<>(); // treemap to sort "automatically" - in case by country code

            int cityCount = 0;
            int nonCityCount = 0;

            for (Station station : stations) {
                countByCountry.merge(station.getCountry(), 1, Integer::sum);

                if (station.isCity()) {
                    cityCount++;
                } else {
                    nonCityCount++;
                }
            }

            this.cityStations = cityCount;
            this.nonCityStations = nonCityCount;
            this.totalStations = stations.size();
        }

        public Map<String, Integer> getCountByCountry() {
            return new TreeMap<>(countByCountry);
        }

        public int getCityStations() {
            return cityStations;
        }

        public int getNonCityStations() {
            return nonCityStations;
        }

        public int getTotalStations() {
            return totalStations;
        }

        public double getRadiusKm() {
            return radiusKm;
        }

        public double getCenterLat() {
            return centerLat;
        }

        public double getCenterLon() {
            return centerLon;
        }

        public List<Map.Entry<String, Integer>> getTopCountries(int n) {
            return countByCountry.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(n)
                    .toList();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Density Summary ===\n");
            sb.append(String.format("Search radius: %.1f km around (%.4f, %.4f)\n",
                    radiusKm, centerLat, centerLon));
            sb.append(String.format("Total stations: %d\n", totalStations));
            sb.append(String.format("  City stations: %d (%.1f%%)\n",
                    cityStations, totalStations > 0 ? 100.0 * cityStations / totalStations : 0));
            sb.append(String.format("  Non-city stations: %d (%.1f%%)\n",
                    nonCityStations, totalStations > 0 ? 100.0 * nonCityStations / totalStations : 0));

            sb.append("\nStations by country:\n");
            for (Map.Entry<String, Integer> entry : countByCountry.entrySet()) {
                sb.append(String.format("  %s: %d (%.1f%%)\n",
                        entry.getKey(), entry.getValue(),
                        100.0 * entry.getValue() / totalStations));
            }

            return sb.toString();
        }

        public String toCompactString() {
            return String.format("Total: %d stations | City: %d | Non-city: %d | Countries: %d",
                    totalStations, cityStations, nonCityStations, countByCountry.size());
        }

}
