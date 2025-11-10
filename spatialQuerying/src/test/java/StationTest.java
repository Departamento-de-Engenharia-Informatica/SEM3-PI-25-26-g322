import isep.ipp.pt.g322.model.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class StationTest {

    private Station validStation;
    private Station anotherStation;

    @BeforeEach
    void setUp() {
        validStation = new Station(
                "Paris Gare du Nord",
                48.8809,
                2.3553,
                "FR",
                "Europe/Paris",
                "CET",
                true,
                true,
                false
        );

        anotherStation = new Station(
                "London St Pancras",
                51.5308,
                -0.1238,
                "GB",
                "Europe/London",
                "WET/GMT",
                true,
                true,
                false
        );
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("Paris Gare du Nord", validStation.getStation());
        assertEquals(48.8809, validStation.getLatitude(), 0.0001);
        assertEquals(2.3553, validStation.getLongitude(), 0.0001);
        assertEquals("FR", validStation.getCountry());
        assertEquals("Europe/Paris", validStation.getTimeZone());
        assertEquals("CET", validStation.getTimeZoneGroup());
        assertTrue(validStation.isCity());
        assertTrue(validStation.isMainStation());
        assertFalse(validStation.isAirport());
    }

    @Test
    void testIsValid_ValidStation() {
        assertTrue(validStation.isValid());
    }

    @Test
    void testIsValid_NullStationName() {
        Station station = new Station(null, 48.8, 2.3, "FR", "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_EmptyStationName() {
        Station station = new Station("", 48.8, 2.3, "FR", "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_WhitespaceStationName() {
        Station station = new Station("   ", 48.8, 2.3, "FR", "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_NullCountry() {
        Station station = new Station("Paris", 48.8, 2.3, null, "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_EmptyCountry() {
        Station station = new Station("Paris", 48.8, 2.3, "", "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_NullTimeZoneGroup() {
        Station station = new Station("Paris", 48.8, 2.3, "FR", "Europe/Paris", null, true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_EmptyTimeZoneGroup() {
        Station station = new Station("Paris", 48.8, 2.3, "FR", "Europe/Paris", "", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_LatitudeTooLow() {
        Station station = new Station("Paris", -91.0, 2.3, "FR", "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_LatitudeTooHigh() {
        Station station = new Station("Paris", 91.0, 2.3, "FR", "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_LatitudeBoundaryMin() {
        Station station = new Station("Paris", -90.0, 2.3, "FR", "Europe/Paris", "CET", true, true, false);
        assertTrue(station.isValid());
    }

    @Test
    void testIsValid_LatitudeBoundaryMax() {
        Station station = new Station("Paris", 90.0, 2.3, "FR", "Europe/Paris", "CET", true, true, false);
        assertTrue(station.isValid());
    }

    @Test
    void testIsValid_LongitudeTooLow() {
        Station station = new Station("Paris", 48.8, -181.0, "FR", "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_LongitudeTooHigh() {
        Station station = new Station("Paris", 48.8, 181.0, "FR", "Europe/Paris", "CET", true, true, false);
        assertFalse(station.isValid());
    }

    @Test
    void testIsValid_LongitudeBoundaryMin() {
        Station station = new Station("Paris", 48.8, -180.0, "FR", "Europe/Paris", "CET", true, true, false);
        assertTrue(station.isValid());
    }

    @Test
    void testIsValid_LongitudeBoundaryMax() {
        Station station = new Station("Paris", 48.8, 180.0, "FR", "Europe/Paris", "CET", true, true, false);
        assertTrue(station.isValid());
    }

    @Test
    void testCompareTo_Equal() {
        Station station1 = new Station("Paris", 48.8, 2.3, "FR", "Europe/Paris", "CET", true, true, false);
        Station station2 = new Station("Paris", 50.0, 3.0, "BE", "Europe/Brussels", "CET", false, false, false);
        assertEquals(0, station1.compareTo(station2));
    }

    @Test
    void testCompareTo_LessThan() {
        Station stationA = new Station("Amsterdam", 52.3, 4.9, "NL", "Europe/Amsterdam", "CET", true, true, false);
        Station stationB = new Station("Berlin", 52.5, 13.4, "DE", "Europe/Berlin", "CET", true, true, false);
        assertTrue(stationA.compareTo(stationB) < 0);
    }

    @Test
    void testCompareTo_GreaterThan() {
        Station stationA = new Station("Zurich", 47.4, 8.5, "CH", "Europe/Zurich", "CET", true, true, false);
        Station stationB = new Station("Amsterdam", 52.3, 4.9, "NL", "Europe/Amsterdam", "CET", true, true, false);
        assertTrue(stationA.compareTo(stationB) > 0);
    }


    @Test
    void testEquals_EqualStations() {
        Station station1 = new Station("Paris", 48.8809, 2.3553, "FR", "Europe/Paris", "CET", true, true, false);
        Station station2 = new Station("Paris", 48.8809, 2.3553, "BE", "Europe/Brussels", "CET", false, false, true);
        assertEquals(station1, station2);
    }

    @Test
    void testEquals_DifferentName() {
        Station station1 = new Station("Paris", 48.8809, 2.3553, "FR", "Europe/Paris", "CET", true, true, false);
        Station station2 = new Station("London", 48.8809, 2.3553, "FR", "Europe/Paris", "CET", true, true, false);
        assertNotEquals(station1, station2);
    }

    @Test
    void testEquals_DifferentLatitude() {
        Station station1 = new Station("Paris", 48.8809, 2.3553, "FR", "Europe/Paris", "CET", true, true, false);
        Station station2 = new Station("Paris", 48.8810, 2.3553, "FR", "Europe/Paris", "CET", true, true, false);
        assertNotEquals(station1, station2);
    }

    @Test
    void testEquals_DifferentLongitude() {
        Station station1 = new Station("Paris", 48.8809, 2.3553, "FR", "Europe/Paris", "CET", true, true, false);
        Station station2 = new Station("Paris", 48.8809, 2.3554, "FR", "Europe/Paris", "CET", true, true, false);
        assertNotEquals(station1, station2);
    }

    @Test
    void testEquals_Null() {
        assertNotEquals(null, validStation);
    }

    @Test
    void testEquals_DifferentClass() {
        assertNotEquals("Not a station", validStation);
    }

    @Test
    void testHashCode_EqualStations() {
        Station station1 = new Station("Paris", 48.8809, 2.3553, "FR", "Europe/Paris", "CET", true, true, false);
        Station station2 = new Station("Paris", 48.8809, 2.3553, "BE", "Europe/Brussels", "CET", false, false, true);
        assertEquals(station1.hashCode(), station2.hashCode());
    }

    @Test
    void testHashCode_DifferentStations() {
        assertNotEquals(validStation.hashCode(), anotherStation.hashCode());
    }


    @Test
    void testStation_WithAirport() {
        Station airport = new Station("Paris CDG", 49.0, 2.5, "FR", "Europe/Paris", "CET", false, false, true);
        assertTrue(airport.isAirport());
        assertFalse(airport.isCity());
        assertFalse(airport.isMainStation());
    }

    @Test
    void testStation_AllBooleansFalse() {
        Station station = new Station("Small Station", 48.8, 2.3, "FR", "Europe/Paris", "CET", false, false, false);
        assertFalse(station.isCity());
        assertFalse(station.isMainStation());
        assertFalse(station.isAirport());
        assertTrue(station.isValid());
    }
}