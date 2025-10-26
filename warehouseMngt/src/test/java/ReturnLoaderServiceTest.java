import isep.ipp.pt.g322.model.Return;
import isep.ipp.pt.g322.service.ReturnLoaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReturnLoaderServiceTest {

    private ReturnLoaderService loaderService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        loaderService = new ReturnLoaderService();
    }

    @Test
    @DisplayName("Should load valid returns from CSV file")
    void testLoadReturns_ValidFile() throws IOException {
        File testFile = createTestFile("valid_returns.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET001,SKU123,10,Damaged,2025-10-26T10:30:00,2026-01-01\n" +
                        "RET002,SKU456,5,Wrong item,2025-10-26T11:00:00,2026-02-15\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(2, returns.size());

        Return ret1 = returns.get(0);
        assertEquals("RET001", ret1.getReturnId());
        assertEquals("SKU123", ret1.getSku());
        assertEquals(10, ret1.getQty());
        assertEquals("Damaged", ret1.getReason());
        assertEquals(LocalDateTime.of(2025, 10, 26, 10, 30, 0), ret1.getTimestamp());
        assertEquals(LocalDate.of(2026, 1, 1), ret1.getExpiryDate());

        Return ret2 = returns.get(1);
        assertEquals("RET002", ret2.getReturnId());
        assertEquals("SKU456", ret2.getSku());
        assertEquals(5, ret2.getQty());
    }

    @Test
    @DisplayName("Should load return with null expiry date")
    void testLoadReturns_NullExpiryDate() throws IOException {
        File testFile = createTestFile("returns_no_expiry.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET003,SKU789,15,Customer request,2025-10-26T12:00:00,\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        Return ret = returns.get(0);
        assertNull(ret.getExpiryDate());
        assertEquals("RET003", ret.getReturnId());
    }

    @Test
    @DisplayName("Should load return without expiry date field")
    void testLoadReturns_MissingExpiryDateField() throws IOException {
        File testFile = createTestFile("returns_missing_expiry.csv",
                "returnId,sku,qty,reason,timestamp\n" +
                        "RET004,SKU999,20,Expired,2025-10-26T13:00:00\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        Return ret = returns.get(0);
        assertNull(ret.getExpiryDate());
        assertEquals("RET004", ret.getReturnId());
    }

    @Test
    @DisplayName("Should handle invalid expiry date gracefully")
    void testLoadReturns_InvalidExpiryDate() throws IOException {
        File testFile = createTestFile("returns_invalid_expiry.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET005,SKU111,8,Damaged,2025-10-26T14:00:00,invalid-date\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        Return ret = returns.get(0);
        assertNull(ret.getExpiryDate());
        assertEquals("RET005", ret.getReturnId());
    }

    @Test
    @DisplayName("Should skip lines with missing required fields")
    void testLoadReturns_MissingRequiredFields() throws IOException {
        File testFile = createTestFile("returns_missing_fields.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET006,SKU222\n" +
                        "RET007,SKU333,12,Damaged,2025-10-26T15:00:00,2026-03-01\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        assertEquals("RET007", returns.getFirst().getReturnId());
    }

    @Test
    @DisplayName("Should skip line with invalid quantity format")
    void testLoadReturns_InvalidQuantityFormat() throws IOException {
        File testFile = createTestFile("returns_invalid_qty.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET008,SKU444,abc,Damaged,2025-10-26T16:00:00,2026-04-01\n" +
                        "RET009,SKU555,25,Wrong item,2025-10-26T17:00:00,2026-05-01\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        assertEquals("RET009", returns.getFirst().getReturnId());
    }

    @Test
    @DisplayName("Should skip line with zero quantity")
    void testLoadReturns_ZeroQuantity() throws IOException {
        File testFile = createTestFile("returns_zero_qty.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET010,SKU666,0,Damaged,2025-10-26T18:00:00,2026-06-01\n" +
                        "RET011,SKU777,30,Wrong item,2025-10-26T19:00:00,2026-07-01\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        assertEquals("RET011", returns.getFirst().getReturnId());
    }

    @Test
    @DisplayName("Should skip line with negative quantity")
    void testLoadReturns_NegativeQuantity() throws IOException {
        File testFile = createTestFile("returns_negative_qty.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET012,SKU888,-5,Damaged,2025-10-26T20:00:00,2026-08-01\n" +
                        "RET013,SKU999,35,Customer request,2025-10-26T21:00:00,2026-09-01\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        assertEquals("RET013", returns.getFirst().getReturnId());
    }

    @Test
    @DisplayName("Should skip line with invalid timestamp format")
    void testLoadReturns_InvalidTimestamp() throws IOException {
        File testFile = createTestFile("returns_invalid_timestamp.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET014,SKU100,40,Damaged,invalid-timestamp,2026-10-01\n" +
                        "RET015,SKU200,45,Wrong item,2025-10-26T22:00:00,2026-11-01\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        assertEquals("RET015", returns.getFirst().getReturnId());
    }

    @Test
    @DisplayName("Should handle empty file with only header")
    void testLoadReturns_EmptyFile() throws IOException {
        File testFile = createTestFile("returns_empty.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertTrue(returns.isEmpty());
    }

    @Test
    @DisplayName("Should handle file with whitespace in values")
    void testLoadReturns_WhitespaceInValues() throws IOException {
        File testFile = createTestFile("returns_whitespace.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        " RET016 , SKU300 , 50 , Damaged , 2025-10-26T23:00:00 , 2026-12-01 \n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        Return ret = returns.getFirst();
        assertEquals("RET016", ret.getReturnId());
        assertEquals("SKU300", ret.getSku());
        assertEquals(50, ret.getQty());
        assertEquals("Damaged", ret.getReason());
    }

    @Test
    @DisplayName("Should handle non-existent file")
    void testLoadReturns_NonExistentFile() {
        List<Return> returns = loaderService.loadReturns("/non/existent/file.csv");

        assertTrue(returns.isEmpty());
    }

    @Test
    @DisplayName("Should load multiple returns with various reasons")
    void testLoadReturns_VariousReasons() throws IOException {
        File testFile = createTestFile("returns_various.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET017,SKU400,10,Damaged,2025-10-26T10:00:00,2026-01-15\n" +
                        "RET018,SKU500,20,Expired,2025-10-26T11:00:00,2026-02-15\n" +
                        "RET019,SKU600,30,Wrong item,2025-10-26T12:00:00,2026-03-15\n" +
                        "RET020,SKU700,40,Customer request,2025-10-26T13:00:00,2026-04-15\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(4, returns.size());
        assertEquals("Damaged", returns.get(0).getReason());
        assertEquals("Expired", returns.get(1).getReason());
        assertEquals("Wrong item", returns.get(2).getReason());
        assertEquals("Customer request", returns.get(3).getReason());
    }

    @Test
    @DisplayName("Should handle mixed valid and invalid lines")
    void testLoadReturns_MixedValidInvalid() throws IOException {
        File testFile = createTestFile("returns_mixed.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET021,SKU800,55,Damaged,2025-10-26T14:00:00,2026-05-15\n" +
                        "RET022,SKU900,invalid,Wrong item,2025-10-26T15:00:00,2026-06-15\n" +
                        "RET023,SKU1000,60,Customer request,2025-10-26T16:00:00,2026-07-15\n" +
                        "RET024,SKU1100\n" +
                        "RET025,SKU1200,65,Expired,2025-10-26T17:00:00,2026-08-15\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(3, returns.size());
        assertEquals("RET021", returns.get(0).getReturnId());
        assertEquals("RET023", returns.get(1).getReturnId());
        assertEquals("RET025", returns.get(2).getReturnId());
    }

    @Test
    @DisplayName("Should preserve data types correctly")
    void testLoadReturns_DataTypes() throws IOException {
        File testFile = createTestFile("returns_datatypes.csv",
                "returnId,sku,qty,reason,timestamp,expiryDate\n" +
                        "RET026,SKU1300,999,Damaged,2025-10-26T23:59:59,2026-12-31\n"
        );

        List<Return> returns = loaderService.loadReturns(testFile.getAbsolutePath());

        assertEquals(1, returns.size());
        Return ret = returns.getFirst();

        assertInstanceOf(String.class, ret.getReturnId());
        assertInstanceOf(String.class, ret.getSku());
        assertInstanceOf(Integer.class, ret.getQty());
        assertInstanceOf(String.class, ret.getReason());
        assertInstanceOf(LocalDateTime.class, ret.getTimestamp());
        assertInstanceOf(LocalDate.class, ret.getExpiryDate());
    }

    private File createTestFile(String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }
}