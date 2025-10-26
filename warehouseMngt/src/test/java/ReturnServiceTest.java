import isep.ipp.pt.g322.model.Box;
import isep.ipp.pt.g322.model.Location;
import isep.ipp.pt.g322.model.Return;
import isep.ipp.pt.g322.model.BayMeta;
import isep.ipp.pt.g322.service.InventoryService;
import isep.ipp.pt.g322.service.ReturnService;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReturnServiceTest {

    private ReturnService returnService;
    private InventoryService mockInventoryService;
    private String testAuditLogPath;
    private Path tempLogFile;

    @BeforeEach
    void setUp() throws IOException {
        mockInventoryService = mock(InventoryService.class);

        tempLogFile = Files.createTempFile("audit_log_test", ".txt");
        testAuditLogPath = tempLogFile.toString();

        returnService = new ReturnService(mockInventoryService, testAuditLogPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempLogFile);
    }

    @Test
    @DisplayName("Should add return to quarantine successfully")
    void testAddToQuarantine_Success() {
        Return returnItem = createReturn("R001", "SKU001", 10, "Changed Mind");

        returnService.addToQuarantine(returnItem);

        assertEquals(1, returnService.getQuarantineSize());
    }

    @Test
    @DisplayName("Should add multiple returns to quarantine")
    void testAddToQuarantine_MultipleItems() {
        Return return1 = createReturn("R001", "SKU001", 10, "Changed Mind");
        Return return2 = createReturn("R002", "SKU002", 5, "Damaged");
        Return return3 = createReturn("R003", "SKU003", 15, "Expired");

        returnService.addToQuarantine(return1);
        returnService.addToQuarantine(return2);
        returnService.addToQuarantine(return3);

        assertEquals(3, returnService.getQuarantineSize());
    }

    @Test
    @DisplayName("Should discard return with 'Damaged' reason")
    void testProcessQuarantine_DiscardDamaged() throws IOException {
        Return damagedReturn = createReturn("R001", "SKU001", 10, "Damaged");
        returnService.addToQuarantine(damagedReturn);

        returnService.processQuarantine();

        verify(mockInventoryService, never()).setBoxLocation(anyString(), any(Location.class));
        verify(mockInventoryService, never()).insertBoxFEFO(any(Box.class));

        String logContent = Files.readString(tempLogFile);
        assertTrue(logContent.contains("Discarded"));
        assertTrue(logContent.contains("returnId=R001"));
        assertTrue(logContent.contains("sku=SKU001"));
        assertTrue(logContent.contains("qty=10"));
    }

    @Test
    @DisplayName("Should discard return with 'Expired' reason")
    void testProcessQuarantine_DiscardExpired() throws IOException {
        Return expiredReturn = createReturn("R002", "SKU002", 5, "Expired");
        returnService.addToQuarantine(expiredReturn);

        returnService.processQuarantine();

        verify(mockInventoryService, never()).setBoxLocation(anyString(), any(Location.class));
        verify(mockInventoryService, never()).insertBoxFEFO(any(Box.class));

        String logContent = Files.readString(tempLogFile);
        assertTrue(logContent.contains("Discarded"));
        assertTrue(logContent.contains("returnId=R002"));
    }

    @Test
    @DisplayName("Should restock return with 'Changed Mind' reason")
    void testProcessQuarantine_RestockChangedMind() throws IOException {
        Return returnItem = createReturn("R003", "SKU003", 15, "Changed Mind");
        Location mockLocation = new Location("Bay", 1, 1);

        setupMockInventoryForRestock(mockLocation);
        returnService.addToQuarantine(returnItem);

        returnService.processQuarantine();

        verify(mockInventoryService, times(1)).setBoxLocation(eq("RET-R003"), eq(mockLocation));

        ArgumentCaptor<Box> boxCaptor = ArgumentCaptor.forClass(Box.class);
        verify(mockInventoryService, times(1)).insertBoxFEFO(boxCaptor.capture());

        Box capturedBox = boxCaptor.getValue();
        assertEquals("RET-R003", capturedBox.getBoxID());
        assertEquals("SKU003", capturedBox.getSKU());
        assertEquals(15, capturedBox.getQuantity());

        String logContent = Files.readString(tempLogFile);
        assertTrue(logContent.contains("Restocked"));
        assertTrue(logContent.contains("returnId=R003"));
    }

    @Test
    @DisplayName("Should restock return with 'Wrong Item' reason")
    void testProcessQuarantine_RestockWrongItem() {
        Return returnItem = createReturn("R004", "SKU004", 20, "Wrong Item");
        Location mockLocation = new Location("Bay", 2, 2);

        setupMockInventoryForRestock(mockLocation);
        returnService.addToQuarantine(returnItem);

        returnService.processQuarantine();

        verify(mockInventoryService, times(1)).setBoxLocation(eq("RET-R004"), any(Location.class));
        verify(mockInventoryService, times(1)).insertBoxFEFO(any(Box.class));
    }

    @Test
    @DisplayName("Should process returns in LIFO order (latest first)")
    void testProcessQuarantine_LIFOOrder() throws IOException {
        LocalDateTime time1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2025, 1, 1, 11, 0);
        LocalDateTime time3 = LocalDateTime.of(2025, 1, 1, 12, 0);

        Return return1 = createReturnWithTimestamp("R001", "SKU001", 10, "Damaged", time1);
        Return return2 = createReturnWithTimestamp("R002", "SKU002", 5, "Damaged", time2);
        Return return3 = createReturnWithTimestamp("R003", "SKU003", 15, "Damaged", time3);

        returnService.addToQuarantine(return1);
        returnService.addToQuarantine(return2);
        returnService.addToQuarantine(return3);

        returnService.processQuarantine();

        String logContent = Files.readString(tempLogFile);
        String[] lines = logContent.split("\n");

        assertTrue(lines[0].contains("returnId=R003"));
        assertTrue(lines[1].contains("returnId=R002"));
        assertTrue(lines[2].contains("returnId=R001"));
    }

    @Test
    @DisplayName("Should use returnId as tiebreaker when timestamps are equal")
    void testProcessQuarantine_SameTimestamp_OrderByReturnId() throws IOException {
        LocalDateTime sameTime = LocalDateTime.of(2025, 1, 1, 10, 0);

        Return return1 = createReturnWithTimestamp("R001", "SKU001", 10, "Damaged", sameTime);
        Return return2 = createReturnWithTimestamp("R003", "SKU003", 15, "Damaged", sameTime);
        Return return3 = createReturnWithTimestamp("R002", "SKU002", 5, "Damaged", sameTime);

        returnService.addToQuarantine(return1);
        returnService.addToQuarantine(return2);
        returnService.addToQuarantine(return3);

        returnService.processQuarantine();

        String logContent = Files.readString(tempLogFile);
        String[] lines = logContent.split("\n");

        assertTrue(lines[0].contains("returnId=R001"));
        assertTrue(lines[1].contains("returnId=R002"));
        assertTrue(lines[2].contains("returnId=R003"));
    }

    @Test
    @DisplayName("Should throw exception when no bay available for restocking")
    void testProcessQuarantine_NoBayAvailable() {
        Return returnItem = createReturn("R007", "SKU007", 10, "Changed Mind");

        InventoryService.InventoryState emptyState = new InventoryService.InventoryState();
        when(mockInventoryService.getState()).thenReturn(emptyState);

        returnService.addToQuarantine(returnItem);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            returnService.processQuarantine();
        });

        assertTrue(exception.getMessage().contains("No available bay with capacity"));
        assertTrue(exception.getMessage().contains("SKU007"));
    }

    @Test
    @DisplayName("Should process mixed returns (some discarded, some restocked)")
    void testProcessQuarantine_MixedReturns() throws IOException {
        Return damaged = createReturn("R008", "SKU008", 5, "Damaged");
        Return expired = createReturn("R009", "SKU009", 10, "Expired");
        Return changedMind = createReturn("R010", "SKU010", 15, "Changed Mind");
        Return wrongItem = createReturn("R011", "SKU011", 20, "Wrong Item");

        Location mockLocation = new Location("Bay", 1, 1);
        setupMockInventoryForRestock(mockLocation);

        returnService.addToQuarantine(damaged);
        returnService.addToQuarantine(expired);
        returnService.addToQuarantine(changedMind);
        returnService.addToQuarantine(wrongItem);

        returnService.processQuarantine();

        verify(mockInventoryService, times(2)).setBoxLocation(anyString(), any(Location.class));
        verify(mockInventoryService, times(2)).insertBoxFEFO(any(Box.class));

        String logContent = Files.readString(tempLogFile);
        assertEquals(2, logContent.split("Discarded").length - 1);
        assertEquals(2, logContent.split("Restocked").length - 1);
    }

    @Test
    @DisplayName("Should return 0 for empty quarantine")
    void testGetQuarantineSize_Empty() {
        assertEquals(0, returnService.getQuarantineSize());
    }

    @Test
    @DisplayName("Should return correct size after processing")
    void testGetQuarantineSize_AfterProcessing() {
        Return return1 = createReturn("R012", "SKU012", 10, "Damaged");
        Return return2 = createReturn("R013", "SKU013", 5, "Damaged");

        returnService.addToQuarantine(return1);
        returnService.addToQuarantine(return2);
        assertEquals(2, returnService.getQuarantineSize());

        returnService.processQuarantine();

        assertEquals(0, returnService.getQuarantineSize());
    }

    @Test
    @DisplayName("Should write audit log with correct format for restocked items")
    void testAuditLog_RestockedFormat() throws IOException {
        Return returnItem = createReturn("R015", "SKU015", 30, "Changed Mind");
        Location mockLocation = new Location("Bay", 1, 1);
        setupMockInventoryForRestock(mockLocation);
        returnService.addToQuarantine(returnItem);

        returnService.processQuarantine();

        String logContent = Files.readString(tempLogFile);
        assertTrue(logContent.contains("returnId=R015"));
        assertTrue(logContent.contains("sku=SKU015"));
        assertTrue(logContent.contains("action=Restocked"));
        assertTrue(logContent.contains("qty=30"));
    }

    @Test
    @DisplayName("Should append to existing audit log file")
    void testAuditLog_AppendMode() throws IOException {
        Files.writeString(tempLogFile, "Existing log entry\n");
        Return returnItem = createReturn("R016", "SKU016", 5, "Damaged");
        returnService.addToQuarantine(returnItem);

        returnService.processQuarantine();

        String logContent = Files.readString(tempLogFile);
        assertTrue(logContent.startsWith("Existing log entry"));
        assertTrue(logContent.contains("returnId=R016"));
    }

    @Test
    @DisplayName("Should create box with correct RET- prefix")
    void testRestockReturn_BoxIdPrefix() {
        Return returnItem = createReturn("R017", "SKU017", 10, "Changed Mind");
        Location mockLocation = new Location("Bay", 1, 1);
        setupMockInventoryForRestock(mockLocation);
        returnService.addToQuarantine(returnItem);

        returnService.processQuarantine();

        ArgumentCaptor<Box> boxCaptor = ArgumentCaptor.forClass(Box.class);
        verify(mockInventoryService).insertBoxFEFO(boxCaptor.capture());

        Box capturedBox = boxCaptor.getValue();
        assertEquals("RET-R017", capturedBox.getBoxID());
    }

    @Test
    @DisplayName("Should preserve return details in new box")
    void testRestockReturn_PreservesDetails() {
        LocalDate expiryDate = LocalDate.of(2025, 12, 31);
        Return returnItem = createReturnWithExpiry("R018", "SKU018", 50, "Changed Mind", expiryDate);
        Location mockLocation = new Location("Bay", 1, 1);
        setupMockInventoryForRestock(mockLocation);
        returnService.addToQuarantine(returnItem);

        returnService.processQuarantine();

        ArgumentCaptor<Box> boxCaptor = ArgumentCaptor.forClass(Box.class);
        verify(mockInventoryService).insertBoxFEFO(boxCaptor.capture());

        Box capturedBox = boxCaptor.getValue();
        assertEquals("SKU018", capturedBox.getSKU());
        assertEquals(50, capturedBox.getQuantity());
        assertEquals(expiryDate, capturedBox.getExpiryDate());
    }

    private Return createReturn(String returnId, String sku, int qty, String reason) {
        return new Return(returnId, sku, qty, reason, LocalDateTime.now(), LocalDate.now());
    }

    private Return createReturnWithTimestamp(String returnId, String sku, int qty,
                                             String reason, LocalDateTime timestamp) {
        return new Return(returnId, sku, qty, reason, timestamp, LocalDate.now());
    }

    private Return createReturnWithExpiry(String returnId, String sku, int qty,
                                          String reason, LocalDate expiryDate) {
        return new Return(returnId, sku, qty, reason, LocalDateTime.now(), expiryDate);
    }

    private void setupMockInventoryForRestock(Location location) {
        Map<String, NavigableMap<Integer, List<Location>>> skuToBays = new HashMap<>();
        Map<Location, BayMeta> bays = new HashMap<>();
        Map<Location, NavigableSet<Box>> bayBoxes = new HashMap<>();

        BayMeta metadata = mock(BayMeta.class);
        when(metadata.getCapacityBoxes()).thenReturn(10);
        bays.put(location, metadata);
        bayBoxes.put(location, new TreeSet<>(InventoryService.FEFO));

        InventoryService.InventoryState state = createInventoryState(skuToBays, bays, bayBoxes);

        when(mockInventoryService.getState()).thenReturn(state);
    }

    private InventoryService.InventoryState createInventoryState(
            Map<String, NavigableMap<Integer, List<Location>>> skuToBays,
            Map<Location, BayMeta> bays,
            Map<Location, NavigableSet<Box>> bayBoxes) {

        try {
            InventoryService.InventoryState state = new InventoryService.InventoryState();

            java.lang.reflect.Field skuToBaysField = InventoryService.InventoryState.class.getDeclaredField("skuToBays");
            skuToBaysField.setAccessible(true);
            skuToBaysField.set(state, skuToBays);

            java.lang.reflect.Field baysField = InventoryService.InventoryState.class.getDeclaredField("bays");
            baysField.setAccessible(true);
            baysField.set(state, bays);

            java.lang.reflect.Field bayBoxesField = InventoryService.InventoryState.class.getDeclaredField("bayBoxes");
            bayBoxesField.setAccessible(true);
            bayBoxesField.set(state, bayBoxes);

            return state;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create InventoryState for testing", e);
        }
    }
}