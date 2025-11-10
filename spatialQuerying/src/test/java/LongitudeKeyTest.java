import isep.ipp.pt.g322.model.LongitudeKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class LongitudeKeyTest {

    private LongitudeKey key1;
    private LongitudeKey key2;

    @BeforeEach
    void setUp() {
        key1 = new LongitudeKey(2.3522);   // Paris longitude
        key2 = new LongitudeKey(-0.1276);  // London longitude
    }

    @Test
    void testCompareTo_Equal() {
        LongitudeKey keyA = new LongitudeKey(2.3522);
        LongitudeKey keyB = new LongitudeKey(2.3522);
        assertEquals(0, keyA.compareTo(keyB));
    }
    @Test
    void testCompareTo_NegativeLongitudes() {
        LongitudeKey keyA = new LongitudeKey(-122.4194);
        LongitudeKey keyB = new LongitudeKey(-74.0060);
        assertTrue(keyA.compareTo(keyB) < 0);
    }

    @Test
    void testCompareTo_MixedPositiveNegative() {
        LongitudeKey western = new LongitudeKey(-10.0);
        LongitudeKey eastern = new LongitudeKey(10.0);
        assertTrue(western.compareTo(eastern) < 0);
        assertTrue(eastern.compareTo(western) > 0);
    }

    @Test
    void testCompareTo_Zero() {
        LongitudeKey primeMeridian = new LongitudeKey(0.0);
        LongitudeKey eastern = new LongitudeKey(10.0);
        LongitudeKey western = new LongitudeKey(-10.0);

        assertTrue(primeMeridian.compareTo(eastern) < 0);
        assertTrue(primeMeridian.compareTo(western) > 0);
        assertEquals(0, primeMeridian.compareTo(new LongitudeKey(0.0)));
    }

    @Test
    void testCompareTo_VeryCloseValues() {
        LongitudeKey keyA = new LongitudeKey(2.352200);
        LongitudeKey keyB = new LongitudeKey(2.352201);
        assertTrue(keyA.compareTo(keyB) < 0);
    }

    @Test
    void testCompareTo_Extremes() {
        LongitudeKey westExtreme = new LongitudeKey(-180.0);
        LongitudeKey eastExtreme = new LongitudeKey(180.0);
        assertTrue(westExtreme.compareTo(eastExtreme) < 0);
        assertTrue(eastExtreme.compareTo(westExtreme) > 0);
    }

    @Test
    void testCompareTo_Symmetric() {
        int result1 = key1.compareTo(key2);
        int result2 = key2.compareTo(key1);
        assertTrue((result1 < 0 && result2 > 0) || (result1 > 0 && result2 < 0) || (result1 == 0 && result2 == 0));
    }

    @Test
    void testCompareTo_ConsistentWithEquals() {
        LongitudeKey keyA = new LongitudeKey(2.3522);
        LongitudeKey keyB = new LongitudeKey(2.3522);
        assertEquals(0, keyA.compareTo(keyB));
        assertEquals(keyA.getLongitude(), keyB.getLongitude(), 0.0);
    }
}