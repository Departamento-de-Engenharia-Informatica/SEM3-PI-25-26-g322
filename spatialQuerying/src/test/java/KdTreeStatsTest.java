import isep.ipp.pt.g322.datastructures.tree.KdTree;
import isep.ipp.pt.g322.model.Station;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KdTreeStatsTest {

    private Station st(String name, double lat, double lon) {
        return new Station(
                name,
                lat,
                lon,
                "PT",
                "Europe/Lisbon",
                "PT",
                true,
                true,
                false
        );
    }

    @Test
    void computeStats_countsNodesAndBucketSizesCorrectly() {

        Station s1 = st("A", 41.0, -8.0);
        Station s2 = st("B", 41.0, -8.0);

        Station s3 = st("C", 48.0, 11.0);

        KdTree tree = new KdTree(List.of(s1, s2, s3));
        KdTree.Stats stats = tree.computeStats();


        assertEquals(2, stats.nodeCount);


        assertTrue(stats.height >= 1 && stats.height <= 2);

        Map<Integer, Integer> hist = stats.bucketHistogram;


        assertEquals(2, hist.size(), "Esperamos buckets de size 1 e 2");
        assertEquals(1, hist.get(2), "Deve haver 1 nó com 2 estações");
        assertEquals(1, hist.get(1), "Deve haver 1 nó com 1 estação");


        int totalBuckets = hist.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(stats.nodeCount, totalBuckets);
    }

    @Test
    void computeStats_onSingleNodeTree() {
        Station s1 = st("Solo", 41.0, -8.0);

        KdTree tree = new KdTree(List.of(s1));
        KdTree.Stats stats = tree.computeStats();

        assertEquals(1, stats.nodeCount);
        assertEquals(1, stats.height);

        Map<Integer, Integer> hist = stats.bucketHistogram;
        assertEquals(1, hist.size());
        assertEquals(1, hist.get(1)); // um nó com um elemento

        int totalBuckets = hist.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(stats.nodeCount, totalBuckets);
    }
}
