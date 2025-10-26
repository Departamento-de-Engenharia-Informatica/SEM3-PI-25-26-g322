package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.BayMeta;
import isep.ipp.pt.g322.model.Location;
import isep.ipp.pt.g322.model.Box;
import isep.ipp.pt.g322.model.Item;

import java.util.*;

public class InventoryService {

    public static class InventoryState {
        public final Map<Location, BayMeta> bays = new HashMap<>();
        public final Map<Location, NavigableSet<isep.ipp.pt.g322.model.Box>> bayBoxes = new HashMap<>();
        public final Map<String, Box> boxById = new HashMap<>();
        public final Map<String, Item> items = new HashMap<>();
        public final Map<String, NavigableMap<Integer, List<Location>>> skuToBays = new HashMap<>();
    }

    public static final Comparator<Box> FEFO = Comparator
            .comparing((Box b) -> b.getExpiryDate(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Box::getReceivedAt)
            .thenComparing(Box::getBoxID);

    private final InventoryState state;
    private final Map<String, Location> boxLocationOverride = new HashMap<>();

    public InventoryService(InventoryState state) {
        this.state = state;
    }

    public void setBoxLocation(String boxId, Location loc) {
        boxLocationOverride.put(boxId, loc);
    }

    private Location boxLocation(Box b) {
        return boxLocationOverride.get(b.getBoxID());
    }

    // ---------- USEI01 operations ----------

    public void insertBoxFEFO(Box box) {
        Location loc = boxLocation(box);
        BayMeta meta = state.bays.get(loc);
        if (meta == null) throw new IllegalArgumentException("ERR: Bay not found " + loc);
        NavigableSet<Box> set = state.bayBoxes.computeIfAbsent(loc, k -> new TreeSet<>(FEFO));
        if (set.size() >= meta.getCapacityBoxes())
            throw new IllegalStateException("ERR: Bay capacity exceeded at " + loc);
        if (state.boxById.containsKey(box.getBoxID()))
            throw new IllegalStateException("ERR: Duplicate boxId " + box.getBoxID());
        set.add(box);
        state.boxById.put(box.getBoxID(), box);
        state.skuToBays.computeIfAbsent(box.getSKU(), k -> new TreeMap<>())
                .computeIfAbsent(loc.getBay(), k -> new ArrayList<>())
                .add(loc);
    }

    public int dispatch(String sku, int qtyReq) {
        int remaining = qtyReq;
        NavigableMap<Integer, List<Location>> map = state.skuToBays.getOrDefault(sku, new TreeMap<>());
        for (var entry : map.entrySet()) {
            for (Location loc : entry.getValue()) {
                NavigableSet<Box> set = state.bayBoxes.getOrDefault(loc, new TreeSet<>(FEFO));
                while (!set.isEmpty() && remaining > 0) {
                    Box first = set.pollFirst(); // frente
                    int take = Math.min(first.getQuantity(), remaining);
                    first.setQuantity(first.getQuantity() - take);
                    remaining -= take;
                    if (first.getQuantity() > 0) {
                        set.add(first);
                    } else {
                        state.boxById.remove(first.getBoxID());
                    }
                }
                if (remaining == 0) return qtyReq;
            }
        }
        return qtyReq - remaining;
    }

    public void relocate(String boxId, Location newLoc) {
        Box b = state.boxById.get(boxId);
        if (b == null) throw new IllegalArgumentException("ERR: boxId not found " + boxId);

        Location oldLoc = boxLocationOverride.get(boxId);
        if (oldLoc == null) throw new IllegalStateException("ERR: old location unknown for " + boxId);
        if (newLoc.equals(oldLoc)) return; // nada a fazer

        // 1) Validar existência e capacidade do novo bay ANTES de mexer
        BayMeta newMeta = state.bays.get(newLoc);
        if (newMeta == null) throw new IllegalArgumentException("ERR: Bay not found " + newLoc);
        NavigableSet<Box> newSet = state.bayBoxes.computeIfAbsent(newLoc, k -> new TreeSet<>(FEFO));
        if (newSet.size() >= newMeta.getCapacityBoxes())
            throw new IllegalStateException("ERR: Bay capacity exceeded at " + newLoc);

        // 2) Remover do bay antigo (se existir)
        NavigableSet<Box> oldSet = state.bayBoxes.get(oldLoc);
        if (oldSet != null) oldSet.remove(b);

        // 3) Atualizar índice sku->bays (remover oldLoc)
        NavigableMap<Integer, List<Location>> map = state.skuToBays.get(b.getSKU());
        if (map != null) {
            List<Location> lst = map.get(oldLoc.getBay());
            if (lst != null) {
                lst.remove(oldLoc);
                if (lst.isEmpty()) map.remove(oldLoc.getBay());
            }
        }

        // 4) Atualizar localização e re-inserir no novo bay na posição FEFO
        setBoxLocation(boxId, newLoc);

        // evitar "duplicate boxId": vamos reusar a mesma Box; remover temporariamente do índice
        state.boxById.remove(boxId);
        insertBoxFEFO(b);           // volta a inserir e repõe boxById + skuToBays (para o novo bay)
    }

    //US05

    public InventoryService.InventoryState getState() {
        return state;
    }

    public Location findAvailableLocationForSKU(String sku) {
        NavigableMap<Integer, List<Location>> skuBays = state.skuToBays.get(sku);
        if (skuBays != null) {
            for (Map.Entry<Integer, List<Location>> entry : skuBays.entrySet()) {
                for (Location loc : entry.getValue()) {
                    BayMeta meta = state.bays.get(loc);
                    NavigableSet<Box> boxes = state.bayBoxes.getOrDefault(loc, new TreeSet<>(FEFO));
                    if (boxes.size() < meta.getCapacityBoxes()) {
                        return loc;
                    }
                }
            }
        }

        for (Map.Entry<Location, BayMeta> entry : state.bays.entrySet()) {
            Location location = entry.getKey();
            BayMeta bayMeta = entry.getValue();
            NavigableSet<Box> boxes = state.bayBoxes.getOrDefault(location, new TreeSet<>(FEFO));
            if (boxes.size() < bayMeta.getCapacityBoxes()) {
                return location;
            }
        }

        throw new IllegalStateException("No available bay with capacity for SKU: " + sku);
    }
}