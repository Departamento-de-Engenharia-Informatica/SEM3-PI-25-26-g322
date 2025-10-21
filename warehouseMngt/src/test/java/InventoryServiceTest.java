import isep.ipp.pt.g322.model.BayMeta;
import isep.ipp.pt.g322.model.Location;
import isep.ipp.pt.g322.service.InventoryService;
import isep.ipp.pt.g322.model.Box;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {

    private InventoryService.InventoryState state;
    private InventoryService service;

    private Location L10, L11;

    @BeforeEach
    void setup() {
        state = new InventoryService.InventoryState();
        service = new InventoryService(state);

        // Dois bays no mesmo armazém/aisle
        L10 = new Location("W1", 1, 10);
        L11 = new Location("W1", 1, 11);

        state.bays.put(L10, new BayMeta("W1", 1, 10, 5));
        state.bays.put(L11, new BayMeta("W1", 1, 11, 5));
    }

    private Box box(String id, String sku, int qty, String expiryOrNull, String receivedAt) {
        LocalDate exp = (expiryOrNull == null) ? null : LocalDate.parse(expiryOrNull);
        LocalDateTime rec = LocalDateTime.parse(receivedAt);
        return new Box(id, sku, qty, exp, rec);
    }

    @Test
    void fefoOrdering_perishablesFirst_thenFIFO_thenBoxId() {
        // 3 boxes no mesmo bay (SKU-A)
        Box b1 = box("BX-001", "SKU-A", 10, "2025-10-01", "2025-09-20T09:00:00");
        Box b2 = box("BX-002", "SKU-A", 10, "2025-10-03", "2025-09-20T10:00:00");
        Box b3 = box("BX-003", "SKU-A", 10, null,            "2025-09-21T11:00:00"); // não perecível

        service.setBoxLocation("BX-001", L10);
        service.setBoxLocation("BX-002", L10);
        service.setBoxLocation("BX-003", L10);

        service.insertBoxFEFO(b1);
        service.insertBoxFEFO(b2);
        service.insertBoxFEFO(b3);

        // ordem esperada no FEFO: b1 (exp 10-01) -> b2 (10-03) -> b3 (null)
        var set = state.bayBoxes.get(L10);
        assertNotNull(set);
        var it = set.iterator();
        assertEquals("BX-001", it.next().getBoxID());
        assertEquals("BX-002", it.next().getBoxID());
        assertEquals("BX-003", it.next().getBoxID());

        // empate em expiry: receivedAt desempata; depois boxId
        Box b4 = box("BX-004", "SKU-A", 10, "2025-10-01", "2025-09-20T08:59:00"); // antes de b1
        service.setBoxLocation("BX-004", L10);
        service.insertBoxFEFO(b4);

        // agora a frente deve ser BX-004
        assertEquals("BX-004", state.bayBoxes.get(L10).first().getBoxID());
    }

    @Test
    void dispatch_consumesFromFront_andSupportsPartialAcrossBays() {
        // L10: duas caixas de SKU-A
        Box a1 = box("A1", "SKU-A", 5, "2025-10-02", "2025-09-20T09:00:00");
        Box a2 = box("A2", "SKU-A", 10, "2025-10-05", "2025-09-20T10:00:00");
        service.setBoxLocation("A1", L10);
        service.setBoxLocation("A2", L10);
        service.insertBoxFEFO(a1);
        service.insertBoxFEFO(a2);

        // L11: mais uma caixa com a mesma SKU (para testar ir ao próximo bay por nº asc)
        Box a3 = box("A3", "SKU-A", 7, null, "2025-09-21T08:00:00");
        service.setBoxLocation("A3", L11);
        service.insertBoxFEFO(a3);

        int served = service.dispatch("SKU-A", 12); // consome 12 no total
        assertEquals(12, served);

        // A1 (qty 5) deve ter sido totalmente consumida; A2 ficou com 3
        assertNull(state.boxById.get("A1"), "A1 deveria ter sido removida (qty 0)");
        assertEquals(3, state.boxById.get("A2").getQuantity());

        // O bay L10 existe, apenas pode ter menos caixas
        assertTrue(state.bayBoxes.containsKey(L10));

        // Consumir mais 6 → vai terminar A2 (3) e ir buscar 3 a L11 (A3)
        int served2 = service.dispatch("SKU-A", 6);
        assertEquals(6, served2);
        assertNull(state.boxById.get("A2"), "A2 deveria ter sido removida (qty 0)");
        assertEquals(4, state.boxById.get("A3").getQuantity());
    }

    @Test
    void relocate_insertsOnlyThatBoxInFEFOPositionOfNewBay() {
        // Em L11 temos dois com expiry recente
        Box b1 = box("R1", "SKU-A", 5, "2025-10-01", "2025-09-20T09:00:00");
        Box b2 = box("R2", "SKU-A", 5, "2025-10-04", "2025-09-20T10:00:00");
        service.setBoxLocation("R1", L11);
        service.setBoxLocation("R2", L11);
        service.insertBoxFEFO(b1);
        service.insertBoxFEFO(b2);

        // Em L10 um não perecível
        Box b3 = box("R3", "SKU-A", 5, null, "2025-09-20T11:00:00");
        service.setBoxLocation("R3", L10);
        service.insertBoxFEFO(b3);

        // Relocalizar R1 (exp 10-01) para L10 → deve ficar à frente do não perecível R3
        service.relocate("R1", L10);

        var orderL10 = new ArrayList<>(state.bayBoxes.get(L10));
        assertEquals("R1", orderL10.get(0).getBoxID()); // entrou na posição FEFO correta
        assertEquals("R3", orderL10.get(1).getBoxID());
    }

    @Test
    void insert_respectsBayCapacity() {
        // Capacidade 1
        Location Lcap = new Location("W1", 1, 99);
        state.bays.put(Lcap, new BayMeta("W1", 1, 99, 1));

        Box c1 = box("C1", "SKU-X", 1, null, "2025-09-20T09:00:00");
        Box c2 = box("C2", "SKU-X", 1, null, "2025-09-20T09:01:00");
        service.setBoxLocation("C1", Lcap);
        service.setBoxLocation("C2", Lcap);

        service.insertBoxFEFO(c1);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.insertBoxFEFO(c2));
        assertTrue(ex.getMessage().contains("capacity"), "Mensagem deve indicar excesso de capacidade");
    }
}