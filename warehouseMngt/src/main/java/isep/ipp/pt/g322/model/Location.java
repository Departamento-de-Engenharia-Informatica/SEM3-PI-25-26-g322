package isep.ipp.pt.g322.model;

public class Location implements Comparable<Location> {
    private final String warehouseId;
    private final int aisle;
    private final int bay;

    public Location(String warehouseId, int aisle, int bay) {
        this.warehouseId = warehouseId;
        this.aisle = aisle;
        this.bay = bay;
    }

    public String getWarehouseId() { return warehouseId; }
    public int getAisle() { return aisle; }
    public int getBay() { return bay; }

    @Override
    public int compareTo(Location o) {
        int c = warehouseId.compareTo(o.warehouseId);
        if (c != 0) return c;
        c = Integer.compare(aisle, o.aisle);
        if (c != 0) return c;
        return Integer.compare(bay, o.bay);
    }

    @Override
    public String toString() {
        return warehouseId + ":" + aisle + ":" + bay;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Location)) return false;
        Location other = (Location) obj;
        return aisle == other.aisle && bay == other.bay && warehouseId.equals(other.warehouseId);
    }

    @Override
    public int hashCode() {
        int result = warehouseId.hashCode();
        result = 31 * result + aisle;
        result = 31 * result + bay;
        return result;
    }
}
