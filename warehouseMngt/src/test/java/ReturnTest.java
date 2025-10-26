import isep.ipp.pt.g322.model.Return;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReturnTest {

    @Test
    @DisplayName("Should create Return with all fields")
    void testReturnConstructor() {
        String returnId = "RET001";
        String sku = "SKU123";
        int qty = 10;
        String reason = "Damaged";
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 26, 10, 30);
        LocalDate expiryDate = LocalDate.of(2026, 1, 1);

        Return returnItem = new Return(returnId, sku, qty, reason, timestamp, expiryDate);

        assertEquals(returnId, returnItem.getReturnId());
        assertEquals(sku, returnItem.getSku());
        assertEquals(qty, returnItem.getQty());
        assertEquals(reason, returnItem.getReason());
        assertEquals(timestamp, returnItem.getTimestamp());
        assertEquals(expiryDate, returnItem.getExpiryDate());
    }

    @Test
    @DisplayName("Should create Return with null expiry date")
    void testReturnConstructorWithNullExpiryDate() {
        String returnId = "RET002";
        String sku = "SKU456";
        int qty = 5;
        String reason = "Wrong item";
        LocalDateTime timestamp = LocalDateTime.now();

        Return returnItem = new Return(returnId, sku, qty, reason, timestamp, null);

        assertNull(returnItem.getExpiryDate());
        assertNotNull(returnItem.getReturnId());
        assertNotNull(returnItem.getSku());
    }

    @Test
    @DisplayName("Should set and get returnId")
    void testSetAndGetReturnId() {
        Return returnItem = createSampleReturn();
        String newReturnId = "RET999";

        returnItem.setReturnId(newReturnId);

        assertEquals(newReturnId, returnItem.getReturnId());
    }

    @Test
    @DisplayName("Should set and get SKU")
    void testSetAndGetSku() {
        Return returnItem = createSampleReturn();
        String newSku = "SKU999";

        returnItem.setSku(newSku);

        assertEquals(newSku, returnItem.getSku());
    }

    @Test
    @DisplayName("Should set and get quantity")
    void testSetAndGetQty() {
        Return returnItem = createSampleReturn();
        int newQty = 100;

        returnItem.setQty(newQty);

        assertEquals(newQty, returnItem.getQty());
    }

    @Test
    @DisplayName("Should set and get reason")
    void testSetAndGetReason() {
        Return returnItem = createSampleReturn();
        String newReason = "Expired";

        returnItem.setReason(newReason);

        assertEquals(newReason, returnItem.getReason());
    }

    @Test
    @DisplayName("Should set and get timestamp")
    void testSetAndGetTimestamp() {
        Return returnItem = createSampleReturn();
        LocalDateTime newTimestamp = LocalDateTime.of(2025, 12, 31, 23, 59);

        returnItem.setTimestamp(newTimestamp);

        assertEquals(newTimestamp, returnItem.getTimestamp());
    }

    @Test
    @DisplayName("Should set and get expiry date")
    void testSetAndGetExpiryDate() {
        Return returnItem = createSampleReturn();
        LocalDate newExpiryDate = LocalDate.of(2027, 6, 15);

        returnItem.setExpiryDate(newExpiryDate);

        assertEquals(newExpiryDate, returnItem.getExpiryDate());
    }

    @Test
    @DisplayName("Should be equal when all fields are equal")
    void testEquals_SameFields() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 26, 10, 30);
        LocalDate expiryDate = LocalDate.of(2026, 1, 1);

        Return return1 = new Return("RET001", "SKU123", 10, "Damaged", timestamp, expiryDate);
        Return return2 = new Return("RET001", "SKU123", 10, "Damaged", timestamp, expiryDate);

        assertEquals(return1, return2);
        assertEquals(return1.hashCode(), return2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when returnId differs")
    void testEquals_DifferentReturnId() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 26, 10, 30);
        LocalDate expiryDate = LocalDate.of(2026, 1, 1);

        Return return1 = new Return("RET001", "SKU123", 10, "Damaged", timestamp, expiryDate);
        Return return2 = new Return("RET002", "SKU123", 10, "Damaged", timestamp, expiryDate);

        assertNotEquals(return1, return2);
    }

    @Test
    @DisplayName("Should not be equal when SKU differs")
    void testEquals_DifferentSku() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 26, 10, 30);
        LocalDate expiryDate = LocalDate.of(2026, 1, 1);

        Return return1 = new Return("RET001", "SKU123", 10, "Damaged", timestamp, expiryDate);
        Return return2 = new Return("RET001", "SKU456", 10, "Damaged", timestamp, expiryDate);

        assertNotEquals(return1, return2);
    }

    @Test
    @DisplayName("Should not be equal when quantity differs")
    void testEquals_DifferentQty() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 26, 10, 30);
        LocalDate expiryDate = LocalDate.of(2026, 1, 1);

        Return return1 = new Return("RET001", "SKU123", 10, "Damaged", timestamp, expiryDate);
        Return return2 = new Return("RET001", "SKU123", 20, "Damaged", timestamp, expiryDate);

        assertNotEquals(return1, return2);
    }

    @Test
    @DisplayName("Should not be equal when comparing with null")
    void testEquals_Null() {
        Return returnItem = createSampleReturn();

        assertNotEquals(null, returnItem);
    }

    @Test
    @DisplayName("Should not be equal when comparing with different class")
    void testEquals_DifferentClass() {
        Return returnItem = createSampleReturn();
        String differentObject = "Not a Return";

        assertNotEquals(returnItem, differentObject);
    }

    @Test
    @DisplayName("Should be equal to itself")
    void testEquals_SameInstance() {
        Return returnItem = createSampleReturn();

        assertEquals(returnItem, returnItem);
    }

    @Test
    @DisplayName("Should produce valid toString output")
    void testToString() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 26, 10, 30);
        LocalDate expiryDate = LocalDate.of(2026, 1, 1);
        Return returnItem = new Return("RET001", "SKU123", 10, "Damaged", timestamp, expiryDate);

        String result = returnItem.toString();

        assertNotNull(result);
        assertTrue(result.contains("RET001"));
        assertTrue(result.contains("SKU123"));
        assertTrue(result.contains("10"));
        assertTrue(result.contains("Damaged"));
        assertTrue(result.contains("2025-10-26"));
        assertTrue(result.contains("2026-01-01"));
    }

    @Test
    @DisplayName("Should handle toString with null expiry date")
    void testToString_NullExpiryDate() {
        Return returnItem = new Return("RET001", "SKU123", 10, "Damaged",
                LocalDateTime.now(), null);

        String result = returnItem.toString();

        assertNotNull(result);
        assertTrue(result.contains("RET001"));
        assertTrue(result.contains("null"));
    }

    @Test
    @DisplayName("HashCode should be consistent")
    void testHashCode_Consistency() {
        Return returnItem = createSampleReturn();

        int hash1 = returnItem.hashCode();
        int hash2 = returnItem.hashCode();

        assertEquals(hash1, hash2);
    }

    private Return createSampleReturn() {
        return new Return(
                "RET001",
                "SKU123",
                10,
                "Damaged",
                LocalDateTime.of(2025, 10, 26, 10, 30),
                LocalDate.of(2026, 1, 1)
        );
    }
}