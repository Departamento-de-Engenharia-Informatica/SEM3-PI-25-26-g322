package isep.ipp.pt.g322.service;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import isep.ipp.pt.g322.model.*;
import isep.ipp.pt.g322.service.CsvImporter;

public class OrderAllocationService {

    private boolean strictMode = true;
    private final InventoryService.InventoryState state;
    private final InventoryService inventoryService;

    // Default constructor for legacy usage (not recommended)
    public OrderAllocationService() {
        this.state = new InventoryService.InventoryState();
        this.inventoryService = new InventoryService(this.state);
    }

    // New constructor for dependency injection
    public OrderAllocationService(InventoryService.InventoryState state, InventoryService inventoryService) {
        this.state = state;
        this.inventoryService = inventoryService;
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    /**
     * Allocates orders using the current inventory state and service.
     * @param orders List of orders to allocate (should be loaded and populated by caller)
     * @return OrderAllocationResult
     * @throws ValidationException
     */
    public OrderAllocationResult allocateOrders(List<Order> orders) throws ValidationException {
        List<Allocation> allocations = new ArrayList<>();
        List<OrderLineStatus> orderStatuses = new ArrayList<>();

        // Sort orders by priority, due date, and order ID
        orders.sort(Comparator
                .comparingInt(Order::getPriority)
                .thenComparing(Order::getDueDate)
                .thenComparing(Order::getOrderID)
        );

        // Allocate each order line using inventoryService
        for (Order order : orders) {
            for (OrderLine line : order.getOrderLines()) {
                String sku = line.getSKU().trim().toUpperCase();
                int requestedQty = line.getQuantityRequested();
                int lineNumber = line.getLineNumber();
                int allocatedQty = 0;
                List<Allocation> tempAllocations = new ArrayList<>();

                int remainingQty = requestedQty;
                while (remainingQty > 0) {
                    // 1. Encontrar baía com caixas do SKU
                    try {
                        Location loc = inventoryService.findAvailableLocationForSKU(sku);
                        // 2. Obter primeira caixa (FEFO) nessa baía
                        Box box = state.bayBoxes.get(loc) != null && !state.bayBoxes.get(loc).isEmpty()
                                ? state.bayBoxes.get(loc).first() : null;
                        if (box == null || !box.getSKU().equalsIgnoreCase(sku) || box.getQuantity() == 0) {
                            break;
                        }
                        // 3. Alocar min(quantidade_restante, quantidade_disponível)
                        int allocQty = Math.min(box.getQuantity(), remainingQty);
                        if (allocQty > 0) {
                        // 4. Criar registo de alocação
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
                            // 5. Despachar do inventário (atualiza quantidade, remove se vazia)
                            inventoryService.dispatch(sku, allocQty);
                            allocatedQty += allocQty;
                            remainingQty -= allocQty;
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        // Nao ha mais caixas disponiveis para este SKU
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
