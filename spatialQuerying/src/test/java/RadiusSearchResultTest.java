
import isep.ipp.pt.g322.model.DistanceKey;
import isep.ipp.pt.g322.model.RadiusSearchResult;
import isep.ipp.pt.g322.model.Station;
import isep.ipp.pt.g322.model.StationDensitySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RadiusSearchResultTest {

    private List<Station> stations;
    private List<Double> distances;
    private Station station1;
    private Station station2;
    private Station station3;

    @BeforeEach
    void setUp() {
        stations = new ArrayList<>();
        distances = new ArrayList<>();

        station1 = new Station("Porto", 41.1579, -8.6291, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);
        station2 = new Station("Lisbon", 38.7223, -9.1393, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);
        station3 = new Station("Madrid", 40.4168, -3.7038, "ES",
                "Europe/Madrid", "Europe/Madrid", true, true, false);
    }

    @Test
    @DisplayName("Constructor should build AVL tree with stations")
    void testConstructor_BuildsAVL() {
        stations.add(station1);
        distances.add(10.5);

        RadiusSearchResult result = new RadiusSearchResult(stations, distances, 50.0, 41.0, -8.0);

        assertNotNull(result.getSortedByDistance());
        assertEquals(1, result.getSortedByDistance().size());
    }

    @Test
    @DisplayName("Constructor should round distances to 2 decimal places")
    void testConstructor_RoundsDistances() {
        stations.add(station1);
        distances.add(10.567);

        RadiusSearchResult result = new RadiusSearchResult(stations, distances, 50.0, 41.0, -8.0);

        List<DistanceKey> keys = (List<DistanceKey>) result.getSortedByDistance().inOrder();
        assertEquals(10.57, keys.get(0).getDistanceKm(), 0.001);
    }

    @Test
    @DisplayName("getAllStationsSorted should return all stations in order")
    void testGetAllStationsSorted() {
        stations.add(station1);
        stations.add(station2);
        distances.add(20.0);
        distances.add(10.0);

        RadiusSearchResult result = new RadiusSearchResult(stations, distances, 50.0, 41.0, -8.0);

        List<Station> sorted = result.getAllStationsSorted();
        assertEquals(2, sorted.size());
    }

    @Test
    @DisplayName("getAllStationsSorted should sort by distance then name descending")
    void testGetAllStationsSorted_Ordering() {
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);
        distances.add(10.0);
        distances.add(10.0);
        distances.add(20.0);

        RadiusSearchResult result = new RadiusSearchResult(stations, distances, 50.0, 41.0, -8.0);

        List<Station> sorted = result.getAllStationsSorted();
        assertEquals(3, sorted.size());

        assertTrue(sorted.get(0).getStation().equals("Porto") ||
                sorted.get(0).getStation().equals("Lisbon"));
        assertEquals("Madrid", sorted.get(2).getStation());
    }

    @Test
    @DisplayName("getAllStationsSorted should return empty list for empty result")
    void testGetAllStationsSorted_Empty() {
        RadiusSearchResult result = new RadiusSearchResult(
                new ArrayList<>(), new ArrayList<>(), 50.0, 41.0, -8.0);

        List<Station> sorted = result.getAllStationsSorted();
        assertTrue(sorted.isEmpty());
    }

    @Test
    @DisplayName("getTotalStations should return correct count")
    void testGetTotalStations() {
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);
        distances.add(10.0);
        distances.add(15.0);
        distances.add(20.0);

        RadiusSearchResult result = new RadiusSearchResult(stations, distances, 50.0, 41.0, -8.0);

        assertEquals(3, result.getTotalStations());
    }

    @Test
    @DisplayName("getTotalStations should return 0 for empty result")
    void testGetTotalStations_Empty() {
        RadiusSearchResult result = new RadiusSearchResult(
                new ArrayList<>(), new ArrayList<>(), 50.0, 41.0, -8.0);

        assertEquals(0, result.getTotalStations());
    }

    @Test
    @DisplayName("getSortedByDistance should return the AVL tree")
    void testGetSortedByDistance() {
        stations.add(station1);
        distances.add(10.0);

        RadiusSearchResult result = new RadiusSearchResult(stations, distances, 50.0, 41.0, -8.0);

        assertNotNull(result.getSortedByDistance());
        assertTrue(result.getSortedByDistance().size() > 0);
    }

    @Test
    @DisplayName("getStationDensitySummary should return summary with correct data")
    void testGetStationDensitySummary() {
        stations.add(station1);
        stations.add(station2);
        distances.add(10.0);
        distances.add(20.0);

        RadiusSearchResult result = new RadiusSearchResult(stations, distances, 50.0, 41.0, -8.0);

        StationDensitySummary summary = result.getStationDensitySummary();
        assertNotNull(summary);
        assertEquals(2, summary.getTotalStations());
        assertEquals(50.0, summary.getRadiusKm(), 0.001);
    }
}