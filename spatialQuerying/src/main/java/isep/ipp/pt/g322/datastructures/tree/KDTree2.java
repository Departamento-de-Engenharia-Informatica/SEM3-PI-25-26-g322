package isep.ipp.pt.g322.datastructures.tree;

import isep.ipp.pt.g322.model.LatitudeKey;
import isep.ipp.pt.g322.model.LongitudeKey;
import isep.ipp.pt.g322.model.Station;

import java.util.*;

public class KDTree2 {

    private final KdNode root;
    private final int totalNodes;

    public KDTree2(AVL<LatitudeKey> latitudeIndex, AVL<LongitudeKey> longitudeIndex) {
        BucketExtractionResult extraction = extractBucketsOptimized(latitudeIndex, longitudeIndex);
        this.root = buildBalancedOptimized(
                extraction.latitudeSortedBuckets,
                extraction.longitudeSortedBuckets,
                0,
                extraction.latitudeSortedBuckets.size() - 1,
                0
        );
        this.totalNodes = extraction.latitudeSortedBuckets.size();
    }

    private BucketExtractionResult extractBucketsOptimized(
            AVL<LatitudeKey> latitudeIndex,
            AVL<LongitudeKey> longitudeIndex) {

        Map<String, PointBucket> coordinateMap = new HashMap<>();

        // extracting from latitude AVL (in-order gives sorted latitudes; latitudeKey)
        List<LatitudeKey> latKeys = new ArrayList<>();
        inOrderTraversal(latitudeIndex.root(), latKeys);

        for (LatitudeKey latKey : latKeys) {
            for (Station station : latKey.getStations()) {
                String key = makeCoordinateKey(station.getLatitude(), station.getLongitude());

                coordinateMap.computeIfAbsent(key, k ->
                        new PointBucket(station.getLatitude(), station.getLongitude())
                );

                coordinateMap.get(key).addStation(station);
            }
        }

        // cross-check with longitude AVL (longitudeKey) for consistency
        List<LongitudeKey> lonKeys = new ArrayList<>();
        inOrderTraversal(longitudeIndex.root(), lonKeys);

        int stationsFromLongitude = lonKeys.stream()
                .mapToInt(key -> key.getStations().size())
                .sum();

        int stationsFromLatitude = coordinateMap.values().stream()
                .mapToInt(bucket -> bucket.stations.size())
                .sum();

        if (stationsFromLatitude != stationsFromLongitude) {
            System.err.println("WARNING: AVL trees contain different numbers of stations!");
            System.err.println("  Latitude AVL: " + stationsFromLatitude);
            System.err.println("  Longitude AVL: " + stationsFromLongitude);
        }

        // sort stations within each bucket by name as per US07 requirement
        for (PointBucket bucket : coordinateMap.values()) {
            bucket.sortStations();
        }

        List<PointBucket> latitudeSorted = new ArrayList<>(coordinateMap.values());
        List<PointBucket> longitudeSorted = new ArrayList<>(coordinateMap.values());

        // sorting by latitude (for axis=0 splits)
        latitudeSorted.sort(Comparator.comparingDouble(p -> p.lat));

        // sorting by longitude (for axis=1 splits)
        longitudeSorted.sort(Comparator.comparingDouble(p -> p.lon));

        return new BucketExtractionResult(latitudeSorted, longitudeSorted);
    }

    /**
     * Creates a unique key for a coordinate pair with high precision.
     */
    private String makeCoordinateKey(double lat, double lon) {
        return String.format("%.8f#%.8f", lat, lon);
    }

    private <T extends Comparable<T>> void inOrderTraversal(BST.Node<T> node, List<T> result) {
        if (node == null) return;
        inOrderTraversal(node.getLeft(), result);
        result.add(node.getElement());
        inOrderTraversal(node.getRight(), result);
    }

