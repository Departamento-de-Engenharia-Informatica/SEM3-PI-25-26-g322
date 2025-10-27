package isep.ipp.pt.g322.UI;

import isep.ipp.pt.g322.service.*;
import isep.ipp.pt.g322.model.*;

import java.util.*;

public class CargoHandlingCLI {

    private static final String ITEMS_CSV = "isep/ipp/pt/g322/data/items.csv";
    private static final String WAGONS_CSV = "isep/ipp/pt/g322/data/wagons.csv";
    private static final String BAYS_CSV = "isep/ipp/pt/g322/data/bays.csv";
    private static final String ORDERS_CSV = "isep/ipp/pt/g322/data/orders.csv";
    private static final String ORDER_LINES_CSV = "isep/ipp/pt/g322/data/order_lines.csv";

    private final InventoryService.InventoryState state = new InventoryService.InventoryState();
    private final InventoryService inventoryService = new InventoryService(state);
    private final CsvImporter importer = new CsvImporter(state, inventoryService);    private final OrderAllocationService allocationService = new OrderAllocationService();
    private final PickingPlanService pickingService = new PickingPlanService();
    private final TrolleyAllocatorService trolleyService = new TrolleyAllocatorService();

    private Map<String, Item> items;
    private List<Wagon> wagons;
    private Warehouse warehouse;
    private List<Order> orders;
    private List<Box> boxes;
    private OrderAllocationResult allocationResult;
    private List<Trolley> trolleyPlan;

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
        System.out.print("Your choice: ");
    }

    private void loadAllData() throws ValidationException {
        System.out.println("Loading CSV files...");
        importer.loadItems(ITEMS_CSV);
        System.out.printf("[OK] %d SKUs loaded%n", items.size());

        importer.loadWagons(WAGONS_CSV);
        System.out.printf("[OK] %d wagons loaded%n", wagons.size());

        importer.loadWarehouse(BAYS_CSV);
        System.out.printf("[OK] Warehouse loaded: aisles=%d, total bays=%d%n",
                warehouse.getAllBays().stream().map(BayMeta::getAisleId).distinct().count(),
                warehouse.getTotalBays());

        orders = importer.loadOrders(ORDERS_CSV);
        importer.loadOrderLines(ORDER_LINES_CSV, orders);
        System.out.printf("[OK] %d orders loaded%n", orders.size());

        boxes = new ArrayList<>(state.boxById.values());
        System.out.printf("[OK] %d boxes loaded into inventory%n", boxes.size());

        distributeBoxesToBaysIfEmpty();
        allocationResult = null;
        trolleyPlan = null;
    }

    private void distributeBoxesToBaysIfEmpty() {
        List<BayMeta> allBays = warehouse.getAllBays();
        boolean baysEmpty = allBays.stream().allMatch(b -> b.getBoxes().isEmpty());
        if (!baysEmpty) return;
        if (boxes == null || boxes.isEmpty()) return;

        int bayIdx = 0;
        for (Box box : boxes) {
            int attempts = 0;
            while (!allBays.get(bayIdx).hasCapacityForSKU(box.getSKU()) && attempts < allBays.size()) {
                bayIdx = (bayIdx + 1) % allBays.size();
                attempts++;
            }
            if (attempts >= allBays.size()) break;

            BayMeta targetBay = allBays.get(bayIdx);
            targetBay.getBoxes().add(box);

            targetBay.getBoxes().sort(Comparator.comparing(Box::getBoxID));

            bayIdx = (bayIdx + 1) % allBays.size();
        }
        System.out.println("[OK] Automatic box distribution across bays completed.");
    }

    private void showSummary() {
        if (items == null) {
            System.out.println("No data loaded. Use option 1 to load files.");
            return;
        }
        int distinctSkus = items.size();
        int totalBoxes = boxes == null ? 0 : boxes.size();
        int totalWagons = wagons == null ? 0 : wagons.size();
        int totalOrders = orders == null ? 0 : orders.size();
        int totalBays = warehouse == null ? 0 : warehouse.getTotalBays();
        System.out.println("=== LOADED DATA SUMMARY ===");
        System.out.println("Distinct SKUs: " + distinctSkus);
        System.out.println("Total boxes: " + totalBoxes);
        System.out.println("Total wagons: " + totalWagons);
        System.out.println("Total orders: " + totalOrders);
        System.out.println("Total bays: " + totalBays);
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
            allocationResult = allocationService.allocateOrders();
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
        pickingService.populateUnitWeight();
        List<Allocation> allocations = pickingService.getAllocations();
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
            System.out.println("Memory cleared.");
        } else {
            System.out.println("Invalid operation.");
        }
    }

    private String optionalInt(Integer v) {
        return v == null ? "null" : String.valueOf(v);
    }

    private String optionalInt(int v) {
        return v == 0 ? "0" : String.valueOf(v);
    }
}
