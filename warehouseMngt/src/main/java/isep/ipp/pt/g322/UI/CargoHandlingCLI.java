package isep.ipp.pt.g322.UI;

import isep.ipp.pt.g322.service.*;
import isep.ipp.pt.g322.model.*;
import isep.ipp.pt.g322.datastructures.tree.KdTree;
import isep.ipp.pt.g322.model.StationManager;

import java.util.*;

public class CargoHandlingCLI {

    private static final String ITEMS_CSV = "isep/ipp/pt/g322/data/items.csv";
    private static final String WAGONS_CSV = "isep/ipp/pt/g322/data/wagons.csv";
    private static final String BAYS_CSV = "isep/ipp/pt/g322/data/bays.csv";
    private static final String ORDERS_CSV = "isep/ipp/pt/g322/data/orders.csv";
    private static final String ORDER_LINES_CSV = "isep/ipp/pt/g322/data/order_lines.csv";

    private final InventoryService.InventoryState state = new InventoryService.InventoryState();
    private final InventoryService inventoryService = new InventoryService(state);
    private final CsvImporter importer = new CsvImporter(state, inventoryService);
    private final OrderAllocationService allocationService = new OrderAllocationService(state, inventoryService);
    private final PickingPlanService pickingService = new PickingPlanService();
    private final TrolleyAllocatorService trolleyService = new TrolleyAllocatorService();

    private Map<String, Item> items;
    private List<Wagon> wagons;
    private Warehouse warehouse;
    private List<Order> orders;
    private List<Box> boxes;
    private OrderAllocationResult allocationResult;
    private List<Trolley> trolleyPlan;

