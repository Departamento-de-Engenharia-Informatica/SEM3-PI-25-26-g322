import isep.ipp.pt.g322.model.DistanceKey;
import isep.ipp.pt.g322.model.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistanceKeyTest {

    private DistanceKey distanceKey;
    private Station station1;
    private Station station2;
    private Station station3;

    @BeforeEach
    void setUp() {
        distanceKey = new DistanceKey(10.5);

        station1 = new Station("Zebra Station", 40.0, -8.0, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);
        station2 = new Station("Alpha Station", 41.0, -8.0, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);
        station3 = new Station("Beta Station", 42.0, -8.0, "PT",
                "Europe/Lisbon", "Europe/Lisbon", false, false, false);
    }

    @Test
    @DisplayName("addStation should add station to list")
    void testAddStation() {
        distanceKey.addStation(station1);

        assertEquals(1, distanceKey.getStations().size());
        assertEquals(station1, distanceKey.getStations().get(0));
    }

    @Test
    @DisplayName("addStation should sort stations by name in descending order")
    void testAddStation_SortsDescending() {
        distanceKey.addStation(station2);
        distanceKey.addStation(station1);
        distanceKey.addStation(station3);

        List<Station> stations = distanceKey.getStations();
        assertEquals(3, stations.size());
        assertEquals("Zebra Station", stations.get(0).getStation());
        assertEquals("Beta Station", stations.get(1).getStation());
        assertEquals("Alpha Station", stations.get(2).getStation());
    }

    @Test
    @DisplayName("addStation should maintain sort order after each addition")
    void testAddStation_MaintainsSortOrder() {
        distanceKey.addStation(station1);
        assertEquals("Zebra Station", distanceKey.getStations().get(0).getStation());

        distanceKey.addStation(station2);
        assertEquals("Zebra Station", distanceKey.getStations().get(0).getStation());
        assertEquals("Alpha Station", distanceKey.getStations().get(1).getStation());

        distanceKey.addStation(station3);
        assertEquals("Zebra Station", distanceKey.getStations().get(0).getStation());
        assertEquals("Beta Station", distanceKey.getStations().get(1).getStation());
        assertEquals("Alpha Station", distanceKey.getStations().get(2).getStation());
    }

    @Test
    @DisplayName("getStations should return defensive copy")
    void testGetStations_DefensiveCopy() {
        distanceKey.addStation(station1);

        List<Station> stations1 = distanceKey.getStations();
        List<Station> stations2 = distanceKey.getStations();

        assertNotSame(stations1, stations2);
        assertEquals(stations1, stations2);
    }

    @Test
    @DisplayName("getStations modifications should not affect internal list")
    void testGetStations_ImmutableExternally() {
        distanceKey.addStation(station1);

        List<Station> stations = distanceKey.getStations();
        stations.clear();

        assertEquals(1, distanceKey.getStations().size());
    }

    @Test
    @DisplayName("compareTo should sort by distance ascending")
    void testCompareTo_Ascending() {
        DistanceKey key1 = new DistanceKey(5.0);
        DistanceKey key2 = new DistanceKey(10.0);

        assertTrue(key1.compareTo(key2) < 0);
        assertTrue(key2.compareTo(key1) > 0);
    }

    @Test
    @DisplayName("compareTo should return 0 for equal distances")
    void testCompareTo_Equal() {
        DistanceKey key1 = new DistanceKey(10.0);
        DistanceKey key2 = new DistanceKey(10.0);

        assertEquals(0, key1.compareTo(key2));
    }

    @Test
    @DisplayName("equals should return true for same distance within tolerance")
    void testEquals_WithinTolerance() {
        DistanceKey key1 = new DistanceKey(10.0);
        DistanceKey key2 = new DistanceKey(10.00009);

        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("equals should return false for distances outside tolerance")
    void testEquals_OutsideTolerance() {
        DistanceKey key1 = new DistanceKey(10.0);
        DistanceKey key2 = new DistanceKey(10.0002);

        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("equals should return true for same object")
    void testEquals_SameObject() {
        assertEquals(distanceKey, distanceKey);
    }

    @Test
    @DisplayName("equals should return false for null")
    void testEquals_Null() {
        assertNotEquals(null, distanceKey);
    }

    @Test
    @DisplayName("equals should return false for different class")
    void testEquals_DifferentClass() {
        assertNotEquals(distanceKey, "10.5");
    }

    @Test
    @DisplayName("hashCode should be same for equal distances")
    void testHashCode_Equal() {
        DistanceKey key1 = new DistanceKey(10.0);
        DistanceKey key2 = new DistanceKey(10.0);

        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("addStation should handle multiple stations with same name prefix")
    void testAddStation_SimilarNames() {
        Station stationA = new Station("Station A", 40.0, -8.0, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);
        Station stationB = new Station("Station B", 41.0, -8.0, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);
        Station stationC = new Station("Station C", 42.0, -8.0, "PT",
                "Europe/Lisbon", "Europe/Lisbon", true, true, false);

        distanceKey.addStation(stationB);
        distanceKey.addStation(stationA);
        distanceKey.addStation(stationC);

        List<Station> stations = distanceKey.getStations();
        assertEquals("Station C", stations.get(0).getStation());
        assertEquals("Station B", stations.get(1).getStation());
        assertEquals("Station A", stations.get(2).getStation());
    }

    @Test
    @DisplayName("compareTo should handle very small differences")
    void testCompareTo_SmallDifferences() {
        DistanceKey key1 = new DistanceKey(10.0);
        DistanceKey key2 = new DistanceKey(10.0001);

        assertTrue(key1.compareTo(key2) < 0);
    }

    @Test
    @DisplayName("compareTo should handle negative distances")
    void testCompareTo_NegativeDistances() {
        DistanceKey key1 = new DistanceKey(-5.0);
        DistanceKey key2 = new DistanceKey(5.0);

        assertTrue(key1.compareTo(key2) < 0);
    }

    @Test
    @DisplayName("addStation should handle single station")
    void testAddStation_SingleStation() {
        distanceKey.addStation(station1);

        List<Station> stations = distanceKey.getStations();
        assertEquals(1, stations.size());
        assertEquals(station1, stations.get(0));
    }
}