    private KdNode buildBalancedOptimized(
            List<PointBucket> latSorted,
            List<PointBucket> lonSorted,
            int start,
            int end,
            int depth) {

        if (start > end) {
            return null;
        }

        int axis = depth % 2;
        List<PointBucket> currentSorted = (axis == 0) ? latSorted : lonSorted;

        int medianIndex = start + (end - start) / 2;
        PointBucket median = currentSorted.get(medianIndex);

        KdNode node = new KdNode(axis, median.lat, median.lon, median.stations);

        if (axis == 0) {
            // splitting by latitude, pass median's longitude for breaking any possible tie
            PartitionResult latPartition = partitionByLatitude(latSorted, median.lat, median.lon);
            PartitionResult lonPartition = partitionByLatitude(lonSorted, median.lat, median.lon);

            node.left = buildBalancedOptimized(
                    latPartition.left,
                    lonPartition.left,
                    0,
                    latPartition.left.size() - 1,
                    depth + 1
            );

            node.right = buildBalancedOptimized(
                    latPartition.right,
                    lonPartition.right,
                    0,
                    latPartition.right.size() - 1,
                    depth + 1
            );
        } else {
            // splitting by latitude, pass median's longitude for breaking any possible tie
            PartitionResult latPartition = partitionByLongitude(latSorted, median.lon, median.lat);
            PartitionResult lonPartition = partitionByLongitude(lonSorted, median.lon, median.lat);

            node.left = buildBalancedOptimized(
                    latPartition.left,
                    lonPartition.left,
                    0,
                    latPartition.left.size() - 1,
                    depth + 1
            );

            node.right = buildBalancedOptimized(
                    latPartition.right,
                    lonPartition.right,
                    0,
                    latPartition.right.size() - 1,
                    depth + 1
            );
        }

        return node;
    }

    private PartitionResult partitionByLatitude(List<PointBucket> sorted, double latThreshold,
                                                double lonMedian) {
        List<PointBucket> left = new ArrayList<>();
        List<PointBucket> right = new ArrayList<>();

        for (PointBucket bucket : sorted) {
            if (bucket.lat < latThreshold) {
                left.add(bucket);
            } else if (bucket.lat > latThreshold) {
                right.add(bucket);
            } else {
                // bucket.lat == latThreshold: break any possible tie using longitude
                if (bucket.lon < lonMedian) {
                    left.add(bucket);
                } else if (bucket.lon > lonMedian) {
                    right.add(bucket);
                }
                // if bucket.lon == lonMedian: then exclude as it's the median point
            }
        }

        return new PartitionResult(left, right);
    }

    private PartitionResult partitionByLongitude(List<PointBucket> sorted, double lonThreshold,
                                                 double latMedian) {
        List<PointBucket> left = new ArrayList<>();
        List<PointBucket> right = new ArrayList<>();

        for (PointBucket bucket : sorted) {
            if (bucket.lon < lonThreshold) {
                left.add(bucket);
            } else if (bucket.lon > lonThreshold) {
                right.add(bucket);
            } else {
                // bucket.lat == latThreshold: break any possible tie using lat
                if (bucket.lat < latMedian) {
                    left.add(bucket);
                } else if (bucket.lat > latMedian) {
                    right.add(bucket);
                }
                // if bucket.lon == lonMedian: then exclude as it's the median point
            }
        }

        return new PartitionResult(left, right);
    }

    // ==================== QUERY METHODS ====================

    public List<Station> rangeQuery(double minLat, double maxLat, double minLon, double maxLon) {
        List<Station> result = new ArrayList<>();
        rangeQueryRecursive(root, minLat, maxLat, minLon, maxLon, result);
        result.sort(Comparator.comparing(Station::getStation));
        return result;
    }

    private void rangeQueryRecursive(KdNode node, double minLat, double maxLat,
                                     double minLon, double maxLon, List<Station> result) {
        if (node == null) return;

        if (node.lat >= minLat && node.lat <= maxLat &&
                node.lon >= minLon && node.lon <= maxLon) {
            result.addAll(node.stationsAtPoint);
        }

        if (node.axis == 0) {  // latitude split aka axis 0
            if (minLat <= node.lat) {
                rangeQueryRecursive(node.left, minLat, maxLat, minLon, maxLon, result);
            }
            if (maxLat >= node.lat) {
                rangeQueryRecursive(node.right, minLat, maxLat, minLon, maxLon, result);
            }
        } else {  // longitude split aka axis 1
            if (minLon <= node.lon) {
                rangeQueryRecursive(node.left, minLat, maxLat, minLon, maxLon, result);
            }
            if (maxLon >= node.lon) {
                rangeQueryRecursive(node.right, minLat, maxLat, minLon, maxLon, result);
            }
        }
    }

    /**
     * Circular range query: Find all stations within a radius from a point.
     *
     * @param centerLat Center latitude
     * @param centerLon Center longitude
     * @param radiusKm Radius in kilometers
     * @return List of stations within the circle, sorted by distance
     */
    public List<StationDistance> circularRangeQuery(double centerLat, double centerLon, double radiusKm) {
        List<StationDistance> result = new ArrayList<>();
        circularRangeQueryRecursive(root, centerLat, centerLon, radiusKm, result);
        result.sort(Comparator.comparingDouble(sd -> sd.distanceKm));
        return result;
    }