    private StationManager stationManager;
    private KdTree kdTree;

    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new CargoHandlingCLI().mainLoop();
    }

    public boolean mainLoop() {
        boolean running = true;
        boolean exitProgram = false;
        while (running) {
            clearScreen();
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> loadAllData();
                    case "2" -> showSummary();
                    case "3" -> runAllocation();
                    case "4" -> runPickingPlan();
                    case "5" -> showResults();
                    case "6" -> toggleStrictMode();
                    case "7" -> reloadOrClear();
                    case "8" -> {
                        System.out.println("Exiting Cargo Handling...");
                        exitProgram = true;
                        running = false;
                        continue;
                    }
                    case "9" -> runSpatialStationQuery();
                    case "10" -> showComplexityAnalysis();
                    case "11" -> runUS07Menu();
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (ValidationException ve) {
                System.out.println("[VALIDATION ERROR] " + ve.getMessage());
            } catch (Exception e) {
                System.out.println("[ERROR] An error occurred: " + e.getMessage());
                e.printStackTrace(System.out);
            }

            if (running) {
                System.out.print("\nReturn to Cargo menu (M) or exit (E)? [M]: ");
                String resp = scanner.nextLine().trim().toUpperCase();
                if ("E".equals(resp)) {
                    System.out.println("Exiting Cargo Handling...");
                    running = false;
                }
            }
        }
        return exitProgram;
    }

    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {
        }
    }

    private void printMenu() {
        System.out.println("=== Cargo Handling CLI ===");
        System.out.println("1) Load/Import data (items, wagons, bays, orders)");
        System.out.println("2) Show summary of loaded data");
        System.out.println("3) Execute order allocation (US2)");
        System.out.println("4) Generate picking plan (US3)");
        System.out.println("5) Show results (allocations / statuses / picking plan)");
        System.out.println("6) Toggle allocation mode (strict / partial)");
        System.out.println("7) Reload / clear in-memory data");
        System.out.println("8) Exit");
        System.out.println("9) Search stations by geographical area (US08)");
        System.out.println("10) Show complexity analysis");
        System.out.println("11) US07 – Spatial Index (KD-Tree) Statistics");
        System.out.print("Your choice: ");
    }

    private void loadAllData() throws ValidationException {
        System.out.println("Loading CSV files...");
        importer.loadItems(ITEMS_CSV);
            this.items = state.items;
            System.out.printf("[OK] %d SKUs loaded%n", items.size());

    // Only use loadWagonsAsList to get all boxes, skip strict placement
    this.wagons = importer.loadWagonsAsList(WAGONS_CSV);
    System.out.printf("[OK] %d wagons loaded%n", wagons.size());

        importer.loadWarehouse(BAYS_CSV);
        if (!state.bays.isEmpty()) {
            String warehouseId = state.bays.values().iterator().next().getWarehouseId();
            Warehouse wh = new Warehouse(warehouseId);
            for (BayMeta bay : state.bays.values()) {
                wh.addBay(bay.getWarehouseId(), bay.getAisleId(), bay.getBayNumber(), bay.getCapacityBoxes());
            }
            this.warehouse = wh;
            System.out.printf("[OK] Warehouse loaded: aisles=%d, total bays=%d%n",
                    warehouse.getAllBays().stream().map(BayMeta::getAisleId).distinct().count(),
                    warehouse.getTotalBays());
        } else {
            this.warehouse = null;
            System.out.println("[ERROR] Warehouse not loaded.");
        }

        orders = importer.loadOrders(ORDERS_CSV);
        importer.loadOrderLines(ORDER_LINES_CSV, orders);
        System.out.printf("[OK] %d orders loaded%n", orders.size());

        boxes = new ArrayList<>();
        for (Wagon w : wagons) {
            boxes.addAll(w.getBoxes());
        }
        System.out.printf("[OK] %d boxes loaded into inventory%n", boxes.size());
        distributeBoxesToBaysIfEmpty();
        state.boxById.clear();
        state.bayBoxes.clear();
        state.skuToBays.clear();
        if (warehouse != null) {
            for (BayMeta bay : warehouse.getAllBays()) {
                Location loc = new Location(bay.getWarehouseId(), Integer.parseInt(bay.getAisleId()), bay.getBayNumber());
                NavigableSet<Box> set = new TreeSet<>(InventoryService.FEFO);
                set.addAll(bay.getBoxes());
                state.bayBoxes.put(loc, set);
                for (Box box : bay.getBoxes()) {
                    state.boxById.put(box.getBoxID(), box);
                    // Update skuToBays index
                    SortedMap<Integer, Set<Location>> byBay = state.skuToBays.computeIfAbsent(box.getSKU(), k -> new TreeMap<>());
                    Set<Location> locs = byBay.computeIfAbsent(loc.getBay(), k -> new LinkedHashSet<>());
                    locs.add(loc);
                }
                System.out.printf("  Bay %s-%s-%d: %d boxes\n", bay.getWarehouseId(), bay.getAisleId(), bay.getBayNumber(), bay.getBoxes().size());
            }
        }
        allocationResult = null;
        trolleyPlan = null;

        // US08: Load stations for spatial queries (optimized - direct to KD-tree)
        try {
            stationManager = new StationManager();
            kdTree = stationManager.loadStationsDirectlyToKdTree("/train_stations_europe.csv");
            
            if (kdTree != null) {
                System.out.println("[OK] Stations loaded for spatial queries");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Could not load stations: " + e.getMessage());
        }
    }

    private void runUS07Menu() {
        if (stationManager == null || kdTree == null) {
            System.out.println("[INFO] Loading stations and building KD-Tree...");
            try {
                stationManager = new StationManager();
                kdTree = stationManager.loadStationsDirectlyToKdTree("/train_stations_europe.csv");
                System.out.println("[OK] KD-Tree built successfully.");
            } catch (Exception e) {
                System.out.println("[ERROR] Failed to build KD-Tree: " + e.getMessage());
                return;
            }
        }

        boolean running = true;
        Scanner sc = new Scanner(System.in);

        while (running) {
            System.out.println("\n=== US07 – Balanced 2D KD-Tree ===");
            System.out.println("1) Show KD-Tree basic statistics");
            System.out.println("2) Show bucket histogram");
            System.out.println("0) Return to main menu");
            System.out.print("Choice: ");

            String op = sc.nextLine().trim();

            switch (op) {
                case "1" -> showKDTreeStats();
                case "2" -> showBucketHistogram();
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }
    private void showKDTreeStats() {
        if (kdTree == null) {
            System.out.println("[ERROR] KD-Tree not built.");
            return;
        }

        KdTree.Stats stats = kdTree.computeStats();

        System.out.println("\n=== KD-Tree Statistics ===");
        System.out.println("Node count : " + stats.nodeCount);
        System.out.println("Tree height: " + stats.height);
        System.out.println("Distinct bucket sizes: " + stats.bucketHistogram.keySet());
    }
    private void showBucketHistogram() {
        if (kdTree == null) return;

        Map<Integer, Integer> hist = kdTree.getBucketSizeDistribution();

        System.out.println("\n=== Bucket Size Histogram ===");
        hist.forEach((bucketSize, countNodes) ->
                System.out.printf("Bucket size %d → %d nodes%n", bucketSize, countNodes)
        );
    }

    // Distribute loaded boxes across bays in round-robin fashion (like colleagues)
    private void distributeBoxesToBaysIfEmpty() {
        if (warehouse == null || boxes == null || boxes.isEmpty()) return;
        List<BayMeta> allBays = warehouse.getAllBays();
        if (allBays.isEmpty()) return;
        // Only distribute if all bays are empty
        boolean baysEmpty = allBays.stream().allMatch(b -> b.getBoxes().isEmpty());
        if (!baysEmpty) return;
        int bayIdx = 0;
        for (Box box : boxes) {
            int attempts = 0;
            while (!allBays.get(bayIdx).hasCapacity() && attempts < allBays.size()) {
                bayIdx = (bayIdx + 1) % allBays.size();
                attempts++;
            }
            if (attempts >= allBays.size()) break;
            allBays.get(bayIdx).getBoxes().add(box);
            allBays.get(bayIdx).getBoxes().sort(Comparator.naturalOrder());
            bayIdx = (bayIdx + 1) % allBays.size();
        }
        System.out.println("[OK] Automatic box distribution across bays completed.");
    }



    private void showSummary() {
        if (items == null) {
            System.out.println("No data loaded. Use option 1 to load files.");
            return;
        }
        int distinctSkus = items == null ? 0 : items.size();
        int totalBoxes = 0;
        if (warehouse != null) {
            for (BayMeta bay : warehouse.getAllBays()) {
                totalBoxes += bay.getBoxes().size();
            }
        }
        int totalWagons = wagons == null ? 0 : wagons.size();
        int totalOrders = orders == null ? 0 : orders.size();
        int totalBays = warehouse == null ? 0 : warehouse.getTotalBays();
        System.out.println("=== LOADED DATA SUMMARY ===");
        System.out.println("Distinct SKUs: " + distinctSkus);
        System.out.println("Total boxes: " + totalBoxes);
        System.out.println("Total wagons: " + totalWagons);
        System.out.println("Total orders: " + totalOrders);
        System.out.println("Total bays: " + totalBays);
        if (warehouse != null) {
            for (BayMeta bay : warehouse.getAllBays()) {
                System.out.printf("Bay %s-%s-%d: %d boxes\n", bay.getWarehouseId(), bay.getAisleId(), bay.getBayNumber(), bay.getBoxes().size());
            }
        }
    }

    private void runAllocation() {
        if (orders == null || warehouse == null) {
            System.out.println("Incomplete data. Load files first (option 1).");
            return;
        }
        System.out.print("Select mode (1=Strict, 2=Partial) [default=1]: ");
        String mode = scanner.nextLine().trim();
        boolean strict = !"2".equals(mode);
        allocationService.setStrictMode(strict);
        System.out.println("Running allocation (mode: " + (strict ? "STRICT" : "PARTIAL") + ")...");
        try {
            allocationResult = allocationService.allocateOrders(orders);
        } catch (ValidationException ve) {
            System.out.println("[VALIDATION] " + ve.getMessage());
            return;
        }

        int allocCount = allocationResult.getAllocations().size();
        long eligible = allocationResult.getOrderStatuses().stream().filter(s -> "ELIGIBLE".equals(s.getStatus())).count();
        long partial = allocationResult.getOrderStatuses().stream().filter(s -> "PARTIAL".equals(s.getStatus())).count();
        long und = allocationResult.getOrderStatuses().stream().filter(s -> "UNDISPATCHABLE".equals(s.getStatus())).count();

        System.out.printf("[OK] Allocation completed: %d rows%n", allocCount);
        System.out.printf("Lines: ELIGIBLE=%d | PARTIAL=%d | UNDISPATCHABLE=%d%n", eligible, partial, und);

        System.out.print("Print allocation results now? (y/N): ");
        String pr = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(pr)) {
            showResults();
        }
    }

    private void runPickingPlan() throws ValidationException {
        if (allocationResult == null) {
            System.out.println("Run allocation first (option 3).");
            return;
        }
        List<Allocation> allocations = allocationResult.getAllocations();
        pickingService.populateUnitWeight(allocations);
        if (allocations == null || allocations.isEmpty()) {
            System.out.println("No allocations available to generate picking plan.");
            return;
        }
        System.out.print("Enter trolley capacity (kg), e.g., 200: ");
        double capacity;
        try {
            capacity = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid capacity value.");
            return;
        }

        System.out.println("Select heuristic:");
        System.out.println("1-FF (First Fit)");
        System.out.println("2-FFD (First Fit Decreasing)");
        System.out.println("3-BFD (Best Fit Decreasing)");
        System.out.println("(default=1)");
        System.out.print("Your choice: ");
        String h = scanner.nextLine().trim();
        TrolleyAllocatorService.Heuristic heuristic = TrolleyAllocatorService.Heuristic.FF;
        if ("2".equals(h)) heuristic = TrolleyAllocatorService.Heuristic.FFD;
        else if ("3".equals(h)) heuristic = TrolleyAllocatorService.Heuristic.BFD;

        List<Trolley> trolleys = trolleyService.allocateTrolleys(allocations, capacity, heuristic);
        this.trolleyPlan = trolleys;
        System.out.printf("[OK] Picking plan generated: %d trolleys used (heuristic=%s)%n", trolleys.size(), heuristic);
        System.out.print("Print full picking plan now? (y/N): ");
        String pr = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(pr)) {
            pickingService.printPickingPlan(trolleys);
        }
    }

    private void showResults() {
        System.out.println("=== RESULTS ===");
        if (allocationResult == null) {
            System.out.println("No allocation results available. Run option 3 first.");
        } else {
            System.out.println("\n-- Allocations --");
            if (allocationResult.getAllocations().isEmpty()) {
                System.out.println("No allocations were made.");
            } else {
                allocationResult.getAllocations().forEach(a -> {
                    System.out.printf("Order: %s | Line: %d | SKU: %s | Qty: %d | Box: %s | Aisle: %s | Bay: %s%n",
                            a.getOrderID(), a.getLineNumber(), a.getSku(), a.getQtAlloc(),
                            a.getBoxID(), optionalInt(a.getAisle()), optionalInt(a.getBay()));
                });
            }

            System.out.println("\n-- Order Line Statuses --");
            allocationResult.getOrderStatuses().forEach(s ->
                    System.out.printf("Order: %s | Line: %d | SKU: %s | Req: %d | Alloc: %d | Status: %s%n",
                            s.getOrderID(), s.getLineNumber(), s.getSku(), s.getReqQty(), s.getQtAlloc(), s.getStatus())
            );
        }

        if (trolleyPlan != null) {
            System.out.println("\n-- Picking Plan (Trolleys) --");
            trolleyPlan.forEach(t -> {
                double used = t.getUsedWeight();
                double cap = t.getCapacity();
                System.out.printf("Trolley %d : used %.2f / %.2f (%.2f%%)%n",
                        t.getTrolleyID(), used, cap, (cap == 0 ? 0.0 : (used / cap * 100)));
                t.getAllocations().forEach(f ->
                        System.out.printf("   -> Order: %s | Line: %d | SKU: %s | Qty: %d | Box: %s | Aisle: %s | Bay: %s | Weight: %.2f%n",
                                f.getOrderID(), f.getLineNumber(), f.getSku(), f.getQtyPlaced(),
                                f.getBoxID(), optionalInt(f.getAisle()), optionalInt(f.getBay()), f.getWeightPlaced())
                );
            });
        }
    }

    private void toggleStrictMode() {
        System.out.print("Select mode (1=Strict, 2=Partial) [default=1]: ");
        String line = scanner.nextLine().trim();
        allocationService.setStrictMode(!"2".equals(line));
        System.out.println("Mode updated. Please re-run allocation to apply changes.");
        allocationResult = null;
        trolleyPlan = null;
    }

    private void reloadOrClear() {
        System.out.print("1=Reload (reload files), 2=Clear (clear memory): ");
        String op = scanner.nextLine().trim();
        if ("1".equals(op)) {
            try {
                loadAllData();
            } catch (ValidationException ve) {
                System.out.println("[VALIDATION] " + ve.getMessage());
            }
        } else if ("2".equals(op)) {
            items = null;
            wagons = null;
            warehouse = null;
            orders = null;
            boxes = null;
            allocationResult = null;
            trolleyPlan = null;
            state.items.clear();
            state.bays.clear();
            state.bayBoxes.clear();
            state.boxById.clear();
            state.skuToBays.clear();
            System.out.println("Memory cleared.");
        } else {
            System.out.println("Invalid operation.");
        }
    }

    private void runSpatialStationQuery() {
        if (kdTree == null) {
            System.out.println("Station KD-tree not loaded. Please run option 1 to load data first.");
            return;
        }
        try {
            System.out.println("Enter latitude min:");
            double latMin = Double.parseDouble(scanner.nextLine().trim());
            System.out.println("Enter latitude max:");
            double latMax = Double.parseDouble(scanner.nextLine().trim());
            System.out.println("Enter longitude min:");
            double lonMin = Double.parseDouble(scanner.nextLine().trim());
            System.out.println("Enter longitude max:");
            double lonMax = Double.parseDouble(scanner.nextLine().trim());

            System.out.println("Filter by city? (true/false/skip):");
            String cityInput = scanner.nextLine().trim().toLowerCase();
            Boolean isCity = cityInput.equals("true") ? Boolean.TRUE : cityInput.equals("false") ? Boolean.FALSE : null;

            System.out.println("Filter by main station? (true/false/skip):");
            String mainInput = scanner.nextLine().trim().toLowerCase();
            Boolean isMain = mainInput.equals("true") ? Boolean.TRUE : mainInput.equals("false") ? Boolean.FALSE : null;

            System.out.println("Filter by country? (PT/ES/all/skip):");
            String countryInput = scanner.nextLine().trim().toUpperCase();
            String country = (countryInput.equals("SKIP") || countryInput.isEmpty()) ? null : countryInput;

            List<Station> results = kdTree.searchRegion(latMin, latMax, lonMin, lonMax, isCity, isMain, country);
            System.out.printf("Found %d stations in region.%n", results.size());
            for (int i = 0; i < Math.min(results.size(), 20); i++) {
                Station s = results.get(i);
                System.out.printf("%3d. %-40s | Country: %-3s | Lat: %8.4f | Lon: %8.4f | City: %-5s | Main: %-5s%n",
                        (i + 1), s.getStation(), s.getCountry(), s.getLatitude(), s.getLongitude(), s.isCity(), s.isMainStation());
            }
            if (results.size() > 20) {
                System.out.printf("... and %d more results\n", results.size() - 20);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Invalid input or search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String optionalInt(Integer v) {
        return v == null ? "null" : String.valueOf(v);
    }

    private String optionalInt(int v) {
        return v == 0 ? "0" : String.valueOf(v);
    }

    private void showComplexityAnalysis() {
        if (stationManager == null) {
            stationManager = new StationManager();
        }
        System.out.println("\n" + stationManager.getComplexityAnalysis());
    }
}
