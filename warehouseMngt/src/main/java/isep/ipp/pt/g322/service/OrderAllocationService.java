package isep.ipp.pt.g322.service;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import isep.ipp.pt.g322.model.OrderAllocationResult;
import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.model.OrderLineStatus;
import isep.ipp.pt.g322.model.Order;
import isep.ipp.pt.g322.model.Box;
import isep.ipp.pt.g322.model.OrderLine;
public class OrderAllocationService {

    public OrderAllocationResult allocateOrders() {
        MockOrderService orderService = new MockOrderService();
        MockInventoryService inventoryService = new MockInventoryService();

        List<Allocation> allocations = new ArrayList<>();
        List<OrderLineStatus> orderStatuses = new ArrayList<>();

        List<Order> orders = orderService.getOrders();
        Map<String, List<Box>> inventoryBySKU = inventoryService.getBoxBySKU();

        // For every order in the orders list, organizes by comparing each order's
        // priority first, then it's due date and then the OrderID (FEFO and FIFO order)
        orders.sort(Comparator
                .comparingInt(Order::getPriority)
                .thenComparing(Order::getDueDate)
                .thenComparing(Order::getOrderID));

        // For every box in the boxes list gets the values on the inventory by SKU and
        // compares based on their ExpiryDate and if that's equal compares based on the
        // time it got received
        for (List<Box> boxes : inventoryBySKU.values()) {
            boolean allHaveExpiry = boxes.stream().allMatch(b -> b.getExpiryDate() != null);
            if (allHaveExpiry) {
                boxes.sort(Comparator.comparing(Box::getExpiryDate));
            } else {
                boxes.sort(Comparator.comparing(Box::getReceivedAt));
            }
        }

        // For every order in the orders list, goes through each line in OrderLines
        // and gets the SKU from each order
        for (Order order : orders) {
            int lineNumber = 1;
            for (OrderLine line : order.getOrderLines()) {
                String sku = line.getSKU();
                int requestedQty = line.getQuantityRequested();
                int remainingQty = requestedQty;

                // For every box in boxes list, gets its quantity and subtracts from the requested
                // quantity on the order, allocates the order and reduces quantity from the used box
                List<Box> boxes = inventoryBySKU.get(sku);
                if (boxes != null) {
                    for (Box box : boxes) {
                        if (remainingQty == 0) break;

                        int boxQty = box.getQuantity();
                        int allocQty = Math.min(boxQty, remainingQty);

                        if (allocQty > 0) {
                            allocations.add(new Allocation(order.getOrderID(), lineNumber, sku, allocQty, box.getBoxID(), 0, 0));
                            remainingQty -= allocQty;
                            box.setQuantity(boxQty - allocQty);
                        }
                    }
                }

                // Checks based on the allocated and requested quantity if the order status is eligible
                // partial, or undispatchable
                int allocatedQty = requestedQty - remainingQty;
                String status = (allocatedQty == requestedQty)
                        ? "ELIGIBLE"
                        : (allocatedQty > 0 ? "PARTIAL" : "UNDISPATCHABLE");

                orderStatuses.add(new OrderLineStatus(order.getOrderID(), lineNumber, sku, requestedQty, allocatedQty, status));
                lineNumber++;
            }
        }

        //return new OrderAllocationResult(allocations, orderStatuses);
        return null;
    }
}
