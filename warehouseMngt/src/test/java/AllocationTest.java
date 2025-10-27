import isep.ipp.pt.g322.model.*;
import isep.ipp.pt.g322.service.*;

import java.util.*;

public class AllocationTest {
    public static void main(String[] args) throws ValidationException {
        // Use the g322 InventoryService and CsvImporter with InventoryState
        InventoryService.InventoryState state = new InventoryService.InventoryState();
        InventoryService inventoryService = new InventoryService(state);
        CsvImporter importer = new CsvImporter(state, inventoryService);

        // Load data into state
        importer.loadItems("isep/ipp/pt/g322/data/items.csv");
        importer.loadWarehouse("isep/ipp/pt/g322/data/bays.csv");
        importer.loadWagons("isep/ipp/pt/g322/data/wagons.csv");

        // Load orders and order lines
        List<Order> orders = importer.loadOrders("isep/ipp/pt/g322/data/orders.csv");
        importer.loadOrderLines("isep/ipp/pt/g322/data/order_lines.csv", orders);

        // ===== 3. PRINT SKUs DISPONÍVEIS NO INVENTÁRIO =====
        System.out.println("SKUs disponíveis no inventário:");
        Map<String, Integer> skuBoxCount = new HashMap<>();
        for (Box box : inventoryService.getState().boxById.values()) {
            skuBoxCount.put(box.getSKU(), skuBoxCount.getOrDefault(box.getSKU(), 0) + 1);
        }
        for (var entry : skuBoxCount.entrySet()) {
            System.out.printf("%s -> %d boxes%n", entry.getKey(), entry.getValue());
        }

        // ===== 4. INJETAR INVENTÁRIO NA SERVICE =====
        OrderAllocationService service = new OrderAllocationService();
        service.setStrictMode(false); // modo parcial

        OrderAllocationResult result = service.allocateOrders();

        // ===== 5. EXIBIR RESULTADOS =====
        System.out.println("\n===== RESULTADO DAS ALOCAÇÕES =====");
        if (result.getAllocations().isEmpty()) {
            System.out.println("Nenhuma alocação foi feita.");
        } else {
            for (Allocation alloc : result.getAllocations()) {
                System.out.printf(
                        "Order: %s | Line: %d | SKU: %s | Qty: %d | Box: %s | Aisle: %d | Bay: %d\n",
                        alloc.getOrderID(),
                        alloc.getLineNumber(),
                        alloc.getSku(),
                        alloc.getQtAlloc(),
                        alloc.getBoxID(),
                        alloc.getAisle(),
                        alloc.getBay()
                );
            }
        }

        System.out.println("\n===== STATUS DAS LINHAS =====");
        if (result.getOrderStatuses().isEmpty()) {
            System.out.println("Nenhum status registrado.");
        } else {
            for (OrderLineStatus status : result.getOrderStatuses()) {
                System.out.printf(
                        "Order: %s | Line: %d | SKU: %s | Req: %d | Alloc: %d | Status: %s%n",
                        status.getOrderID(),
                        status.getLineNumber(),
                        status.getSku(),
                        status.getReqQty(),
                        status.getQtAlloc(),
                        status.getStatus()
                );
            }
        }
    }
}
