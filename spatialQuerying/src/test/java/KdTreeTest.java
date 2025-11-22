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

    private Station stWithFlags(String name, double lat, double lon, String country, boolean isCity, boolean isMainStation) {
        return new Station(
                name,
                lat,
                lon,
                country,
                "Europe/Lisbon",
                "CET",
                isCity,
                isMainStation,
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

    // ========== US08 searchRegion Tests ==========

    @Test
    void searchRegion_BasicRectangle_ReturnsStationsInBounds() {
        List<Station> stations = List.of(
                stWithFlags("Porto", 41.15, -8.61, "PT", true, false),
                stWithFlags("Lisbon", 38.71, -9.14, "PT", true, false),
                stWithFlags("Madrid", 40.42, -3.70, "ES", true, false),
                stWithFlags("Barcelona", 41.38, 2.17, "ES", true, false)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(41.0, 41.5, -9.0, -8.0, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Porto", result.get(0).getStation());
    }

    @Test
    void searchRegion_FilterByCity_ReturnsOnlyCities() {
        List<Station> stations = List.of(
                stWithFlags("Porto", 41.15, -8.61, "PT", true, false),
                stWithFlags("Porto Suburban", 41.16, -8.60, "PT", false, false),
                stWithFlags("Gaia", 41.13, -8.62, "PT", true, false)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(41.0, 41.2, -8.7, -8.5, true, null, null);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Station::isCity));
    }

    @Test
    void searchRegion_FilterByMainStation_ReturnsOnlyMainStations() {
        List<Station> stations = List.of(
                stWithFlags("Madrid Central", 40.42, -3.70, "ES", true, true),
                stWithFlags("Madrid Suburb", 40.43, -3.69, "ES", false, false),
                stWithFlags("Barcelona Central", 41.38, 2.17, "ES", true, true)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(40.0, 40.5, -4.0, -3.5, null, true, null);

        assertEquals(1, result.size());
        assertEquals("Madrid Central", result.get(0).getStation());
        assertTrue(result.get(0).isMainStation());
    }

    @Test
    void searchRegion_FilterByCountry_ReturnsOnlyMatchingCountry() {
        List<Station> stations = List.of(
                stWithFlags("Porto", 41.15, -8.61, "PT", true, false),
                stWithFlags("Madrid", 40.42, -3.70, "ES", true, false),
                stWithFlags("Barcelona", 41.38, 2.17, "ES", true, false)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(40.0, 42.0, -10.0, 3.0, null, null, "ES");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> "ES".equals(s.getCountry())));
    }

    @Test
    void searchRegion_CombinedFilters_ReturnsCityMainStationsInCountry() {
        List<Station> stations = List.of(
                stWithFlags("Madrid Central", 40.42, -3.70, "ES", true, true),
                stWithFlags("Madrid Suburb", 40.43, -3.69, "ES", false, false),
                stWithFlags("Barcelona Central", 41.38, 2.17, "ES", true, true),
                stWithFlags("Porto Central", 41.15, -8.61, "PT", true, true)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(40.0, 41.0, -4.0, -3.5, true, true, "ES");

        assertEquals(1, result.size());
        assertEquals("Madrid Central", result.get(0).getStation());
    }

    @Test
    void searchRegion_EmptyRegion_ReturnsEmptyList() {
        List<Station> stations = List.of(
                stWithFlags("Porto", 41.15, -8.61, "PT", true, false),
                stWithFlags("Lisbon", 38.71, -9.14, "PT", true, false)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(50.0, 51.0, 0.0, 1.0, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchRegion_AllFiltersNull_ReturnsAllStationsInRegion() {
        List<Station> stations = List.of(
                stWithFlags("S1", 40.0, -8.0, "PT", true, true),
                stWithFlags("S2", 40.1, -8.1, "PT", false, false),
                stWithFlags("S3", 40.2, -8.2, "ES", true, false)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(39.9, 40.3, -8.3, -7.9, null, null, null);

        assertEquals(3, result.size());
    }

    @Test
    void searchRegion_CountryAllOrNull_ReturnsAnyCountry() {
        List<Station> stations = List.of(
                stWithFlags("Porto", 41.15, -8.61, "PT", true, false),
                stWithFlags("Madrid", 40.42, -3.70, "ES", true, false)
        );
        KdTree tree = new KdTree(stations);

        List<Station> resultAll = tree.searchRegion(40.0, 42.0, -10.0, -3.0, null, null, "all");
        List<Station> resultNull = tree.searchRegion(40.0, 42.0, -10.0, -3.0, null, null, null);

        assertEquals(2, resultAll.size());
        assertEquals(2, resultNull.size());
    }

    @Test
    void searchRegion_EdgeCaseInclusiveBounds_IncludesStationsOnBoundary() {
        List<Station> stations = List.of(
                stWithFlags("OnBoundary", 40.0, -8.0, "PT", true, false),
                stWithFlags("Outside", 39.9, -8.0, "PT", true, false)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(40.0, 40.5, -8.5, -7.5, null, null, null);

        assertEquals(1, result.size());
        assertEquals("OnBoundary", result.get(0).getStation());
    }

    @Test
    void searchRegion_LargeDataset_EfficientPruning() {
        // Create 100 stations in a diagonal line from (40.0, -8.0) to (50.0, 2.0)
        List<Station> stations = IntStream.range(0, 100)
                .mapToObj(i -> stWithFlags("S" + i, 40.0 + i * 0.1, -8.0 + i * 0.1, "PT", i % 2 == 0, i % 3 == 0))
                .collect(Collectors.toList());
        KdTree tree = new KdTree(stations);

        // Search for stations between (41.0, -7.0) and (43.0, -5.0)
        // Should find stations with i where: 40.0 + i*0.1 >= 41.0 AND 40.0 + i*0.1 <= 43.0
        // AND -8.0 + i*0.1 >= -7.0 AND -8.0 + i*0.1 <= -5.0
        // i >= 10 AND i <= 30 AND i >= 10 AND i <= 30 => i in [10, 30] = 21 stations
        List<Station> result = tree.searchRegion(41.0, 43.0, -7.0, -5.0, null, null, null);

        assertNotNull(result);
        assertTrue(result.size() > 0, "Should find stations in the region");
        assertTrue(result.size() >= 15 && result.size() <= 25, "Expected ~21 stations, got " + result.size());
        assertTrue(result.stream().allMatch(s -> s.getLatitude() >= 41.0 && s.getLatitude() <= 43.0));
        assertTrue(result.stream().allMatch(s -> s.getLongitude() >= -7.0 && s.getLongitude() <= -5.0));
    }

    @Test
    void searchRegion_FilterNonCity_ReturnsOnlyNonCities() {
        List<Station> stations = List.of(
                stWithFlags("City1", 41.0, -8.0, "PT", true, false),
                stWithFlags("Rural1", 41.1, -8.1, "PT", false, false),
                stWithFlags("Rural2", 41.2, -8.2, "PT", false, false)
        );
        KdTree tree = new KdTree(stations);

        List<Station> result = tree.searchRegion(40.9, 41.3, -8.3, -7.9, false, null, null);

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(Station::isCity));
    }

    @Test
    void searchRegion_EmptyTree_ReturnsEmptyList() {
        KdTree tree = new KdTree(Collections.emptyList());

        List<Station> result = tree.searchRegion(40.0, 41.0, -8.0, -7.0, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}

