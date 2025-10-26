import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.model.Box;
import isep.ipp.pt.g322.service.InventoryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoxesTest {
    public static void main(String[] args) {
        InventoryService.InventoryState state = new InventoryService.InventoryState();
        InventoryService inventoryService = new InventoryService(state);
        List<Box> boxes = new ArrayList<>(state.boxById.values());
        Map<String, List<Box>> boxMap = new HashMap<>();
        for (Box box : boxes) {
            boxMap.computeIfAbsent(box.getSKU(), k -> new ArrayList<>()).add(box);
        }
        System.out.println("\n===== TOTAL BOXES (via inventoryService) =====");
        System.out.println("Total boxes: " + inventoryService.getState().boxById.size());
        System.out.println("\n===== BOXES PER SKU =====");
        for (Map.Entry<String, List<Box>> entry : boxMap.entrySet()) {
            System.out.println("SKU: " + entry.getKey() + " | Box count: " + entry.getValue().size());
        }
        System.out.println(boxes);
        System.out.println("\n===== ALLOCATIONS RESULT =====");
        for (Box box : boxes) {
            System.out.println("BoxID: " + box.getBoxID()
                    + " | SKU: " + box.getSKU()
                    + " | Quantity: " + box.getQuantity());
        }
    }
}