    private void circularRangeQueryRecursive(KdNode node, double centerLat, double centerLon,
                                             double radiusKm, List<StationDistance> result) {
        if (node == null) return;

        double distance = haversineDistance(centerLat, centerLon, node.lat, node.lon);

        if (distance <= radiusKm) {
            for (Station station : node.stationsAtPoint) {
                result.add(new StationDistance(station, distance));
            }
        }

        double distanceToPlane;
        if (node.axis == 0) {  // latitude split aka axis 0
            // approximate distance (1 degree lat ≈ 111 km)
            distanceToPlane = Math.abs(node.lat - centerLat) * 111.0;
        } else {  // longitude split
            // approximate distance )
            double latRadians = Math.toRadians(centerLat);
            distanceToPlane = Math.abs(node.lon - centerLon) * 111.0 * Math.cos(latRadians);
        }

        // search both sides if splitting plane intersects circle
        if (distanceToPlane <= radiusKm) {
            circularRangeQueryRecursive(node.left, centerLat, centerLon, radiusKm, result);
            circularRangeQueryRecursive(node.right, centerLat, centerLon, radiusKm, result);
        } else {
            // only searches the side containing the center of the circle.
            if (node.axis == 0) {
                if (centerLat < node.lat) {
                    circularRangeQueryRecursive(node.left, centerLat, centerLon, radiusKm, result);
                } else {
                    circularRangeQueryRecursive(node.right, centerLat, centerLon, radiusKm, result);
                }
            } else {
                if (centerLon < node.lon) {
                    circularRangeQueryRecursive(node.left, centerLat, centerLon, radiusKm, result);
                } else {
                    circularRangeQueryRecursive(node.right, centerLat, centerLon, radiusKm, result);
                }
            }
        }
    }

    /**
     * Nearest neighbor query: Find the closest station to a given point.
     *
     * Time Complexity: O(log n) average case, O(n) worst case
     *
     * @param lat Query latitude
     * @param lon Query longitude
     * @return Closest station with its distance
     */
    public StationDistance nearestNeighbor(double lat, double lon) {
        if (root == null) return null;

        NearestNeighborResult result = new NearestNeighborResult();
        nearestNeighborRecursive(root, lat, lon, result);

        return result.best;
    }

    private void nearestNeighborRecursive(KdNode node, double queryLat, double queryLon,
                                          NearestNeighborResult result) {
        if (node == null) return;

        // calc distance to cur point
        double distance = haversineDistance(queryLat, queryLon, node.lat, node.lon);

        if (result.best == null || distance < result.best.distanceKm) {
            // pick first station at this coordinate as they are sorted by name according to previous US req
            result.best = new StationDistance(node.stationsAtPoint.get(0), distance);
            result.bestDistanceKm = distance;
        }

        // check which side of the split contains the query point
        boolean goLeft;
        double distanceToPlane;

        if (node.axis == 0) {  // latitude split
            goLeft = queryLat < node.lat;
            distanceToPlane = Math.abs(node.lat - queryLat) * 111.0;  // approximate km
        } else {  // longitude split
            goLeft = queryLon < node.lon;
            double latRadians = Math.toRadians(queryLat);
            distanceToPlane = Math.abs(node.lon - queryLon) * 111.0 * Math.cos(latRadians);
        }

        KdNode firstSide = goLeft ? node.left : node.right;
        KdNode secondSide = goLeft ? node.right : node.left;

        nearestNeighborRecursive(firstSide, queryLat, queryLon, result);

        if (result.best == null || distanceToPlane < result.bestDistanceKm) {
            nearestNeighborRecursive(secondSide, queryLat, queryLon, result);
        }
    }

    /**
     * K-Nearest neighbors query: Find the k closest stations to a given point.
     * Time Complexity: O(log n + k) average case
     *
     * @param lat Query latitude
     * @param lon Query longitude
     * @param k Number of neighbors to find
     * @return List of k closest stations, sorted by distance
     */
    public List<StationDistance> kNearestNeighbors(double lat, double lon, int k) {
        if (root == null || k <= 0) return new ArrayList<>();

        PriorityQueue<StationDistance> maxHeap = new PriorityQueue<>(
                k,
                Comparator.comparingDouble((StationDistance sd) -> sd.distanceKm).reversed()
        );

        kNearestRecursive(root, lat, lon, k, maxHeap);

        List<StationDistance> result = new ArrayList<>(maxHeap);
        result.sort(Comparator.comparingDouble(sd -> sd.distanceKm));

        return result;
    }

