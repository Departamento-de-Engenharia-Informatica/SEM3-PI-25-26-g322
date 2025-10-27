package isep.ipp.pt.g322.service;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import isep.ipp.pt.g322.model.*;
import isep.ipp.pt.g322.service.CsvImporter;

public class OrderAllocationService {

    private boolean strictMode = true;

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public OrderAllocationResult allocateOrders() throws ValidationException {
        // 1. Create the shared inventory state
        InventoryService.InventoryState state = new InventoryService.InventoryState();
        InventoryService inventoryService = new InventoryService(state);
        CsvImporter csvImporter = new CsvImporter(state, inventoryService);

        List<Allocation> allocations = new ArrayList<>();
        List<OrderLineStatus> orderStatuses = new ArrayList<>();

        // 2. Load items, warehouse, and wagons into the state
    csvImporter.loadItems("warehouseMngt/src/main/java/isep/ipp/pt/g322/data/items.csv");
    csvImporter.loadWarehouse("warehouseMngt/src/main/java/isep/ipp/pt/g322/data/bays.csv");
    csvImporter.loadWagons("warehouseMngt/src/main/java/isep/ipp/pt/g322/data/wagons.csv");

        // 3. Load orders and order lines
        List<Order> orders = csvImporter.loadOrders("warehouseMngt/src/main/java/isep/ipp/pt/g322/data/orders.csv");
        csvImporter.loadOrderLines("warehouseMngt/src/main/java/isep/ipp/pt/g322/data/order_lines.csv", orders);

        // 4. Sort orders by priority, due date, and order ID
        orders.sort(Comparator
                .comparingInt(Order::getPriority)
                .thenComparing(Order::getDueDate)
                .thenComparing(Order::getOrderID)
        );

        // 5. Allocate each order line using inventoryService
        for (Order order : orders) {
            for (OrderLine line : order.getOrderLines()) {
                String sku = line.getSKU().trim().toUpperCase();
                int requestedQty = line.getQuantityRequested();
                int lineNumber = line.getLineNumber();
                int allocatedQty = 0;
                List<Allocation> tempAllocations = new ArrayList<>();

                int remainingQty = requestedQty;
                while (remainingQty > 0) {
                    // Find a bay with available boxes for this SKU
                    try {
                        Location loc = inventoryService.findAvailableLocationForSKU(sku);
                        // Get the box with FEFO in this location
                        Box box = state.bayBoxes.get(loc) != null && !state.bayBoxes.get(loc).isEmpty()
                                ? state.bayBoxes.get(loc).first() : null;
                        if (box == null || !box.getSKU().equalsIgnoreCase(sku) || box.getQuantity() == 0) {
                            break;
                        }
                        int allocQty = Math.min(box.getQuantity(), remainingQty);
                        if (allocQty > 0) {
                            Allocation alloc = new Allocation(
                                    order.getOrderID(),
                                    lineNumber,
                                    sku,
                                    allocQty,
                                    box.getBoxID(),
                                    loc.getAisle(),
                                    loc.getBay(),
                                    order.getPriority(),
                                    order.getDueDate()
                            );
                            tempAllocations.add(alloc);
                            // Dispatch from inventory
                            inventoryService.dispatch(sku, allocQty);
                            allocatedQty += allocQty;
                            remainingQty -= allocQty;
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        // No more available boxes for this SKU
                        break;
                    }
                }

                String status;
                if (strictMode) {
                    if (allocatedQty == requestedQty) {
                        status = "ELIGIBLE";
                        allocations.addAll(tempAllocations);
                    } else {
                        status = "UNDISPATCHABLE";
                    }
                } else {
                    if (allocatedQty == 0) {
                        status = "UNDISPATCHABLE";
                    } else if (allocatedQty < requestedQty) {
                        status = "PARTIAL";
                        allocations.addAll(tempAllocations);
                    } else {
                        status = "ELIGIBLE";
                        allocations.addAll(tempAllocations);
                    }
                }

                orderStatuses.add(new OrderLineStatus(
                        order.getOrderID(),
                        lineNumber,
                        sku,
                        requestedQty,
                        allocatedQty,
                        status
                ));
            }
        }

        return new OrderAllocationResult(allocations, orderStatuses);
    }
}
