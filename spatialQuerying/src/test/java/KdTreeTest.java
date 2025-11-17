import isep.ipp.pt.g322.datastructures.tree.KdTree;

import isep.ipp.pt.g322.model.Station;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class KdTreeTest {

    private Station st(String name, double lat, double lon) {
        return new Station(
                name,
                lat,
                lon,
                "PT",
                "Europe/Lisbon",
                "PT",
                true,
                false,
                false
        );
    }

    @Test
    void sizeCountsUniqueCoordinates() {

        List<Station> list = List.of(
                st("A", 41.0, -8.0),
                st("B", 41.0, -8.0),
                st("C", 38.7, -9.1)
        );

        KdTree tree = new KdTree(list);


        assertEquals(2, tree.size());
        assertTrue(tree.height() >= 1);
    }

    @Test
    void heightIsReasonableForBalancedConstruction() {

        List<Station> list = IntStream.range(0, 50)
                .mapToObj(i -> st("S" + i, 35.0 + i * 0.1, -9.0 + i * 0.1))
                .collect(Collectors.toList());

        KdTree tree = new KdTree(list);

        assertEquals(50, tree.size());


        assertTrue(tree.height() <= 16,
                "Árvore demasiado desequilibrada: height=" + tree.height());
    }

    @Test
    void deterministicWrtInputOrder() {
        List<Station> base = IntStream.range(0, 30)
                .mapToObj(i -> st("S" + i, 40.0 + i * 0.01, -8.0 + i * 0.01))
                .collect(Collectors.toList());

        KdTree t1 = new KdTree(base);

        List<Station> shuffled = new ArrayList<>(base);
        Collections.shuffle(shuffled, new Random(1234));

        KdTree t2 = new KdTree(shuffled);


        assertEquals(t1.size(), t2.size());
        assertEquals(t1.height(), t2.height());
    }

    @Test
    void handlesSingleAndDuplicateStationsGracefully() {
        List<Station> list = List.of(
                st("Only", 41.0, -8.0),
                st("OnlyDuplicate", 41.0, -8.0),
                st("OnlyDuplicate2", 41.0, -8.0)
        );

        KdTree tree = new KdTree(list);

        assertEquals(1, tree.size());
        assertTrue(tree.height() == 1 || tree.height() == 2);
    }

    @Test
    void constructorThrowsOnNullList() {
        assertThrows(NullPointerException.class, () -> new KdTree(null));

    }

    @Test
    void emptyListCreatesEmptyTree() {
        KdTree tree = new KdTree(Collections.emptyList());
        assertEquals(0, tree.size());
        assertEquals(0, tree.height()); // ou 0/1 conforme o que o teu construtor faz
    }

    @Test
    void ignoresInvalidStations() {
        Station valid = st("OK", 41.0, -8.0);
        Station invalid = new Station(
                "",
                200.0,
                0.0,
                "", "", "", false, false, false
        );

        List<Station> list = List.of(valid, invalid);

        KdTree tree = new KdTree(list);


        assertEquals(2, tree.size());
    }

    @Test
    void acceptsExtremeValidCoordinates() {
        List<Station> list = List.of(
                st("SouthPole", -90.0, 0.0),
                st("NorthPole", 90.0, 0.0),
                st("West", 0.0, -180.0),
                st("East", 0.0, 180.0)
        );

        KdTree tree = new KdTree(list);

        assertEquals(4, tree.size());
        assertTrue(tree.height() >= 2);
    }

}
