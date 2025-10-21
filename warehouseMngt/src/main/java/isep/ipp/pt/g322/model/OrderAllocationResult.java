package isep.ipp.pt.g322.model;

import java.util.List;

public class OrderAllocationResult {
    private List<Allocation> allocations;
    private List<OrderLineStatus> orderStatuses;

    public OrderAllocationResult(List<Allocation> allocations, List<OrderLineStatus> orderStatuses) {
        this.allocations = allocations;
        this.orderStatuses = orderStatuses;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public List<OrderLineStatus> getOrderStatuses() {
        return orderStatuses;
    }
}