    // goal of using priorityQueue here is to maintain the k closest stations found at the moment of the method recursive stack call
    // also it's more efficient to add. the root contains the farthest element, which is the max distance. to get it use peek()
    private void kNearestRecursive(KdNode node, double queryLat, double queryLon,
                                   int k, PriorityQueue<StationDistance> maxHeap) {
        if (node == null) return;

        double distance = haversineDistance(queryLat, queryLon, node.lat, node.lon);

        for (Station station : node.stationsAtPoint) {
            StationDistance sd = new StationDistance(station, distance);

            if (maxHeap.size() < k) {
                maxHeap.offer(sd);
            } else if (distance < maxHeap.peek().distanceKm) {
                maxHeap.poll();
                maxHeap.offer(sd);
            }
        }

        boolean goLeft;
        double distanceToPlane;

        if (node.axis == 0) {
            goLeft = queryLat < node.lat;
            distanceToPlane = Math.abs(node.lat - queryLat) * 111.0;
        } else {
            goLeft = queryLon < node.lon;
            double latRadians = Math.toRadians(queryLat);
            distanceToPlane = Math.abs(node.lon - queryLon) * 111.0 * Math.cos(latRadians);
        }

        KdNode firstSide = goLeft ? node.left : node.right;
        KdNode secondSide = goLeft ? node.right : node.left;

        kNearestRecursive(firstSide, queryLat, queryLon, k, maxHeap);

        if (maxHeap.size() < k || distanceToPlane < maxHeap.peek().distanceKm) {
            kNearestRecursive(secondSide, queryLat, queryLon, k, maxHeap);
        }
    }

    /**
     * Haversine distance calculation between two points on Earth.
     *
     * @return Distance in kilometers
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // earth's radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // ==================== STATISTICS METHODS ====================

    /**
     * Returns the number of nodes in the KD-tree.
     */
    public int size() {
        return size(root);
    }

    private int size(KdNode node) {
        if (node == null) return 0;
        return 1 + size(node.left) + size(node.right);
    }

    public int height() {
        return height(root);
    }

