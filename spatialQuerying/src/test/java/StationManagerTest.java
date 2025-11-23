import isep.ipp.pt.g322.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;

import isep.ipp.pt.g322.datastructures.tree.KDTree2;


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
        assertEquals(24, size, "Should have 24 unique latitudes");
    }

    @Test
    void testGetLongitudeIndexSize_AfterLoading() {
        manager.loadStationsFromCSV("/test_stations.csv");

        int size = manager.getLongitudeIndexSize();
        assertTrue(size > 0, "Longitude index should have entries");
        assertTrue(size <= manager.getValidStations(),
                "Unique longitudes should be <= total stations");
        assertEquals(24, size, "Should have 24 unique longitudes");
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

    @Test
    void testGetStationsByTimeZoneWindow_MultipleZones() {
        manager.loadStationsFromCSV("/test_stations.csv");

        String[] timeZones = {"CET", "WET/GMT"};
        List<Station> stations = manager.getStationsByTimeZoneWindow(timeZones);

        assertNotNull(stations);
        assertFalse(stations.isEmpty());
        assertTrue(stations.size() >= 14, "Should have at least 14 stations in CET and WET/GMT combined");

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

    @Test
    void testBuildSpatialIndex_Success() {
        manager.loadStationsFromCSV("/test_stations.csv");

        assertDoesNotThrow(() -> manager.buildSpatialIndex(),
                "Building spatial index should not throw exception");
    }

    @Test
    void testBuildSpatialIndex_ThrowsExceptionWhenIndicesEmpty() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.buildSpatialIndex(),
                "Should throw exception when AVL indices are not populated");

        assertTrue(exception.getMessage().contains("AVL indices must be populated"),
                "Exception message should mention AVL indices");
    }

    @Test
    void testBuildSpatialIndex_CanBeCalledMultipleTimes() {
        manager.loadStationsFromCSV("/test_stations.csv");

        assertDoesNotThrow(() -> {
            manager.buildSpatialIndex();
            manager.buildSpatialIndex();
        }, "Should be able to rebuild spatial index");
    }

    @Test
    void testGetSpatialIndexStatistics_Success() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2Stats stats = manager.getSpatialIndexStatistics();

        assertNotNull(stats, "Statistics should not be null");
        assertTrue(stats.size > 0, "Tree size should be greater than 0");
        assertTrue(stats.height > 0, "Tree height should be greater than 0");
        assertNotNull(stats.bucketDistribution, "Bucket distribution should not be null");
        assertFalse(stats.bucketDistribution.isEmpty(), "Bucket distribution should not be empty");
    }

    @Test
    void testGetSpatialIndexStatistics_ThrowsExceptionWhenNotBuilt() {
        manager.loadStationsFromCSV("/test_stations.csv");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.getSpatialIndexStatistics(),
                "Should throw exception when spatial index not built");

        assertTrue(exception.getMessage().contains("not built yet"),
                "Exception message should mention index not built");
    }

    @Test
    void testGetSpatialIndexStatistics_ValidatesTreeStructure() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2Stats stats = manager.getSpatialIndexStatistics();

        int expectedMaxHeight = (int) Math.ceil(Math.log(stats.size) / Math.log(2)) * 2;
        assertTrue(stats.height <= expectedMaxHeight,
                "Tree height should be reasonably balanced: height=" + stats.height +
                        ", size=" + stats.size + ", expected max=" + expectedMaxHeight);
    }

    @Test
    void testGetSpatialIndexStatistics_BucketDistributionSumsToTreeSize() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2Stats stats = manager.getSpatialIndexStatistics();
        int totalPoints = stats.bucketDistribution.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(stats.size, totalPoints,
                "Sum of bucket distribution should equal tree size");
    }

    @Test
    void testPrintSpatialIndexStatistics_DoesNotThrowException() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        assertDoesNotThrow(() -> manager.printSpatialIndexStatistics(),
                "Printing statistics should not throw exception");
    }

    @Test
    void testPrintSpatialIndexStatistics_BeforeBuilding() {
        assertDoesNotThrow(() -> manager.printSpatialIndexStatistics(),
                "Should handle case when index not built yet");
    }

    @Test
    void testKNearestStations_FindsCorrectNumber() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double lat = 41.1579;
        double lon = -8.6291;
        int k = 5;

        List<KDTree2.StationDistance> results = manager.kNearestStations(lat, lon, k);

        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= k, "Should return at most k stations");
        assertTrue(results.size() > 0, "Should find at least one station");
    }

    @Test
    void testKNearestStations_ResultsAreSortedByDistance() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double lat = 48.8566;
        double lon = 2.3522;
        int k = 10;

        List<KDTree2.StationDistance> results = manager.kNearestStations(lat, lon, k);

        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).distanceKm <= results.get(i + 1).distanceKm,
                    "Results should be sorted by distance (ascending)");
        }
    }

    @Test
    void testKNearestStations_DistancesArePositive() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double lat = 40.4168; // Madrid
        double lon = -3.7038;
        int k = 5;

        List<KDTree2.StationDistance> results = manager.kNearestStations(lat, lon, k);

        for (KDTree2.StationDistance sd : results) {
            assertTrue(sd.distanceKm >= 0, "Distance should be non-negative");
        }
    }

    @Test
    void testKNearestStations_WithZeroK() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        List<KDTree2.StationDistance> results = manager.kNearestStations(41.0, -8.0, 0);

        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Should return empty list for k=0");
    }

    @Test
    void testKNearestStations_WithNegativeK() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        List<KDTree2.StationDistance> results = manager.kNearestStations(41.0, -8.0, -5);

        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Should return empty list for negative k");
    }

    @Test
    void testKNearestStations_ThrowsExceptionWhenNotBuilt() {
        manager.loadStationsFromCSV("/test_stations.csv");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.kNearestStations(41.0, -8.0, 5),
                "Should throw exception when spatial index not built");

        assertTrue(exception.getMessage().contains("not built"),
                "Exception message should mention index not built");
    }

    @Test
    void testKNearestStations_LargeK() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        int k = 1000; // more than total stations
        List<KDTree2.StationDistance> results = manager.kNearestStations(41.0, -8.0, k);

        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= manager.getValidStations(),
                "Should not return more stations than available");
    }

    @Test
    void testKNearestStationsWithTimezone_FiltersCorrectly() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double lat = 48.8566; // Paris
        double lon = 2.3522;
        int k = 10;
        String timezone = "CET";

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithTimezone(lat, lon, k, timezone);

        assertNotNull(results, "Results should not be null");
        for (KDTree2.StationDistance sd : results) {
            assertEquals(timezone, sd.station.getTimeZoneGroup(),
                    "All results should match the timezone filter");
        }
    }

    @Test
    void testKNearestStationsWithTimezone_NullFilterReturnsAllStations() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double lat = 41.0;
        double lon = -8.0;
        int k = 5;

        List<KDTree2.StationDistance> withoutFilter = manager.kNearestStations(lat, lon, k);
        List<KDTree2.StationDistance> withNullFilter = manager.kNearestStationsWithTimezone(lat, lon, k, null);

        assertEquals(withoutFilter.size(), withNullFilter.size(),
                "Null filter should behave like no filter");
    }

    @Test
    void testKNearestStationsWithTimezone_ResultsAreSorted() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithTimezone(
                48.8566, 2.3522, 10, "CET");

        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).distanceKm <= results.get(i + 1).distanceKm,
                    "Results should be sorted by distance");
        }
    }

    @Test
    void testKNearestStationsWithTimezone_NonExistentTimezone() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithTimezone(
                41.0, -8.0, 10, "NONEXISTENT_TZ");

        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.size() < 10,
                "Should find fewer or no stations with non-existent timezone");
    }

    @Test
    void testKNearestStationsWithTimezone_MayReturnFewerThanK() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        int k = 100;
        List<KDTree2.StationDistance> results = manager.kNearestStationsWithTimezone(
                41.0, -8.0, k, "WET");

        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= k, "Should not exceed k stations");
    }

    @Test
    void testKNearestStationsWithTimezone_ThrowsExceptionWhenNotBuilt() {
        manager.loadStationsFromCSV("/test_stations.csv");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.kNearestStationsWithTimezone(41.0, -8.0, 5, "CET"),
                "Should throw exception when spatial index not built");

        assertTrue(exception.getMessage().contains("not built"),
                "Exception message should mention index not built");
    }

    @Test
    void testKNearestStationsWithCriteria_SingleCriterion() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET");

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithCriteria(
                48.8566, 2.3522, 10, criteria);

        assertNotNull(results, "Results should not be null");
        for (KDTree2.StationDistance sd : results) {
            assertEquals("CET", sd.station.getTimeZoneGroup(),
                    "All results should match timezone criterion");
        }
    }

    @Test
    void testKNearestStationsWithCriteria_MultipleCriteria() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET")
                .mainStationOnly(true);

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithCriteria(
                48.8566, 2.3522, 10, criteria);

        assertNotNull(results, "Results should not be null");
        for (KDTree2.StationDistance sd : results) {
            assertEquals("CET", sd.station.getTimeZoneGroup(),
                    "All results should match timezone criterion");
            assertTrue(sd.station.isMainStation(),
                    "All results should be main stations");
        }
    }

    @Test
    void testKNearestStationsWithCriteria_CityOnly() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .cityOnly(true);

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithCriteria(
                41.0, -8.0, 10, criteria);

        assertNotNull(results, "Results should not be null");
        for (KDTree2.StationDistance sd : results) {
            assertTrue(sd.station.isCity(), "All results should be cities");
        }
    }

    @Test
    void testKNearestStationsWithCriteria_AirportOnly() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .airportOnly(true);

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithCriteria(
                41.0, -8.0, 10, criteria);

        assertNotNull(results, "Results should not be null");
        for (KDTree2.StationDistance sd : results) {
            assertTrue(sd.station.isAirport(), "All results should be airports");
        }
    }

    @Test
    void testKNearestStationsWithCriteria_CountryFilter() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .country("PT");

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithCriteria(
                41.0, -8.0, 10, criteria);

        assertNotNull(results, "Results should not be null");
        for (KDTree2.StationDistance sd : results) {
            assertEquals("PT", sd.station.getCountry(),
                    "All results should be from Portugal");
        }
    }

    @Test
    void testKNearestStationsWithCriteria_NullCriteriaReturnsAll() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double lat = 41.0;
        double lon = -8.0;
        int k = 5;

        List<KDTree2.StationDistance> withoutFilter = manager.kNearestStations(lat, lon, k);
        List<KDTree2.StationDistance> withNullCriteria = manager.kNearestStationsWithCriteria(
                lat, lon, k, null);

        assertEquals(withoutFilter.size(), withNullCriteria.size(),
                "Null criteria should behave like no filter");
    }

    @Test
    void testKNearestStationsWithCriteria_ResultsAreSorted() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET");

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithCriteria(
                48.8566, 2.3522, 10, criteria);

        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).distanceKm <= results.get(i + 1).distanceKm,
                    "Results should be sorted by distance");
        }
    }

    @Test
    void testKNearestStationsWithCriteria_RestrictiveCriteriaMayReturnFewer() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET")
                .country("PT")
                .mainStationOnly(true)
                .airportOnly(true);

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithCriteria(
                41.0, -8.0, 10, criteria);

        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= 10, "Should not exceed requested k");
    }

    @Test
    void testKNearestStationsWithCriteria_ComplexCombination() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET")
                .cityOnly(true)
                .mainStationOnly(false);

        List<KDTree2.StationDistance> results = manager.kNearestStationsWithCriteria(
                48.8566, 2.3522, 10, criteria);

        assertNotNull(results, "Results should not be null");
        for (KDTree2.StationDistance sd : results) {
            assertEquals("CET", sd.station.getTimeZoneGroup());
            assertTrue(sd.station.isCity());
            assertFalse(sd.station.isMainStation());
        }
    }

    @Test
    void testKNearestStationsWithCriteria_ThrowsExceptionWhenNotBuilt() {
        manager.loadStationsFromCSV("/test_stations.csv");

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.kNearestStationsWithCriteria(41.0, -8.0, 5, criteria),
                "Should throw exception when spatial index not built");

        assertTrue(exception.getMessage().contains("not built"),
                "Exception message should mention index not built");
    }

    @Test
    void testKNearestStationsWithCriteria_EmptyCriteriaEqualsNoCriteria() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double lat = 41.0;
        double lon = -8.0;
        int k = 5;

        KDTree2.StationFilterCriteria emptyCriteria = new KDTree2.StationFilterCriteria();

        List<KDTree2.StationDistance> withEmpty = manager.kNearestStationsWithCriteria(
                lat, lon, k, emptyCriteria);
        List<KDTree2.StationDistance> withoutFilter = manager.kNearestStations(lat, lon, k);

        assertEquals(withoutFilter.size(), withEmpty.size(),
                "Empty criteria should return same as no filter");
    }

    @Test
    void testKNearestStations_ExactCoordinateMatch() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        List<Station> allStations = manager.getStationsByLatitudeRange(-90, 90);
        if (!allStations.isEmpty()) {
            Station firstStation = allStations.get(0);
            List<KDTree2.StationDistance> results = manager.kNearestStations(
                    firstStation.getLatitude(), firstStation.getLongitude(), 1);

            assertFalse(results.isEmpty(), "Should find at least one station");
            assertTrue(results.get(0).distanceKm < 1.0,
                    "Distance to exact coordinate should be very small");
        }
    }

    @Test
    void testKNearestStations_RemoteLocation() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double lat = 0.0;
        double lon = 180.0;
        int k = 3;

        List<KDTree2.StationDistance> results = manager.kNearestStations(lat, lon, k);

        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Should find stations even from remote location");
        for (KDTree2.StationDistance sd : results) {
            assertTrue(sd.distanceKm > 1000.0,
                    "Distance from Pacific to Europe should be > 1000 km");
        }
    }

    @Test
    void testComplexityAnalysis_IncludesNewMethods() {
        String analysis = manager.getComplexityAnalysis();

        assertTrue(analysis.contains("US07"), "Should mention US07");
        assertTrue(analysis.contains("US08"), "Should mention US08");
        assertTrue(analysis.contains("US09"), "Should mention US09");
        assertTrue(analysis.contains("2D-Tree"), "Should mention 2D-Tree");
        assertTrue(analysis.contains("K-nearest"), "Should mention K-nearest");
        assertTrue(analysis.contains("filter"), "Should mention filtering");
    }

    @Test
    void testRadiusSearchWithSummary_FindsStationsWithinRadius() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double centerLat = 41.1579;
        double centerLon = -8.6291;
        double radiusKm = 100.0;

        RadiusSearchResult result = manager.radiusSearchWithSummary(centerLat, centerLon, radiusKm);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.getTotalStations() > 0, "Should find stations within radius");
        assertEquals(radiusKm, result.getRadiusKm(), 0.001);
        assertEquals(centerLat, result.getCenterLat(), 0.001);
        assertEquals(centerLon, result.getCenterLon(), 0.001);
    }

    @Test
    void testRadiusSearchWithSummary_AllStationsWithinRadius() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double centerLat = 48.8566;
        double centerLon = 2.3522;
        double radiusKm = 50.0;

        RadiusSearchResult result = manager.radiusSearchWithSummary(centerLat, centerLon, radiusKm);

        List<Station> allStations = result.getAllStationsSorted();
        for (Station station : allStations) {
            double distance = calculateDistance(centerLat, centerLon,
                    station.getLatitude(), station.getLongitude());
            assertTrue(distance <= radiusKm + 1.0,
                    "Station " + station.getStation() + " should be within radius");
        }
    }

    @Test
    void testRadiusSearchWithSummary_SmallRadiusFindsFewerStations() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double centerLat = 41.0;
        double centerLon = -8.0;

        RadiusSearchResult smallRadius = manager.radiusSearchWithSummary(centerLat, centerLon, 50.0);
        RadiusSearchResult largeRadius = manager.radiusSearchWithSummary(centerLat, centerLon, 500.0);

        assertTrue(smallRadius.getTotalStations() <= largeRadius.getTotalStations(),
                "Smaller radius should find same or fewer stations");
    }

    @Test
    void testRadiusSearchWithSummary_ZeroRadiusFindsNearestOrNone() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        RadiusSearchResult result = manager.radiusSearchWithSummary(41.0, -8.0, 0.0);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.getTotalStations() >= 0, "Should handle zero radius");
    }

    @Test
    void testRadiusSearchWithSummary_VeryLargeRadiusFindsAllStations() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        RadiusSearchResult result = manager.radiusSearchWithSummary(45.0, 5.0, 10000.0);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.getTotalStations() > 0, "Should find stations with very large radius");
    }

    @Test
    void testRadiusSearchWithSummary_ThrowsExceptionWhenNotBuilt() {
        manager.loadStationsFromCSV("/test_stations.csv");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.radiusSearchWithSummary(41.0, -8.0, 100.0),
                "Should throw exception when spatial index not built");

        assertTrue(exception.getMessage().contains("not built"),
                "Exception message should mention index not built");
    }

    @Test
    void testRadiusSearchWithSummary_HasValidSummary() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        RadiusSearchResult result = manager.radiusSearchWithSummary(41.0, -8.0, 200.0);

        assertNotNull(result.getStationDensitySummary(), "Summary should not be null");
        assertEquals(result.getTotalStations(),
                result.getStationDensitySummary().getTotalStations(),
                "Summary should have same total as result");
    }

    @Test
    void testRadiusSearchWithSummary_HasValidAVLTree() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        RadiusSearchResult result = manager.radiusSearchWithSummary(48.8566, 2.3522, 700.0);

        assertNotNull(result.getSortedByDistance(), "AVL tree should not be null");
        assertTrue(result.getSortedByDistance().size() > 0, "AVL tree should have entries");
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_FiltersByTimezone() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET");

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                48.8566, 2.3522, 200.0, criteria);

        assertNotNull(result, "Result should not be null");
        for (Station station : result.getAllStationsSorted()) {
            assertEquals("CET", station.getTimeZoneGroup(),
                    "All stations should match timezone filter");
        }
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_FiltersByCountry() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .country("PT");

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                41.0, -8.0, 300.0, criteria);

        assertNotNull(result, "Result should not be null");
        for (Station station : result.getAllStationsSorted()) {
            assertEquals("PT", station.getCountry(),
                    "All stations should be from Portugal");
        }
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_FiltersByCityOnly() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .cityOnly(true);

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                41.0, -8.0, 200.0, criteria);

        assertNotNull(result, "Result should not be null");
        for (Station station : result.getAllStationsSorted()) {
            assertTrue(station.isCity(), "All stations should be cities");
        }
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_FiltersByMainStationOnly() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .mainStationOnly(true);

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                48.8566, 2.3522, 150.0, criteria);

        assertNotNull(result, "Result should not be null");
        for (Station station : result.getAllStationsSorted()) {
            assertTrue(station.isMainStation(), "All stations should be main stations");
        }
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_FiltersByAirportOnly() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .airportOnly(true);

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                41.0, -8.0, 300.0, criteria);

        assertNotNull(result, "Result should not be null");
        for (Station station : result.getAllStationsSorted()) {
            assertTrue(station.isAirport(), "All stations should be airports");
        }
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_MultipleCriteria() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET")
                .cityOnly(true)
                .mainStationOnly(true);

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                48.8566, 2.3522, 200.0, criteria);

        assertNotNull(result, "Result should not be null");
        for (Station station : result.getAllStationsSorted()) {
            assertEquals("CET", station.getTimeZoneGroup());
            assertTrue(station.isCity());
            assertTrue(station.isMainStation());
        }
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_NullCriteriaReturnsAll() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double centerLat = 41.0;
        double centerLon = -8.0;
        double radiusKm = 200.0;

        RadiusSearchResult withoutFilter = manager.radiusSearchWithSummary(
                centerLat, centerLon, radiusKm);
        RadiusSearchResult withNullFilter = manager.radiusSearchWithSummaryFiltered(
                centerLat, centerLon, radiusKm, null);

        assertEquals(withoutFilter.getTotalStations(), withNullFilter.getTotalStations(),
                "Null criteria should return same as no filter");
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_EmptyCriteriaReturnsAll() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double centerLat = 41.0;
        double centerLon = -8.0;
        double radiusKm = 200.0;

        KDTree2.StationFilterCriteria emptyCriteria = new KDTree2.StationFilterCriteria();

        RadiusSearchResult withoutFilter = manager.radiusSearchWithSummary(
                centerLat, centerLon, radiusKm);
        RadiusSearchResult withEmptyFilter = manager.radiusSearchWithSummaryFiltered(
                centerLat, centerLon, radiusKm, emptyCriteria);

        assertEquals(withoutFilter.getTotalStations(), withEmptyFilter.getTotalStations(),
                "Empty criteria should return same as no filter");
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_RestrictiveCriteriaFindsFewerStations() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double centerLat = 48.8566;
        double centerLon = 2.3522;
        double radiusKm = 200.0;

        RadiusSearchResult unfiltered = manager.radiusSearchWithSummary(
                centerLat, centerLon, radiusKm);

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET")
                .mainStationOnly(true);

        RadiusSearchResult filtered = manager.radiusSearchWithSummaryFiltered(
                centerLat, centerLon, radiusKm, criteria);

        assertTrue(filtered.getTotalStations() <= unfiltered.getTotalStations(),
                "Filtered search should find same or fewer stations");
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_ThrowsExceptionWhenNotBuilt() {
        manager.loadStationsFromCSV("/test_stations.csv");

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.radiusSearchWithSummaryFiltered(41.0, -8.0, 100.0, criteria),
                "Should throw exception when spatial index not built");

        assertTrue(exception.getMessage().contains("not built"),
                "Exception message should mention index not built");
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_NoMatchingStations() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("NONEXISTENT")
                .country("XX");

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                41.0, -8.0, 200.0, criteria);

        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.getTotalStations(),
                "Should find no stations matching impossible criteria");
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_ResultsWithinRadius() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        double centerLat = 41.0;
        double centerLon = -8.0;
        double radiusKm = 150.0;

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .cityOnly(true);

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                centerLat, centerLon, radiusKm, criteria);

        for (Station station : result.getAllStationsSorted()) {
            double distance = calculateDistance(centerLat, centerLon,
                    station.getLatitude(), station.getLongitude());
            assertTrue(distance <= radiusKm + 1.0,
                    "Filtered station should still be within radius");
        }
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_PreservesDistanceOrdering() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .timezoneGroup("CET");

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                48.8566, 2.3522, 200.0, criteria);

        List<Station> stations = result.getAllStationsSorted();
        if (stations.size() > 1) {
            double centerLat = result.getCenterLat();
            double centerLon = result.getCenterLon();

            for (int i = 0; i < stations.size() - 1; i++) {
                double dist1 = calculateDistance(centerLat, centerLon,
                        stations.get(i).getLatitude(), stations.get(i).getLongitude());
                double dist2 = calculateDistance(centerLat, centerLon,
                        stations.get(i + 1).getLatitude(), stations.get(i + 1).getLongitude());

                assertTrue(dist1 <= dist2 + 1.0,
                        "Stations should be sorted by distance (with tolerance for rounding)");
            }
        }
    }

    @Test
    void testRadiusSearchWithSummaryFiltered_SummaryReflectsFilteredResults() {
        manager.loadStationsFromCSV("/test_stations.csv");
        manager.buildSpatialIndex();

        KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                .country("PT");

        RadiusSearchResult result = manager.radiusSearchWithSummaryFiltered(
                41.0, -8.0, 300.0, criteria);

        StationDensitySummary summary = result.getStationDensitySummary();
        assertEquals(result.getTotalStations(), summary.getTotalStations(),
                "Summary should reflect filtered results");

        for (Map.Entry<String, Integer> entry : summary.getCountByCountry().entrySet()) {
            if (entry.getValue() > 0) {
                assertEquals("PT", entry.getKey(),
                        "Summary should only contain filtered country");
            }
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
