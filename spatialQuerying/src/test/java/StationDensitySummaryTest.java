
import isep.ipp.pt.g322.model.Station;
import isep.ipp.pt.g322.model.StationDensitySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StationDensitySummaryTest {

    private List<Station> testStations;
    private Station stationPT1;
    private Station stationPT2;
    private Station stationES1;
    private Station stationFR1;
    private Station stationFR2;

    @BeforeEach
    void setUp() {
        testStations = new ArrayList<>();
        stationPT1 = new Station("Porto", 41.1579, -8.6291, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);
        stationPT2 = new Station("Lisbon", 38.7223, -9.1393, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);
        stationES1 = new Station("Madrid", 40.4168, -3.7038, "ES",
                "Europe/Madrid", "Europe/Madrid", true, true, false);
        stationFR1 = new Station("Paris", 48.8566, 2.3522, "FR",
                "Europe/Paris", "Europe/Paris", true, true, false);
        stationFR2 = new Station("Lyon Station", 45.7640, 4.8357, "FR",
                "Europe/Paris", "Europe/Paris", false, false, false);
    }

    @Test
    @DisplayName("getCountByCountry should return a copy (defensive copy)")
    void testGetCountByCountry_DefensiveCopy() {
        testStations.add(stationPT1);
        testStations.add(stationES1);

        StationDensitySummary summary = new StationDensitySummary(testStations, 100.0, 40.0, -8.0);

        Map<String, Integer> countByCountry1 = summary.getCountByCountry();
        Map<String, Integer> countByCountry2 = summary.getCountByCountry();

        assertNotSame(countByCountry1, countByCountry2);
        assertEquals(countByCountry1, countByCountry2);
    }

    @Test
    @DisplayName("getCountByCountry should return sorted map (TreeMap)")
    void testGetCountByCountry_Sorted() {
        testStations.add(stationPT1);
        testStations.add(stationES1);
        testStations.add(stationFR1);

        StationDensitySummary summary = new StationDensitySummary(testStations, 100.0, 40.0, -8.0);

        List<String> keys = new ArrayList<>(summary.getCountByCountry().keySet());
        assertEquals("ES", keys.get(0));
        assertEquals("FR", keys.get(1));
        assertEquals("PT", keys.get(2));
    }

    @Test
    @DisplayName("getTopCountries should return top N countries by count")
    void testGetTopCountries() {
        testStations.add(stationPT1);
        testStations.add(stationPT2);
        testStations.add(stationES1);
        testStations.add(stationFR1);
        testStations.add(stationFR2);

        StationDensitySummary summary = new StationDensitySummary(testStations, 100.0, 40.0, -8.0);

        List<Map.Entry<String, Integer>> topCountries = summary.getTopCountries(2);

        assertEquals(2, topCountries.size());
        assertTrue(topCountries.stream().anyMatch(e -> e.getKey().equals("PT") && e.getValue() == 2));
        assertTrue(topCountries.stream().anyMatch(e -> e.getKey().equals("FR") && e.getValue() == 2));
    }

    @Test
    @DisplayName("getTopCountries should handle N greater than number of countries")
    void testGetTopCountries_NGreaterThanSize() {
        testStations.add(stationPT1);
        testStations.add(stationES1);

        StationDensitySummary summary = new StationDensitySummary(testStations, 100.0, 40.0, -8.0);

        List<Map.Entry<String, Integer>> topCountries = summary.getTopCountries(10);

        assertEquals(2, topCountries.size());
    }

    @Test
    @DisplayName("getTopCountries should return empty list when N is 0")
    void testGetTopCountries_Zero() {
        testStations.add(stationPT1);

        StationDensitySummary summary = new StationDensitySummary(testStations, 100.0, 40.0, -8.0);

        List<Map.Entry<String, Integer>> topCountries = summary.getTopCountries(0);

        assertTrue(topCountries.isEmpty());
    }

    @Test
    @DisplayName("City and non-city counts should sum to total")
    void testCityCounts_SumToTotal() {
        testStations.add(stationPT1);
        testStations.add(stationPT2);
        testStations.add(stationES1);
        testStations.add(stationFR2);

        StationDensitySummary summary = new StationDensitySummary(testStations, 100.0, 40.0, -8.0);

        assertEquals(summary.getTotalStations(),
                summary.getCityStations() + summary.getNonCityStations());
    }
}