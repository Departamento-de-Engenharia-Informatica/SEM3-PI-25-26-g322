import isep.ipp.pt.g322.model.LatitudeKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class LatitudeKeyTest {

    private LatitudeKey key1;
    private LatitudeKey key2;

    @BeforeEach
    void setUp() {
        key1 = new LatitudeKey(48.8566);  // Paris latitude
        key2 = new LatitudeKey(51.5074);  // London latitude
    }

    @Test
    void testCompareTo_Equal() {
        LatitudeKey keyA = new LatitudeKey(48.8566);
        LatitudeKey keyB = new LatitudeKey(48.8566);
        assertEquals(0, keyA.compareTo(keyB));
    }

    @Test
    void testCompareTo_NegativeLatitudes() {
        LatitudeKey keyA = new LatitudeKey(-33.8688);
        LatitudeKey keyB = new LatitudeKey(-22.9068);
        assertTrue(keyA.compareTo(keyB) < 0);
    }

    @Test
    void testCompareTo_MixedPositiveNegative() {
        LatitudeKey positive = new LatitudeKey(48.8566);
        LatitudeKey negative = new LatitudeKey(-33.8688);
        assertTrue(negative.compareTo(positive) < 0);
        assertTrue(positive.compareTo(negative) > 0);
    }

    @Test
    void testCompareTo_Zero() {
        LatitudeKey zero = new LatitudeKey(0.0);
        LatitudeKey positive = new LatitudeKey(10.0);
        LatitudeKey negative = new LatitudeKey(-10.0);

        assertTrue(zero.compareTo(positive) < 0);
        assertTrue(zero.compareTo(negative) > 0);
        assertEquals(0, zero.compareTo(new LatitudeKey(0.0)));
    }

    @Test
    void testCompareTo_VeryCloseValues() {
        LatitudeKey keyA = new LatitudeKey(48.856600);
        LatitudeKey keyB = new LatitudeKey(48.856601);
        assertTrue(keyA.compareTo(keyB) < 0);
    }

    @Test
    void testCompareTo_Extremes() {
        LatitudeKey southPole = new LatitudeKey(-90.0);
        LatitudeKey northPole = new LatitudeKey(90.0);
        assertTrue(southPole.compareTo(northPole) < 0);
        assertTrue(northPole.compareTo(southPole) > 0);
    }

    @Test
    void testCompareTo_Symmetric() {
        int result1 = key1.compareTo(key2);
        int result2 = key2.compareTo(key1);
        assertTrue((result1 < 0 && result2 > 0) || (result1 > 0 && result2 < 0) || (result1 == 0 && result2 == 0));
    }

    @Test
    void testCompareTo_ConsistentWithEquals() {
        LatitudeKey keyA = new LatitudeKey(48.8566);
        LatitudeKey keyB = new LatitudeKey(48.8566);
        assertEquals(0, keyA.compareTo(keyB));
        assertEquals(keyA.getLatitude(), keyB.getLatitude(), 0.0);
    }
}