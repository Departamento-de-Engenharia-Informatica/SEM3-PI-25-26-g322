import isep.ipp.pt.g322.model.Station;
import isep.ipp.pt.g322.model.StationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class StationManagerTest {
    private StationManager manager;

    private static final int EXPECTED_TOTAL_LINES = 28;
    private static final int EXPECTED_VALID_STATIONS = 25;
    private static final int EXPECTED_INVALID_STATIONS = 3;

    @BeforeEach
    void setUp() {
        manager = new StationManager();
    }

    @Test
    void testLoadStationsFromCSV_ValidFile() {
        int loaded = manager.loadStationsFromCSV("/test_stations.csv");

        assertEquals(EXPECTED_VALID_STATIONS, loaded, "Should load exactly 23 valid stations");
        assertEquals(loaded, manager.getValidStations());
        assertTrue(manager.getLatitudeIndexSize() > 0);
        assertTrue(manager.getLongitudeIndexSize() > 0);
        assertTrue(manager.getTimeZoneCountryIndexSize() > 0);
    }

    @Test
    void testGetTotalStations_AfterLoading() {
        manager.loadStationsFromCSV("/test_stations.csv");

        int total = manager.getTotalStations();
        int valid = manager.getValidStations();
        int invalid = manager.getInvalidStations();

        assertEquals(EXPECTED_TOTAL_LINES, total, "Should process 26 lines");
        assertEquals(EXPECTED_VALID_STATIONS, valid, "Should have 23 valid stations");
        assertEquals(EXPECTED_INVALID_STATIONS, invalid, "Should have 3 invalid stations");
        assertEquals(total, valid + invalid, "Total should equal valid + invalid");
    }

    @Test
    void testGetValidStations_InitiallyZero() {
        assertEquals(0, manager.getValidStations());
    }

    @Test
    void testGetInvalidStations_InitiallyZero() {
        assertEquals(0, manager.getInvalidStations());
    }

    @Test
    void testGetLatitudeIndexSize_AfterLoading() {
        manager.loadStationsFromCSV("/test_stations.csv");

        int size = manager.getLatitudeIndexSize();
        assertTrue(size > 0, "Latitude index should have entries");
        assertTrue(size <= manager.getValidStations(),
                "Unique latitudes should be <= total stations");
        assertEquals(25, size, "Should have 25 unique latitudes");
    }

    @Test
    void testGetLongitudeIndexSize_AfterLoading() {
        manager.loadStationsFromCSV("/test_stations.csv");

        int size = manager.getLongitudeIndexSize();
        assertTrue(size > 0, "Longitude index should have entries");
        assertTrue(size <= manager.getValidStations(),
                "Unique longitudes should be <= total stations");
        assertEquals(25, size, "Should have 25 unique longitudes");
    }

    @Test
    void testGetTimeZoneCountryIndexSize_AfterLoading() {
        manager.loadStationsFromCSV("/test_stations.csv");

        int size = manager.getTimeZoneCountryIndexSize();
        assertTrue(size > 0, "TimeZone-Country index should have entries");
        assertTrue(size <= manager.getValidStations(),
                "Unique combinations should be <= total stations");
        assertTrue(size >= 10, "Should have at least 10 unique timezone-country combinations");
    }

    @Test
    void testGetStationsByTimeZoneGroup_CET() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> cetStations = manager.getStationsByTimeZoneGroup("CET");

        assertNotNull(cetStations);
        assertFalse(cetStations.isEmpty(), "Should find CET stations");
        assertTrue(cetStations.size() >= 10, "Mock has at least 10 CET stations");

        for (Station station : cetStations) {
            assertEquals("CET", station.getTimeZoneGroup());
        }
    }

    @Test
    void testGetStationsByTimeZoneGroup_WET() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> wetStations = manager.getStationsByTimeZoneGroup("WET");

        assertNotNull(wetStations);
        assertFalse(wetStations.isEmpty(), "Should find WET stations");
        assertTrue(wetStations.size() >= 4, "Mock has at least 4 WET stations");

        for (Station station : wetStations) {
            assertEquals("WET", station.getTimeZoneGroup());
        }
    }

    @Test
    void testGetStationsByTimeZoneGroup_NonExistent() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByTimeZoneGroup("NONEXISTENT");

        assertNotNull(stations);
        assertTrue(stations.isEmpty(), "Should return empty list for non-existent timezone");
    }

    @Test
    void testGetStationsByTimeZoneGroup_EmptyManager() {
        List<Station> stations = manager.getStationsByTimeZoneGroup("CET");

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetStationsByTimeZoneGroup_StationsAreSorted() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByTimeZoneGroup("CET");

        if (stations.size() > 1) {
            for (int i = 0; i < stations.size() - 1; i++) {
                assertTrue(stations.get(i).getStation().compareTo(stations.get(i + 1).getStation()) <= 0,
                        "Stations should be sorted by name");
            }
        }
    }

    // Query Tests - getStationsByTimeZoneWindow
    @Test
    void testGetStationsByTimeZoneWindow_MultipleZones() {
        manager.loadStationsFromCSV("/test_stations.csv");

        String[] timeZones = {"CET", "WET/GMT"};
        List<Station> stations = manager.getStationsByTimeZoneWindow(timeZones);

        assertNotNull(stations);
        assertFalse(stations.isEmpty());
        assertTrue(stations.size() >= 14, "Should have at least 14 stations in CET and WET/GMT combined");

        // Verify all stations are in one of the specified timezones
        for (Station station : stations) {
            assertTrue(station.getTimeZoneGroup().equals("CET") ||
                    station.getTimeZoneGroup().equals("WET/GMT"));
        }
    }

    @Test
    void testGetStationsByTimeZoneWindow_SingleZone() {
        manager.loadStationsFromCSV("/test_stations.csv");

        String[] timeZones = {"CET"};
        List<Station> windowStations = manager.getStationsByTimeZoneWindow(timeZones);
        List<Station> singleStations = manager.getStationsByTimeZoneGroup("CET");

        assertEquals(singleStations.size(), windowStations.size());
    }

    @Test
    void testGetStationsByTimeZoneWindow_EmptyArray() {
        manager.loadStationsFromCSV("/test_stations.csv");

        String[] timeZones = {};
        List<Station> stations = manager.getStationsByTimeZoneWindow(timeZones);

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetStationsByTimeZoneWindow_NonExistentZones() {
        manager.loadStationsFromCSV("/test_stations.csv");

        String[] timeZones = {"FAKE1", "FAKE2"};
        List<Station> stations = manager.getStationsByTimeZoneWindow(timeZones);

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetStationsByLatitudeRange_PortugalRegion() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByLatitudeRange(37.0, 42.0);

        assertNotNull(stations);
        assertFalse(stations.isEmpty(), "Should find stations in Portugal region");
        assertTrue(stations.size() >= 6, "Should have at least 6 stations in this range");

        for (Station station : stations) {
            assertTrue(station.getLatitude() >= 37.0,
                    "Latitude should be >= 37.0, but was " + station.getLatitude());
            assertTrue(station.getLatitude() <= 42.0,
                    "Latitude should be <= 42.0, but was " + station.getLatitude());
        }
    }

    @Test
    void testGetStationsByLatitudeRange_NarrowRange() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByLatitudeRange(43.0, 44.5);

        assertNotNull(stations);
        assertTrue(stations.size() >= 2, "Should find at least 2 Paris stations");

        for (Station station : stations) {
            assertTrue(station.getLatitude() >= 43.0);
            assertTrue(station.getLatitude() <= 44.5);
        }
    }

    @Test
    void testGetStationsByLatitudeRange_InvalidRange() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByLatitudeRange(50.0, 40.0);

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetStationsByLatitudeRange_OutOfBounds() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByLatitudeRange(-80.0, -70.0);

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetStationsByLatitudeRange_EmptyManager() {
        List<Station> stations = manager.getStationsByLatitudeRange(37.0, 42.0);

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetStationsByLongitudeRange_IberianPeninsula() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByLongitudeRange(-10.0, 5.0);

        assertNotNull(stations);
        assertFalse(stations.isEmpty(), "Should find stations in Iberian Peninsula");
        assertTrue(stations.size() >= 6, "Should have at least 6 stations in this range");

        for (Station station : stations) {
            assertTrue(station.getLongitude() >= -10.0,
                    "Longitude should be >= -10.0, but was " + station.getLongitude());
            assertTrue(station.getLongitude() <= 5.0,
                    "Longitude should be <= 5.0, but was " + station.getLongitude());
        }
    }

    @Test
    void testGetStationsByLongitudeRange_CentralEurope() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByLongitudeRange(8.0, 17.0);

        assertNotNull(stations);
        assertTrue(stations.size() >= 5, "Should have at least 5 stations in Central Europe");

        for (Station station : stations) {
            assertTrue(station.getLongitude() >= 8.0);
            assertTrue(station.getLongitude() <= 17.0);
        }
    }

    @Test
    void testGetStationsByLongitudeRange_InvalidRange() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByLongitudeRange(10.0, -10.0);

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetStationsByLongitudeRange_OutOfBounds() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<Station> stations = manager.getStationsByLongitudeRange(100.0, 120.0);

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetStationsByLongitudeRange_EmptyManager() {
        List<Station> stations = manager.getStationsByLongitudeRange(-10.0, 5.0);

        assertNotNull(stations);
        assertTrue(stations.isEmpty());
    }

    @Test
    void testGetValidationErrors_InitiallyEmpty() {
        assertTrue(manager.getValidationErrors().isEmpty());
    }

    @Test
    void testGetValidationErrors_AfterLoading() {
        manager.loadStationsFromCSV("/test_stations.csv");

        List<String> errors = manager.getValidationErrors();
        assertNotNull(errors);
        assertEquals(EXPECTED_INVALID_STATIONS, errors.size(),
                "Should have 3 validation errors from mock data");
    }

    @Test
    void testPrintStatistics_DoesNotThrowException() {
        manager.loadStationsFromCSV("/test_stations.csv");

        assertDoesNotThrow(() -> manager.printStatistics());
    }

    @Test
    void testPrintStatistics_EmptyManager() {
        assertDoesNotThrow(() -> manager.printStatistics());
    }

    @Test
    void testGetComplexityAnalysis_ReturnsNonEmptyString() {
        String analysis = manager.getComplexityAnalysis();

        assertNotNull(analysis);
        assertFalse(analysis.isEmpty());
        assertTrue(analysis.contains("O(n"));
        assertTrue(analysis.contains("AVL"));
    }

    @Test
    void testGetComplexityAnalysis_ContainsExpectedSections() {
        String analysis = manager.getComplexityAnalysis();

        assertTrue(analysis.contains("Loading stations"));
        assertTrue(analysis.contains("Query by Time Zone Group"));
        assertTrue(analysis.contains("Query by Time Zone Window"));
        assertTrue(analysis.contains("Range queries"));
    }
}