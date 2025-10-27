import isep.ipp.pt.g322.model.*;
import isep.ipp.pt.g322.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvImporterTest {
    private InventoryService.InventoryState state;
    private CsvImporter importer;

    private static final String ITEMS      = "src/main/java/isep/ipp/pt/g322/data/items.csv";
    private static final String BAYS       = "src/main/java/isep/ipp/pt/g322/data/bays.csv";
    private static final String WAGONS     = "src/main/java/isep/ipp/pt/g322/data/wagons.csv";
    private static final String ORDERS     = "src/main/java/isep/ipp/pt/g322/data/orders.csv";
    private static final String ORDERLINES = "src/main/java/isep/ipp/pt/g322/data/order_lines.csv";


    @BeforeEach
    void setUp() {
        state = new InventoryService.InventoryState();
        InventoryService service = new InventoryService(state);
        importer = new CsvImporter(state, service);
    }

    @Test
    void testLoadItems() {
        assertDoesNotThrow(() -> importer.loadItems(ITEMS));
        assertTrue(!state.items.isEmpty(), "Items should be loaded");
        assertTrue(state.items.containsKey("SKU0001"), "Item SKU0001 should exist");
    }

    @Test
    void testLoadWarehouse() {
        assertDoesNotThrow(() -> importer.loadWarehouse(BAYS));
        assertTrue(!state.bays.isEmpty(), "Bays should be loaded");
        assertTrue(state.bays.containsKey(new Location("W1", 1, 1)), "Bay W1:1:1 should exist");
    }

    @Test
    void testLoadWagons() {
        // wagons dependem de items e bays já carregados
        assertDoesNotThrow(() -> importer.loadItems(ITEMS));
        assertDoesNotThrow(() -> importer.loadWarehouse(BAYS));
        assertDoesNotThrow(() -> importer.loadWagons(WAGONS));
        assertTrue(!state.boxById.isEmpty(), "Boxes should be loaded");
        assertTrue(state.boxById.containsKey("BOX00001"), "Box BOX00001 should exist");
    }

    @Test
    void testLoadOrders() {
        List<Order> orders = assertDoesNotThrow(() -> importer.loadOrders(ORDERS));
        assertTrue(!orders.isEmpty(), "Orders should be loaded");
        assertTrue(orders.stream().anyMatch(o -> o.getOrderID().equals("ORD00001")),
                "Order ORD00001 should exist");
    }

    @Test
    void testLoadOrderLines() {
        List<Order> orders = assertDoesNotThrow(() -> importer.loadOrders(ORDERS));
        assertDoesNotThrow(() -> importer.loadOrderLines(ORDERLINES, orders));
        assertTrue(!orders.get(0).getOrderLines().isEmpty(), "Order lines should be loaded");
    }
}
