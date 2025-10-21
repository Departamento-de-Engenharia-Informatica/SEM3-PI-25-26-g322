package isep.ipp.pt.g322.model;

public class BayMeta {
    private final String warehouseId;
    private final int aisle;
    private final int bay;
    private final int capacityBoxes;

    public BayMeta(String warehouseId, int aisle, int bay, int capacityBoxes) {
        this.warehouseId = warehouseId;
        this.aisle = aisle;
        this.bay = bay;
        this.capacityBoxes = capacityBoxes;
    }

    public String getWarehouseId() { return warehouseId; }
    public int getAisle() { return aisle; }
    public int getBay() { return bay; }
    public int getCapacityBoxes() { return capacityBoxes; }

    public Location toLocation() {
        return new Location(warehouseId, aisle, bay);
    }
}