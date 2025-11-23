package isep.ipp.pt.g322.datastructures.tree;

import isep.ipp.pt.g322.model.Station;

import java.util.*;
import java.util.stream.Collectors;

public class KdTree {
    /**
     * US08 — Queries de exemplo:
     *
     * // 1. Todas as estações em Portugal na região do Porto (apenas cidades)
     * List<Station> q1 = kdTree.searchRegion(41.1, 41.3, -8.7, -8.5, true, null, "PT");
     *
     * // 2. Estações principais na Espanha dentro da área de Madrid
     * List<Station> q2 = kdTree.searchRegion(40.3, 40.6, -3.9, -3.5, null, true, "ES");
     *
     * // 3. Todas as estações no retângulo, qualquer país, não cidades
     * List<Station> q3 = kdTree.searchRegion(39.0, 42.0, -9.0, -2.0, false, null, "all");
     *
     * // 4. Todas as estações na região de Lisboa, qualquer tipo
     * List<Station> q4 = kdTree.searchRegion(38.7, 38.8, -9.2, -9.0, null, null, "PT");
     *
     * // 5. Todas as estações na caixa delimitadora, sem filtros
     * List<Station> q5 = kdTree.searchRegion(36.0, 44.0, -10.0, 4.0, null, null, null);
     */

    private final KdNode root;
        /**
         * @param latMin 
         * @param latMax 
         * @param lonMin
         * @param lonMax 
         * @param isCity 
         * @param isMainStation
         * @param country
         * @return 
         */
        public List<Station> searchRegion(double latMin, double latMax, double lonMin, double lonMax,
                                          Boolean isCity, Boolean isMainStation, String country) {
            List<Station> result = new ArrayList<>();
            searchRegionRecursive(root, latMin, latMax, lonMin, lonMax, isCity, isMainStation, country, result);
            return result;
        }

        private void searchRegionRecursive(KdNode node, double latMin, double latMax, double lonMin, double lonMax,
                                           Boolean isCity, Boolean isMainStation, String country, List<Station> result) {
            if (node == null) return;
            // Prune if node is outside region
            if (node.lat < latMin || node.lat > latMax || node.lon < lonMin || node.lon > lonMax) {
                // Check which side to recurse
                if (node.axis == 0) { // latitude split
                    if (node.lat > latMax) searchRegionRecursive(node.left, latMin, latMax, lonMin, lonMax, isCity, isMainStation, country, result);
                    else if (node.lat < latMin) searchRegionRecursive(node.right, latMin, latMax, lonMin, lonMax, isCity, isMainStation, country, result);
                } else { // longitude split
                    if (node.lon > lonMax) searchRegionRecursive(node.left, latMin, latMax, lonMin, lonMax, isCity, isMainStation, country, result);
                    else if (node.lon < lonMin) searchRegionRecursive(node.right, latMin, latMax, lonMin, lonMax, isCity, isMainStation, country, result);
                }
                return;
            }
            // Node is inside region
            for (Station s : node.stationsAtPoint) {
                // Apply filters
                boolean matchesCity = (isCity == null) || (s.isCity() == isCity);
                boolean matchesMain = (isMainStation == null) || (s.isMainStation() == isMainStation);
                boolean matchesCountry = (country == null) || country.equalsIgnoreCase("all") || s.getCountry().equalsIgnoreCase(country);
                
                if (matchesCity && matchesMain && matchesCountry) {
                    result.add(s);
                }
            }
            // Recurse both sides
            searchRegionRecursive(node.left, latMin, latMax, lonMin, lonMax, isCity, isMainStation, country, result);
            searchRegionRecursive(node.right, latMin, latMax, lonMin, lonMax, isCity, isMainStation, country, result);
        }

    public KdTree(List<Station> stations) {

        Map<String, List<Station>> grouped = stations.stream()
                .collect(Collectors.groupingBy(s ->
                        s.getLatitude() + "#" + s.getLongitude()));

        List<PointBucket> buckets = new ArrayList<>();
        for (var e : grouped.entrySet()) {
            List<Station> samePoint = e.getValue();
            Station ref = samePoint.get(0);
            samePoint.sort(Comparator.comparing(Station::getStation));
            buckets.add(new PointBucket(ref.getLatitude(), ref.getLongitude(), samePoint));
        }

        this.root = buildBalanced(buckets, 0);
    }



