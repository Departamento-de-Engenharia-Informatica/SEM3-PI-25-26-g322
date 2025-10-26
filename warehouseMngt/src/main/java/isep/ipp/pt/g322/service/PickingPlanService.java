package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.model.AllocationFragment;
import isep.ipp.pt.g322.model.Item;
import isep.ipp.pt.g322.model.OrderAllocationResult;
import isep.ipp.pt.g322.model.Trolley;

import java.util.List;
import java.util.Map;

public class PickingPlanService {
    private List<Allocation> allocations;

public void populateUnitWeight() throws ValidationException {
        OrderAllocationService orderAllocationService = new OrderAllocationService();
        OrderAllocationResult result = orderAllocationService.allocateOrders();
        allocations = result.getAllocations();

        ItemService itemService = new ItemService();
        Map<String, Item> itemMap = itemService.getItemBySKU();

        for (Allocation allocation : allocations) {
            String sku = allocation.getSku();
            Item item = itemMap.get(sku);

            if (item != null) {
                allocation.setUnitWeight(item.getUnitWeight());
            } else {
                System.out.println("Warning: SKU not found in itemMap -> " + sku);
            }
        }
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }
     public void printPickingPlan(List<Trolley> trolleys) {
        System.out.println("Total de trolleys utilizados: " + trolleys.size());
        for (Trolley trolley : trolleys) {
            double utilization = trolley.getUsedWeight() / trolley.getCapacity() * 100;
            double utilizationRounded = Math.round(utilization * 100.0) / 100.0;
            System.out.println("\nTrolley ID: " + trolley.getTrolleyID() + " | Utilização: " + utilizationRounded + "%");
            for (AllocationFragment fragment : trolley.getAllocations()) {
                System.out.println(
                        "OrderID: " + fragment.getOrderID() +
                                " | Line: " + fragment.getLineNumber() +
                                " | SKU: " + fragment.getSku() +
                                " | BoxID: " + fragment.getBoxID() +
                                " | Quantity: " + fragment.getQtyPlaced() +
                                " | Weight: " + Math.round(fragment.getWeightPlaced() * 100.0) / 100.0
                );
            }
        }
    }
}


