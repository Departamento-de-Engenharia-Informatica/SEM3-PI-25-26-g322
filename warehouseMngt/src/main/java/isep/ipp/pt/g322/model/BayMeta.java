package isep.ipp.pt.g322.model;

import java.util.*;


public class BayMeta {
   private final String warehouseId;
    private final String aisleId;
    private final int bayNumber;
    private final int capacityBoxes;
    private final List<Box> boxes = new ArrayList<>();

    public BayMeta(String warehouseId, String aisleId, int bayNumber, int capacityBoxes) {
        this.warehouseId = warehouseId;
        this.aisleId = aisleId;
        this.bayNumber = bayNumber;
        this.capacityBoxes = capacityBoxes;
    }

    public int getBayNumber() { return bayNumber; }
    public String getAisleId() { return aisleId; }
    public String getWarehouseId() { return warehouseId; }
    public int getCapacityBoxes() { return capacityBoxes; }

    public List<Box> getBoxes() { return boxes; }

    public boolean hasCapacity() {
        return boxes.size() < capacityBoxes;
    }

    /** Adds box in FEFO/FIFO order. */
    public void addBox(Box box) {
        if (!hasCapacity())
            throw new IllegalStateException("Bay full (" + warehouseId + "/" + aisleId + "/" + bayNumber + ")");
        boxes.add(box);
        Collections.sort(boxes);
    }

    public Box removeFrontBox() {
        return boxes.isEmpty() ? null : boxes.remove(0);
    }

    @Override
    public String toString() {
        return String.format("Bay[%s-%s-%d] (%d/%d boxes)", warehouseId, aisleId, bayNumber, boxes.size(), capacityBoxes);
    }

    public boolean hasCapacityForSKU(String sku) {
        return boxes.size() < capacityBoxes; 
    }
}
