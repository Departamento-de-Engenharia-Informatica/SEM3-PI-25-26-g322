import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.model.Trolley;
import isep.ipp.pt.g322.service.PickingPlanService;
import isep.ipp.pt.g322.service.TrolleyAllocatorService;
import isep.ipp.pt.g322.service.TrolleyAllocatorService.Heuristic;
import isep.ipp.pt.g322.service.ValidationException;

import java.util.List;
import java.util.Scanner;

public class TrolleyTest {
    public static void main(String[] args) throws ValidationException {
        Scanner scanner = new Scanner(System.in);
        double capacity;
        int choice;
        Heuristic heuristicChoice = Heuristic.FF; // valor padrão

        // Setup inventory state and services
        isep.ipp.pt.g322.service.InventoryService.InventoryState state = new isep.ipp.pt.g322.service.InventoryService.InventoryState();
        isep.ipp.pt.g322.service.InventoryService inventoryService = new isep.ipp.pt.g322.service.InventoryService(state);
        isep.ipp.pt.g322.service.CsvImporter importer = new isep.ipp.pt.g322.service.CsvImporter(state, inventoryService);

        // Load data
        importer.loadItems("isep/ipp/pt/g322/data/items.csv");
        importer.loadWarehouse("isep/ipp/pt/g322/data/bays.csv");
        importer.loadWagons("isep/ipp/pt/g322/data/wagons.csv");
        java.util.List<isep.ipp.pt.g322.model.Order> orders = importer.loadOrders("isep/ipp/pt/g322/data/orders.csv");
        importer.loadOrderLines("isep/ipp/pt/g322/data/order_lines.csv", orders);

        // Allocate orders
        isep.ipp.pt.g322.service.OrderAllocationService allocService = new isep.ipp.pt.g322.service.OrderAllocationService(state, inventoryService);
        isep.ipp.pt.g322.model.OrderAllocationResult result = allocService.allocateOrders(orders);
        List<Allocation> allocations = result.getAllocations();

        // Populate unit weights
        PickingPlanService pps = new PickingPlanService();
        pps.populateUnitWeight(allocations);

        TrolleyAllocatorService service = new TrolleyAllocatorService();

        System.out.print("Please insert trolley capacity: ");
        capacity = scanner.nextDouble();

        System.out.println("Please choose the heuristic: ");
        System.out.println("1 - FF  (First Fit)");
        System.out.println("2 - FFD (First Fit Decreasing)");
        System.out.println("3 - BFD (Best Fit Decreasing)");
        System.out.print("Enter your choice: ");

        choice = scanner.nextInt();

        switch (choice) {
            case 1:
                heuristicChoice = Heuristic.FF;
                break;
            case 2:
                heuristicChoice = Heuristic.FFD;
                break;
            case 3:
                heuristicChoice = Heuristic.BFD;
                break;
            default:
                System.out.println("Invalid choice. Defaulting to FF.");
                heuristicChoice = Heuristic.FF;
                break;
        }

        List<Trolley> trolleys = service.allocateTrolleys(allocations, capacity, heuristicChoice);

        System.out.println("\n===== PICKING PLAN (" + heuristicChoice + ") =====");
        pps.printPickingPlan(trolleys);
    }
}
