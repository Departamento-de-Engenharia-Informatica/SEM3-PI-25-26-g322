import isep.ipp.pt.g322.model.*;
import isep.ipp.pt.g322.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvImporterTest {
    private InventoryService.InventoryState state;
    private CsvImporter importer;

    @BeforeEach
    void setUp() {
        state = new InventoryService.InventoryState();
        importer = new CsvImporter(state);
    }

    @Test
void testLoadItems() {
    String filePath = "src/main/java/isep/ipp/pt/g322/data/items.csv";
    assertDoesNotThrow(() -> importer.loadItems(filePath));
    assertFalse(state.items.isEmpty(), "Items should be loaded");
    assertTrue(state.items.containsKey("SKU0001"), "Item SKU0001 should exist");
}

@Test
void testLoadWarehouse() {
    String filePath = "src/main/java/isep/ipp/pt/g322/data/bays.csv";
    assertDoesNotThrow(() -> importer.loadWarehouse(filePath));
    assertFalse(state.bays.isEmpty(), "Bays should be loaded");
    assertTrue(state.bays.containsKey(new Location("W1", 1, 1)), "Bay W1-1-1 should exist");
}

    @Test
    void testLoadWagons() {
        String filePath = "src/main/java/isep/ipp/pt/g322/data/wagons.csv";
        assertDoesNotThrow(() -> importer.loadWarehouse(filePath));
        assertFalse(state.boxById.isEmpty(), "Boxes should be loaded");
        assertTrue(state.boxById.containsKey("BOX00001"), "Box BOX00001 should exist");
    }

    @Test
    void testLoadOrders() {
        List<Order> orders = assertDoesNotThrow(() -> importer.loadOrders("src/main/java/isep/ipp/pt/g322/data/orders.csv"));
        assertFalse(orders.isEmpty(), "Orders should be loaded");
        assertTrue(orders.stream().anyMatch(o -> o.getOrderID().equals("ORD00001")), "Order ORD00001 should exist");
    }

    @Test
    void testLoadOrderLines() {
        List<Order> orders = assertDoesNotThrow(() -> importer.loadOrders("src/main/java/isep/ipp/pt/g322/data/order_lines.csv"));
        assertDoesNotThrow(() -> importer.loadOrderLines("order_lines.csv", orders));
        assertFalse(orders.get(0).getOrderLines().isEmpty(), "Order lines should be loaded");
    }

    @Test
    void testLoadReturns() {
        assertDoesNotThrow(() -> importer.loadWagons("src/main/java/isep/ipp/pt/g322/data/returns.csv"));
        // Add assertions based on expected return data
    }
}