    private int height(KdNode node) {
        if (node == null) return 0;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public Map<Integer, Integer> getBucketSizeDistribution() {
        Map<Integer, Integer> distribution = new TreeMap<>();
        collectBucketSizes(root, distribution);
        return distribution;
    }

    private void collectBucketSizes(KdNode node, Map<Integer, Integer> distribution) {
        if (node == null) return;

        int bucketSize = node.stationsAtPoint.size();
        distribution.merge(bucketSize, 1, Integer::sum);

        collectBucketSizes(node.left, distribution);
        collectBucketSizes(node.right, distribution);
    }


    public KdNode getRoot() {
        return root;
    }

    /**
     * Generates detailed summary of the KD-tree structure.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== OPTIMIZED KD-Tree Summary ===\n");
        sb.append("Size (nodes): ").append(size()).append("\n");
        sb.append("Height: ").append(height()).append("\n");
        sb.append("Expected height for balanced tree: ≈")
                .append((int)Math.ceil(Math.log(size()) / Math.log(2))).append("\n");

        int actualHeight = height();
        int expectedHeight = (int)Math.ceil(Math.log(size()) / Math.log(2));
        double balance = (double) actualHeight / expectedHeight;
        sb.append("Balance factor: ").append(String.format("%.2f", balance))
                .append(" (1.0 = perfect, <1.5 = good)\n\n");

        sb.append("Bucket Size Distribution:\n");
        Map<Integer, Integer> dist = getBucketSizeDistribution();
        int totalStations = 0;
        for (Map.Entry<Integer, Integer> entry : dist.entrySet()) {
            int stationsPerBucket = entry.getKey();
            int numBuckets = entry.getValue();
            totalStations += stationsPerBucket * numBuckets;

            sb.append(String.format("  %d station(s) per coordinate: %d nodes (%.2f%%)\n",
                    stationsPerBucket,
                    numBuckets,
                    100.0 * numBuckets / size()));
        }
        sb.append("\nTotal stations indexed: ").append(totalStations).append("\n");

        return sb.toString();
    }

    private static class PointBucket {
        double lat;
        double lon;
        List<Station> stations;

        PointBucket(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
            this.stations = new ArrayList<>();
        }

        void addStation(Station station) {
            stations.add(station);
        }

        void sortStations() {
            stations.sort(Comparator.comparing(Station::getStation));
        }
    }

    private static class BucketExtractionResult {
        List<PointBucket> latitudeSortedBuckets;
        List<PointBucket> longitudeSortedBuckets;

        BucketExtractionResult(List<PointBucket> latSorted, List<PointBucket> lonSorted) {
            this.latitudeSortedBuckets = latSorted;
            this.longitudeSortedBuckets = lonSorted;
        }
    }

    private static class PartitionResult {
        List<PointBucket> left;
        List<PointBucket> right;

        PartitionResult(List<PointBucket> left, List<PointBucket> right) {
            this.left = left;
            this.right = right;
        }
    }

    private static class NearestNeighborResult {
        StationDistance best;
        double bestDistanceKm = Double.MAX_VALUE;
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
            this.stationsAtPoint = new ArrayList<>(stationsAtPoint);
        }
    }

    /**
     * K-Nearest neighbors query with timezone filter (US09 - 1 filter at most)
     * more efficient to handle 0-1 filters or 2-(...) this way and more easily testable, as parameter type changes, so less conditionals mean
     * more straightforward code for review/debug. adjust if needed
     *
     * @param lat Query latitude
     * @param lon Query longitude
     * @param k Number of neighbors to find
     * @param timezoneFilter Timezone group to filter by (null = no filter)
     * @return List of k closest stations matching the filter, sorted by distance
     */
    public List<StationDistance> kNearestNeighborsWithFilter(double lat, double lon, int k, String timezoneFilter) {
        if (root == null || k <= 0) return new ArrayList<>();

        PriorityQueue<StationDistance> maxHeap = new PriorityQueue<>(
                k,
                Comparator.comparingDouble((StationDistance sd) -> sd.distanceKm).reversed()
        );

        kNearestWithFilter(root, lat, lon, k, timezoneFilter, maxHeap);

        List<StationDistance> result = new ArrayList<>(maxHeap);
        result.sort(Comparator.comparingDouble(sd -> sd.distanceKm));

        return result;
    }

    private void kNearestWithFilter(KdNode node, double queryLat, double queryLon,
                                             int k, String timezoneFilter,
                                             PriorityQueue<StationDistance> maxHeap) {
        if (node == null) return;

        // shortest distance between 2 points on the surface of a sphere (earth in this case)
        double distance = haversineDistance(queryLat, queryLon, node.lat, node.lon);

        // to add stations to the coordinate that are in accordance with the filter aka timezone
        for (Station station : node.stationsAtPoint) {
            if (timezoneFilter == null || station.getTimeZoneGroup().equals(timezoneFilter)) {
                StationDistance sd = new StationDistance(station, distance);

                if (maxHeap.size() < k) {
                    maxHeap.offer(sd);
                } else if (distance < maxHeap.peek().distanceKm) {
                    maxHeap.poll();
                    maxHeap.offer(sd);
                }
            }
        }

        // to determine the order of the search
        boolean goLeft;
        double distanceToPlane;

        if (node.axis == 0) {
            goLeft = queryLat < node.lat;
            distanceToPlane = Math.abs(node.lat - queryLat) * 111.0;
        } else {
            goLeft = queryLon < node.lon;
            double latRadians = Math.toRadians(queryLat);
            distanceToPlane = Math.abs(node.lon - queryLon) * 111.0 * Math.cos(latRadians);
        }

        KdNode firstSide = goLeft ? node.left : node.right;
        KdNode secondSide = goLeft ? node.right : node.left;

        kNearestWithFilter(firstSide, queryLat, queryLon, k, timezoneFilter, maxHeap);

        // to check other side if it contains nearer points/locations
        if (maxHeap.size() < k || distanceToPlane < maxHeap.peek().distanceKm) {
            kNearestWithFilter(secondSide, queryLat, queryLon, k, timezoneFilter, maxHeap);
        }
    }

    /**
     * K-Nearest neighbors with multiple criteria filters (same as above, but this time with more than 1 filter,
     * with more than one filter we can have an array of filters, which has an impact on performance,
     * so 2 methods can be built, one for 1 filter and other for 2 or more)
     *
     * @param lat Query latitude
     * @param lon Query longitude
     * @param k Number of neighbors to find
     * @param criteria Filter criteria (can be null for no filter)
     * @return List of k closest stations matching all criteria
     */
    public List<StationDistance> kNearestNeighborsWithCriteria(double lat, double lon, int k,
                                                               StationFilterCriteria criteria) {
        if (root == null || k <= 0) return new ArrayList<>();

        PriorityQueue<StationDistance> maxHeap = new PriorityQueue<>(
                k,
                Comparator.comparingDouble((StationDistance sd) -> sd.distanceKm).reversed()
        );

        kNearestWithCriteria(root, lat, lon, k, criteria, maxHeap);

        List<StationDistance> result = new ArrayList<>(maxHeap);
        result.sort(Comparator.comparingDouble(sd -> sd.distanceKm));

        return result;
    }

    private void kNearestWithCriteria(KdNode node, double queryLat, double queryLon,
                                               int k, StationFilterCriteria criteria,
                                               PriorityQueue<StationDistance> maxHeap) {
        if (node == null) return;

        double distance = haversineDistance(queryLat, queryLon, node.lat, node.lon);

        for (Station station : node.stationsAtPoint) {
            if (criteria == null || criteria.matches(station)) {
                StationDistance sd = new StationDistance(station, distance);

                if (maxHeap.size() < k) {
                    maxHeap.offer(sd);
                } else if (distance < maxHeap.peek().distanceKm) {
                    maxHeap.poll();
                    maxHeap.offer(sd);
                }
            }
        }

        boolean goLeft;
        double distanceToPlane;

        if (node.axis == 0) {
            goLeft = queryLat < node.lat;
            distanceToPlane = Math.abs(node.lat - queryLat) * 111.0;
        } else {
            goLeft = queryLon < node.lon;
            double latRadians = Math.toRadians(queryLat);
            distanceToPlane = Math.abs(node.lon - queryLon) * 111.0 * Math.cos(latRadians);
        }

        KdNode firstSide = goLeft ? node.left : node.right;
        KdNode secondSide = goLeft ? node.right : node.left;

        kNearestWithCriteria(firstSide, queryLat, queryLon, k, criteria, maxHeap);

        if (maxHeap.size() < k || distanceToPlane < maxHeap.peek().distanceKm) {
            kNearestWithCriteria(secondSide, queryLat, queryLon, k, criteria, maxHeap);
        }
    }

    public static class StationFilterCriteria {
        private String timezoneGroup;
        private String country;
        private Boolean isMainStation;
        private Boolean isCity;
        private Boolean isAirport;

        public StationFilterCriteria() {}

        public StationFilterCriteria timezoneGroup(String timezone) {
            this.timezoneGroup = timezone;
            return this;
        }

        public StationFilterCriteria country(String country) {
            this.country = country;
            return this;
        }

        public StationFilterCriteria mainStationOnly(boolean mainStation) {
            this.isMainStation = mainStation;
            return this;
        }

        public StationFilterCriteria cityOnly(boolean city) {
            this.isCity = city;
            return this;
        }

        public StationFilterCriteria airportOnly(boolean airport) {
            this.isAirport = airport;
            return this;
        }

        public boolean matches(Station station) {
            if (timezoneGroup != null && !station.getTimeZoneGroup().equals(timezoneGroup)) {
                return false;
            }
            if (country != null && !station.getCountry().equals(country)) {
                return false;
            }
            if (isMainStation != null && station.isMainStation() != isMainStation) {
                return false;
            }
            if (isCity != null && station.isCity() != isCity) {
                return false;
            }
            if (isAirport != null && station.isAirport() != isAirport) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            List<String> filters = new ArrayList<>();
            if (timezoneGroup != null) filters.add("timezone=" + timezoneGroup);
            if (country != null) filters.add("country=" + country);
            if (isMainStation != null) filters.add("mainStation=" + isMainStation);
            if (isCity != null) filters.add("city=" + isCity);
            if (isAirport != null) filters.add("airport=" + isAirport);
            return filters.isEmpty() ? "No filters" : String.join(", ", filters);
        }
    }

    public static class StationDistance {
        public final Station station;
        public final double distanceKm;

        public StationDistance(Station station, double distanceKm) {
            this.station = station;
            this.distanceKm = distanceKm;
        }

        @Override
        public String toString() {
            return String.format("%s (%.2f km)", station.getStation(), distanceKm);
        }
    }
}