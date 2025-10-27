package isep.ipp.pt.g322;
import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.model.Order;
import isep.ipp.pt.g322.service.OrderAllocationService;
import isep.ipp.pt.g322.model.OrderAllocationResult;
import isep.ipp.pt.g322.service.PickingPlanService;
import isep.ipp.pt.g322.service.ValidationException;
import isep.ipp.pt.g322.service.InventoryService;
import isep.ipp.pt.g322.service.CsvImporter;

import java.util.List;

public class Main {
    public static void main(String[] args) throws ValidationException {
        // Setup inventory state and services
        InventoryService.InventoryState state = new InventoryService.InventoryState();
        InventoryService inventoryService = new InventoryService(state);
        CsvImporter importer = new CsvImporter(state, inventoryService);

        // Load data
        importer.loadItems("isep/ipp/pt/g322/data/items.csv");
        importer.loadWarehouse("isep/ipp/pt/g322/data/bays.csv");
        importer.loadWagons("isep/ipp/pt/g322/data/wagons.csv");
        List<Order> orders = importer.loadOrders("isep/ipp/pt/g322/data/orders.csv");
        importer.loadOrderLines("isep/ipp/pt/g322/data/order_lines.csv", orders);

        // Allocate orders
        OrderAllocationService allocService = new OrderAllocationService(state, inventoryService);
        OrderAllocationResult result = allocService.allocateOrders(orders);
        List<Allocation> allocations = result.getAllocations();

        // Populate unit weights
        PickingPlanService pps = new PickingPlanService();
        pps.populateUnitWeight(allocations);

        // Print allocations
        System.out.println("===== ALLOCATIONS COM UNIT WEIGHT =====");
        System.out.println("SKU\tQuantity\tUnitWeight\tTotalWeight");
        for (Allocation alloc : allocations) {
            System.out.println(alloc.getSku() + "\t"
                    + alloc.getQtAlloc() + "\t"
                    + alloc.getUnitWeight() + "\t"
                    + alloc.getTotalWeight());
        }

        System.out.println("\nTest completed: All allocations should now have unitWeight set.");
    }
}

