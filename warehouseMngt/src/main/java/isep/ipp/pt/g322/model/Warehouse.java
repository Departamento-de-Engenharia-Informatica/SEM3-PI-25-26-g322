package isep.ipp.pt.g322.model;

import java.util.*;

public class Warehouse {

    private final String id;
    private final Map<String, Aisle> aisles = new HashMap<>();

    public Warehouse(String id) {
        this.id = id;
    }
    public String getId() { return id; }

    public void addBay(String warehouseId, String aisleId, int bayNumber, int capacity) {
        Aisle aisle = aisles.computeIfAbsent(aisleId, Aisle::new);
        aisle.addBay(new BayMeta(warehouseId, aisleId, bayNumber, capacity));
    }

    public Aisle getAisle(String aisleId) {
        return aisles.get(aisleId);
    }

    public List<BayMeta> getAllBays() {
        List<BayMeta> result = new ArrayList<>();
        for (Aisle a : aisles.values()) result.addAll(a.getBays().values());
        return result;
    }

    public BayMeta findFirstAvailableBay() {
        for (BayMeta b : getAllBays())
            if (b.hasCapacity()) return b;
        return null;
    }

    public int getTotalBays() {
        return getAllBays().size();
    }
}