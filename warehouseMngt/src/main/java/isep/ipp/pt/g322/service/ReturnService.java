package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.Box;
import isep.ipp.pt.g322.model.Location;
import isep.ipp.pt.g322.model.Return;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ReturnService {

    // quarantine storage - processes latest returns first
    private final PriorityQueue<Return> quarantine;
    private final InventoryService inventoryService;
    private final String auditLogPath;

    // comparator: timestamp DESC, then returnId ASC
    private static final Comparator<Return> LIFO_COMPARATOR =
            Comparator.comparing(Return::getTimestamp).reversed()
                    .thenComparing(Return::getReturnId);

    public ReturnService(InventoryService inventoryService, String auditLogPath) {
        this.quarantine = new PriorityQueue<>(LIFO_COMPARATOR);
        this.inventoryService = inventoryService;
        this.auditLogPath = auditLogPath;
    }

    /**
     * Add a return to quarantine
     */
    public void addToQuarantine(Return returnItem) {
        quarantine.offer(returnItem);
    }

    /**
     * Process all items in quarantine
     */
    public void processQuarantine() {
        while (!quarantine.isEmpty()) {
            Return ret = quarantine.poll(); // Get latest return

            if (shouldDiscard(ret)) {
                logAction(ret, "Discarded", ret.getQty(), 0);
            } else {
                restockReturn(ret);
                logAction(ret, "Restocked", ret.getQty(), ret.getQty());
            }
        }
    }

    /**
     * Determine if item should be discarded based on reason
     */
    private boolean shouldDiscard(Return ret) {
        String reason = ret.getReason();
        return "Damaged".equals(reason) || "Expired".equals(reason);
    }

    /**
     * Restock a return back into inventory
     */
    private void restockReturn(Return ret) {
        String newBoxId = "RET-" + ret.getReturnId();

        Box newBox = new Box(
                newBoxId,
                ret.getSku(),
                ret.getQty(),
                ret.getExpiryDate(),
                LocalDateTime.now()
        );

        Location targetLocation = findLocationForSKU(ret.getSku());

        inventoryService.setBoxLocation(newBoxId, targetLocation);
        inventoryService.insertBoxFEFO(newBox);
    }

    private Location findLocationForSKU(String sku) {
        InventoryService.InventoryState state = inventoryService.getState();

        // in case there's a bay with this sku - prio 1
        var skuBays = state.skuToBays.get(sku);
        if (skuBays != null) {
            for (var entry : skuBays.entrySet()) {
                for (Location location : entry.getValue()) {
                    if (hasCapacity(state, location)) {
                        return location;
                    }
                }
            }
        }

        // if not, in case there's a bay with space
        for (var entry : state.bays.entrySet()) {
            Location location = entry.getKey();
            if (hasCapacity(state, location)) {
                return location;
            }
        }

        // thrown exception
        throw new IllegalStateException(
                "ERR: No available bay with capacity for SKU: " + sku
        );
    }

    private boolean hasCapacity(InventoryService.InventoryState state, Location loc) {
        var meta = state.bays.get(loc);
        if (meta == null) return false;

        var boxes = state.bayBoxes.get(loc);
        int currentBoxCount = (boxes != null) ? boxes.size() : 0;

        return currentBoxCount < meta.getCapacityBoxes();
    }

    /**
     * Write audit log entry
     */
    private void logAction(Return ret, String action, int qty, int qtyRestocked) {
        try (PrintWriter log = new PrintWriter(new FileWriter(auditLogPath, true))) {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            if (qtyRestocked > 0 && qtyRestocked < qty) {
                log.printf("%s | returnId=%s | sku=%s | action=%s | qty=%d | qtyRestocked=%d | qtyDiscarded=%d%n",
                        timestamp, ret.getReturnId(), ret.getSku(), action,
                        qty, qtyRestocked, qty - qtyRestocked);
            } else {
                log.printf("%s | returnId=%s | sku=%s | action=%s | qty=%d%n",
                        timestamp, ret.getReturnId(), ret.getSku(), action, qty);
            }

        } catch (IOException e) {
            System.err.println("ERROR: Failed to write audit log: " + e.getMessage());
        }
    }

    /**
     * Get number of items waiting in quarantine
     */
    public int getQuarantineSize() {
        return quarantine.size();
    }
}