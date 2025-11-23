import isep.ipp.pt.g322.datastructures.tree.AVL;
import isep.ipp.pt.g322.datastructures.tree.KDTree2;

import isep.ipp.pt.g322.model.LatitudeKey;
import isep.ipp.pt.g322.model.LongitudeKey;
import isep.ipp.pt.g322.model.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KDTree2Test {

    private AVL<LatitudeKey> latitudeIndex;
    private AVL<LongitudeKey> longitudeIndex;
    private KDTree2 kdTree;

    @BeforeEach
    void setUp() {
        latitudeIndex = new AVL<>();
        longitudeIndex = new AVL<>();
    }

    private Station createStation(String id, double lat, double lon, String timezone, String country) {
        return new Station(
                id,
                lat,
                lon,
                country,
                timezone,
                timezone,
                false,
                false,
                false
        );
    }

    private void addStationToIndices(Station station) {
        LatitudeKey latKey = new LatitudeKey(station.getLatitude());
        LongitudeKey lonKey = new LongitudeKey(station.getLongitude());

        LatitudeKey existingLatKey = latitudeIndex.find(latKey);
        if (existingLatKey == null) {
            latitudeIndex.insert(latKey);
            existingLatKey = latKey;
        }
        existingLatKey.addStation(station);

        LongitudeKey existingLonKey = longitudeIndex.find(lonKey);
        if (existingLonKey == null) {
            longitudeIndex.insert(lonKey);
            existingLonKey = lonKey;
        }
        existingLonKey.addStation(station);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create empty KD-tree with empty indices")
        void testEmptyTree() {
            kdTree = new KDTree2(latitudeIndex, longitudeIndex);
            assertEquals(0, kdTree.size());
            assertEquals(0, kdTree.height());
            assertNull(kdTree.getRoot());
        }

        @Test
        @DisplayName("Should create KD-tree with single station")
        void testSingleStation() {
            Station s1 = createStation("S1", 40.0, -74.0, "America/New_York", "US");
            addStationToIndices(s1);

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            assertEquals(1, kdTree.size());
            assertEquals(1, kdTree.height());
            assertNotNull(kdTree.getRoot());
        }

        @Test
        @DisplayName("Should create balanced tree with multiple stations")
        void testMultipleStations() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 41.0, -73.0, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 39.0, -75.0, "America/New_York", "US"));
            addStationToIndices(createStation("S4", 42.0, -72.0, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            assertEquals(4, kdTree.size());
            assertTrue(kdTree.height() <= 3);
        }
    }

    @Nested
    @DisplayName("Range Query Tests")
    class RangeQueryTests {

        @BeforeEach
        void setUpStations() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 41.0, -73.0, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 39.0, -75.0, "America/New_York", "US"));
            addStationToIndices(createStation("S4", 42.0, -72.0, "America/New_York", "US"));
            addStationToIndices(createStation("S5", 38.0, -76.0, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);
        }

        @Test
        @DisplayName("Should return all stations in range")
        void testRangeQueryAllInRange() {
            List<Station> result = kdTree.rangeQuery(38.0, 42.0, -76.0, -72.0);
            assertEquals(5, result.size());
        }

        @Test
        @DisplayName("Should return subset of stations in range")
        void testRangeQueryPartial() {
            List<Station> result = kdTree.rangeQuery(39.5, 41.5, -74.5, -72.5);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return empty list when no stations in range")
        void testRangeQueryNoMatches() {
            List<Station> result = kdTree.rangeQuery(50.0, 55.0, -80.0, -78.0);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle point query (same min and max)")
        void testPointQuery() {
            List<Station> result = kdTree.rangeQuery(40.0, 40.0, -74.0, -74.0);
            assertEquals(1, result.size());
            assertEquals("S1", result.get(0).getStation());
        }

        @Test
        @DisplayName("Should return results sorted by station name")
        void testRangeQuerySorted() {
            List<Station> result = kdTree.rangeQuery(38.0, 42.0, -76.0, -72.0);
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getStation().compareTo(result.get(i + 1).getStation()) <= 0);
            }
        }
    }

    @Nested
    @DisplayName("Circular Range Query Tests")
    class CircularRangeQueryTests {

        @BeforeEach
        void setUpStations() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 40.1, -74.1, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 40.2, -74.2, "America/New_York", "US"));
            addStationToIndices(createStation("S4", 45.0, -80.0, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);
        }

        @Test
        @DisplayName("Should find stations within radius")
        void testCircularRangeQuery() {
            List<KDTree2.StationDistance> result = kdTree.circularRangeQuery(40.0, -74.0, 30.0);

            assertTrue(result.size() >= 1);
            assertTrue(result.stream().allMatch(sd -> sd.distanceKm <= 30.0));
        }

        @Test
        @DisplayName("Should return results sorted by distance")
        void testCircularRangeQuerySorted() {
            List<KDTree2.StationDistance> result = kdTree.circularRangeQuery(40.0, -74.0, 1000.0);

            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).distanceKm <= result.get(i + 1).distanceKm);
            }
        }

        @Test
        @DisplayName("Should return empty list with very small radius")
        void testCircularRangeQuerySmallRadius() {
            List<KDTree2.StationDistance> result = kdTree.circularRangeQuery(50.0, -80.0, 0.001);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should calculate correct distances")
        void testCircularRangeQueryDistances() {
            List<KDTree2.StationDistance> result = kdTree.circularRangeQuery(40.0, -74.0, 1000.0);

            for (KDTree2.StationDistance sd : result) {
                assertTrue(sd.distanceKm >= 0);
            }
        }
    }

    @Nested
    @DisplayName("Nearest Neighbor Tests")
    class NearestNeighborTests {

        @BeforeEach
        void setUpStations() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 41.0, -73.0, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 39.0, -75.0, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);
        }

        @Test
        @DisplayName("Should find nearest neighbor")
        void testNearestNeighbor() {
            KDTree2.StationDistance result = kdTree.nearestNeighbor(40.1, -74.1);

            assertNotNull(result);
            assertEquals("S1", result.station.getStation());
        }

        @Test
        @DisplayName("Should return exact match when query point is a station")
        void testNearestNeighborExactMatch() {
            KDTree2.StationDistance result = kdTree.nearestNeighbor(40.0, -74.0);

            assertNotNull(result);
            assertEquals("S1", result.station.getStation());
            assertEquals(0.0, result.distanceKm, 0.001);
        }

        @Test
        @DisplayName("Should return null for empty tree")
        void testNearestNeighborEmptyTree() {
            kdTree = new KDTree2(new AVL<>(), new AVL<>());
            KDTree2.StationDistance result = kdTree.nearestNeighbor(40.0, -74.0);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("K-Nearest Neighbors Tests")
    class KNearestNeighborsTests {

        @BeforeEach
        void setUpStations() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 40.1, -74.1, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 40.2, -74.2, "America/New_York", "US"));
            addStationToIndices(createStation("S4", 40.3, -74.3, "America/New_York", "US"));
            addStationToIndices(createStation("S5", 40.4, -74.4, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);
        }

        @Test
        @DisplayName("Should find k nearest neighbors")
        void testKNearestNeighbors() {
            List<KDTree2.StationDistance> result = kdTree.kNearestNeighbors(40.0, -74.0, 3);

            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Should return results sorted by distance")
        void testKNearestNeighborsSorted() {
            List<KDTree2.StationDistance> result = kdTree.kNearestNeighbors(40.0, -74.0, 5);

            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).distanceKm <= result.get(i + 1).distanceKm);
            }
        }

        @Test
        @DisplayName("Should handle k larger than tree size")
        void testKNearestNeighborsKTooLarge() {
            List<KDTree2.StationDistance> result = kdTree.kNearestNeighbors(40.0, -74.0, 10);

            assertEquals(5, result.size());
        }

        @Test
        @DisplayName("Should return empty list for k <= 0")
        void testKNearestNeighborsInvalidK() {
            List<KDTree2.StationDistance> result = kdTree.kNearestNeighbors(40.0, -74.0, 0);
            assertTrue(result.isEmpty());

            result = kdTree.kNearestNeighbors(40.0, -74.0, -1);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list for empty tree")
        void testKNearestNeighborsEmptyTree() {
            kdTree = new KDTree2(new AVL<>(), new AVL<>());
            List<KDTree2.StationDistance> result = kdTree.kNearestNeighbors(40.0, -74.0, 3);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("K-Nearest Neighbors with Filter Tests")
    class KNearestNeighborsWithFilterTests {

        @BeforeEach
        void setUpStations() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 40.1, -74.1, "America/Chicago", "US"));
            addStationToIndices(createStation("S3", 40.2, -74.2, "America/New_York", "US"));
            addStationToIndices(createStation("S4", 40.3, -74.3, "America/Chicago", "CA"));
            addStationToIndices(createStation("S5", 40.4, -74.4, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);
        }

        @Test
        @DisplayName("Should filter by timezone")
        void testFilterByTimezone() {
            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithFilter(40.0, -74.0, 5, "America/New_York");

            assertEquals(3, result.size());
            assertTrue(result.stream()
                    .allMatch(sd -> sd.station.getTimeZoneGroup().equals("America/New_York")));
        }

        @Test
        @DisplayName("Should return all stations when filter is null")
        void testNoFilter() {
            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithFilter(40.0, -74.0, 5, null);

            assertEquals(5, result.size());
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void testNoMatches() {
            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithFilter(40.0, -74.0, 5, "Europe/London");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should respect k limit with filter")
        void testKLimitWithFilter() {
            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithFilter(40.0, -74.0, 2, "America/New_York");

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("K-Nearest Neighbors with Criteria Tests")
    class KNearestNeighborsWithCriteriaTests {

        @BeforeEach
        void setUpStations() {
            Station s1 = new Station("S1", 40.0, -74.0, "US",
                    "America/New_York", "America/New_York", true, false, true);
            Station s2 = new Station("S2", 40.1, -74.1, "US",
                    "America/New_York", "America/New_York", false, true, false);
            Station s3 = new Station("S3", 40.2, -74.2, "CA",
                    "America/Toronto", "America/Toronto", true, true, false);
            Station s4 = new Station("S4", 40.3, -74.3, "US",
                    "America/New_York", "America/New_York", false, false, true);

            addStationToIndices(s1);
            addStationToIndices(s2);
            addStationToIndices(s3);
            addStationToIndices(s4);

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);
        }

        @Test
        @DisplayName("Should filter by multiple criteria")
        void testMultipleCriteria() {
            KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                    .country("US")
                    .airportOnly(true);

            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithCriteria(40.0, -74.0, 5, criteria);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(sd ->
                    sd.station.getCountry().equals("US") && sd.station.isAirport()));
        }

        @Test
        @DisplayName("Should filter by timezone and main station")
        void testTimezoneAndMainStation() {
            KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                    .timezoneGroup("America/New_York")
                    .mainStationOnly(true);

            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithCriteria(40.0, -74.0, 5, criteria);

            assertEquals(1, result.size());
            assertEquals("S2", result.get(0).station.getStation());
        }

        @Test
        @DisplayName("Should filter by city only")
        void testCityFilter() {
            KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                    .cityOnly(true);

            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithCriteria(40.0, -74.0, 5, criteria);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(sd -> sd.station.isCity()));
        }

        @Test
        @DisplayName("Should return all when no criteria")
        void testNoCriteria() {
            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithCriteria(40.0, -74.0, 5, null);

            assertEquals(4, result.size());
        }

        @Test
        @DisplayName("Should return empty when no matches")
        void testNoMatchesCriteria() {
            KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                    .country("UK");

            List<KDTree2.StationDistance> result =
                    kdTree.kNearestNeighborsWithCriteria(40.0, -74.0, 5, criteria);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should return correct size")
        void testSize() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 41.0, -73.0, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 39.0, -75.0, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            assertEquals(3, kdTree.size());
        }

        @Test
        @DisplayName("Should return correct height")
        void testHeight() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 41.0, -73.0, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 39.0, -75.0, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            assertEquals(2, kdTree.height());
        }

        @Test
        @DisplayName("Should return bucket size distribution")
        void testBucketSizeDistribution() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 41.0, -73.0, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            Map<Integer, Integer> distribution = kdTree.getBucketSizeDistribution();

            assertTrue(distribution.containsKey(1));
            assertTrue(distribution.containsKey(2));
        }

        @Test
        @DisplayName("Should generate summary")
        void testGetSummary() {
            addStationToIndices(createStation("S1", 40.0, -74.0, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 41.0, -73.0, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            String summary = kdTree.getSummary();

            assertNotNull(summary);
            assertTrue(summary.contains("KD-Tree Summary"));
            assertTrue(summary.contains("Size"));
            assertTrue(summary.contains("Height"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle coordinates at boundaries")
        void testBoundaryCoordinates() {
            addStationToIndices(createStation("S1", 90.0, 180.0, "UTC", "XX"));
            addStationToIndices(createStation("S2", -90.0, -180.0, "UTC", "XX"));
            addStationToIndices(createStation("S3", 0.0, 0.0, "UTC", "XX"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            assertEquals(3, kdTree.size());
            assertNotNull(kdTree.getRoot());
        }

        @Test
        @DisplayName("Should handle large dataset efficiently")
        void testLargeDataset() {
            for (int i = 0; i < 1000; i++) {
                double lat = 30.0 + (i % 100) * 0.1;
                double lon = -100.0 + (i / 100) * 0.1;
                addStationToIndices(createStation("S" + i, lat, lon, "America/Chicago", "US"));
            }

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            assertEquals(1000, kdTree.size());

            // Tree should be reasonably balanced
            int expectedHeight = (int) Math.ceil(Math.log(1000) / Math.log(2));
            assertTrue(kdTree.height() <= expectedHeight * 1.5);
        }

        @Test
        @DisplayName("Should handle very close coordinates")
        void testVeryCloseCoordinates() {
            addStationToIndices(createStation("S1", 40.000000, -74.000000, "America/New_York", "US"));
            addStationToIndices(createStation("S2", 40.000001, -74.000001, "America/New_York", "US"));
            addStationToIndices(createStation("S3", 40.000002, -74.000002, "America/New_York", "US"));

            kdTree = new KDTree2(latitudeIndex, longitudeIndex);

            assertEquals(3, kdTree.size());

            KDTree2.StationDistance nearest = kdTree.nearestNeighbor(40.000000, -74.000000);
            assertNotNull(nearest);
            assertTrue(nearest.distanceKm < 0.001);
        }
    }

    @Nested
    @DisplayName("StationFilterCriteria Tests")
    class StationFilterCriteriaTests {

        @Test
        @DisplayName("Should create criteria with builder pattern")
        void testBuilderPattern() {
            KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                    .timezoneGroup("America/New_York")
                    .country("US")
                    .mainStationOnly(true);

            assertNotNull(criteria);
            assertEquals("timezone=America/New_York, country=US, mainStation=true",
                    criteria.toString());
        }

        @Test
        @DisplayName("Should match station with all criteria")
        void testMatchesAllCriteria() {
            Station station = new Station("S1", 40.0, -74.0, "US",
                    "America/New_York", "America/New_York", true, true, true);

            KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria()
                    .timezoneGroup("America/New_York")
                    .country("US")
                    .mainStationOnly(true)
                    .cityOnly(true)
                    .airportOnly(true);

            assertTrue(criteria.matches(station));
        }

        @Test
        @DisplayName("Should display 'No filters' when empty")
        void testToStringEmpty() {
            KDTree2.StationFilterCriteria criteria = new KDTree2.StationFilterCriteria();
            assertEquals("No filters", criteria.toString());
        }
    }
}
