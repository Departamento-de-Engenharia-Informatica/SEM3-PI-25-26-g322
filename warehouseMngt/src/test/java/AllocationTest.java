import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.model.OrderAllocationResult;
import isep.ipp.pt.g322.model.OrderLineStatus;
import isep.ipp.pt.g322.service.OrderAllocationService;
import isep.ipp.pt.g322.service.PickingPlanService;

import java.util.List;

public class AllocationTest {
    public static void main(String[] args) {

        OrderAllocationService allocationService = new OrderAllocationService();
        OrderAllocationResult result = allocationService.allocateOrders();
        PickingPlanService pps = new PickingPlanService();
        pps.populateUnitWeight();
        List<Allocation> allocations = pps.getAllocations();

        System.out.println("\n===== RESULTADO DAS ALOCAÇÕES =====");
        for (Allocation alloc : allocations) {
            System.out.println("OrderID: " + alloc.getOrderID()
                    + " | Line: " + alloc.getLineNumber()
                    + " | SKU: " + alloc.getSku()
                    + " | Qty Alocada: " + alloc.getQtAlloc()
                    + " | BoxID: " + alloc.getBoxID()
                    + " | Total Weight: " + Math.round(alloc.getTotalWeight()*100.0)/100.0);
        }

        System.out.println("\n===== STATUS DAS LINHAS =====");
        for (OrderLineStatus status : result.getOrderStatuses()) {
            System.out.println("OrderID: " + status.getOrderID()
                    + " | Line: " + status.getLineNumber()
                    + " | SKU: " + status.getSku()
                    + " | Requested: " + status.getReqQty()
                    + " | Allocated: " + status.getQtAlloc()
                    + " | Status: " + status.getStatus());
        }

    }
}

