package isep.ipp.pt.g322.model;

import java.util.*;

public class Aisle {

    private final String id;
    private final Map<Integer, BayMeta> bays = new TreeMap<>();

    public Aisle(String id) { this.id = id; }

    public String getId() { return id; }

    public Map<Integer, BayMeta> getBays() { return bays; }

    public void addBay(BayMeta bay) {
        if (bays.containsKey(bay.getBayNumber()))
            throw new IllegalArgumentException("Duplicate bay " + bay.getBayNumber() + " in aisle " + id);
        bays.put(bay.getBayNumber(), bay);
    }
}