    public int size() {
        return size(root);
    }

    public int height() {
        return height(root);
    }

    public KdNode getRoot() {
        return root;
    }

    public Map<Integer, Integer> getBucketSizeDistribution() {
        Map<Integer, Integer> bucketSizeMap = new HashMap<>();
        collectBucketSizes(root, bucketSizeMap);
        return bucketSizeMap;
    }

    private void collectBucketSizes(KdNode n, Map<Integer, Integer> bucketSizeMap) {
        if (n == null) return;
        int size = n.stationsAtPoint.size();
        bucketSizeMap.merge(size, 1, Integer::sum);
        collectBucketSizes(n.left, bucketSizeMap);
        collectBucketSizes(n.right, bucketSizeMap);
    }

    private int size(KdNode n) {
        if (n == null) return 0;
        return 1 + size(n.left) + size(n.right);
    }

    private int height(KdNode n) {
        if (n == null) return 0;
        return 1 + Math.max(height(n.left), height(n.right));
    }

    private KdNode buildBalanced(List<PointBucket> pts, int depth) {
        if (pts == null || pts.isEmpty()) return null;

        int axis = depth % 2; // 0 = latitude, 1 = longitude
        pts.sort(axis == 0
                ? Comparator.comparingDouble(p -> p.lat)
                : Comparator.comparingDouble(p -> p.lon));

        int mid = pts.size() / 2;
        PointBucket median = pts.get(mid);

        KdNode node = new KdNode(axis, median.lat, median.lon, median.stations);
        node.left = buildBalanced(pts.subList(0, mid), depth + 1);
        node.right = buildBalanced(pts.subList(mid + 1, pts.size()), depth + 1);
        return node;
    }



    private static class PointBucket {
        double lat;
        double lon;
        List<Station> stations;

        PointBucket(double lat, double lon, List<Station> stations) {
            this.lat = lat;
            this.lon = lon;
            this.stations = stations;
        }
    }

    public static class KdNode {
        final int axis;
        final double lat;
        final double lon;
        final List<Station> stationsAtPoint;
        KdNode left;
        KdNode right;

        KdNode(int axis, double lat, double lon, List<Station> stationsAtPoint) {
            this.axis = axis;
            this.lat = lat;
            this.lon = lon;
            this.stationsAtPoint = stationsAtPoint;
        }
    }

    public static class Stats {

        public final int nodeCount;

        public final int height;

        public final Map<Integer, Integer> bucketHistogram;

        public Stats(int nodeCount, int height, Map<Integer, Integer> bucketHistogram) {
            this.nodeCount = nodeCount;
            this.height = height;
            this.bucketHistogram = bucketHistogram;
        }
    }


    public Stats computeStats() {
        Map<Integer, Integer> histogram = new HashMap<>();
        StatAcc acc = computeStatsRecursive(root, histogram);
        return new Stats(acc.nodes, acc.height, histogram);
    }


    private static class StatAcc {
        final int nodes;
        final int height;

        StatAcc(int nodes, int height) {
            this.nodes = nodes;
            this.height = height;
        }
    }

    private StatAcc computeStatsRecursive(KdNode node, Map<Integer, Integer> histogram) {

        if (node == null) {
            return new StatAcc(0, 0);
        }


        int bucketSize = (node.stationsAtPoint == null ? 0 : node.stationsAtPoint.size());
        histogram.merge(bucketSize, 1, Integer::sum);


        StatAcc left  = computeStatsRecursive(node.left,  histogram);
        StatAcc right = computeStatsRecursive(node.right, histogram);

        int nodes  = 1 + left.nodes + right.nodes;
        int height = 1 + Math.max(left.height, right.height); // folha -> height = 1

        return new StatAcc(nodes, height);
    }


    public Map<Integer, Integer> bucketSizeHistogram() {
        return computeStats().bucketHistogram;
    }
}
