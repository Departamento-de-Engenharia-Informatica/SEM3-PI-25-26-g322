package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.*;

public class WarehouseManager {

    private final Warehouse warehouse;

    public WarehouseManager(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    /** Unloads all boxes from a wagon into the warehouse. */
    public void unloadWagon(Wagon wagon) {
        for (Box box : wagon.getBoxes()) {
            BayMeta bay = warehouse.findFirstAvailableBay();
            if (bay == null)
                throw new IllegalStateException("No available bays in warehouse");
            bay.addBox(box);
        }
    }

    public void printWarehouseState() {
        for (BayMeta bay : warehouse.getAllBays()) {
            System.out.println(bay);
            for (Box box : bay.getBoxes()) {
                System.out.println("   " + box);
            }
        }
    }
}