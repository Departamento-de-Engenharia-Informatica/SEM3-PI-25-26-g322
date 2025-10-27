package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.BayMeta;
import isep.ipp.pt.g322.model.Location;
import isep.ipp.pt.g322.model.Box;
import isep.ipp.pt.g322.model.Item;

import java.util.*;

public class InventoryService {

    // -------------------- STATE --------------------
    public static class InventoryState {
        public final Map<Location, BayMeta> bays = new HashMap<>();
        public final Map<Location, NavigableSet<Box>> bayBoxes = new HashMap<>();
        public final Map<String, Box> boxById = new HashMap<>();
        public final Map<String, Item> items = new HashMap<>();
        // antes: NavigableMap<Integer, List<Location>>
        public final Map<String, SortedMap<Integer, Set<Location>>> skuToBays = new HashMap<>();
    }


    // FEFO: expiry asc (nulls last) -> receivedAt asc -> boxId asc
    public static final Comparator<Box> FEFO = Comparator
            .comparing(Box::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Box::getReceivedAt)
            .thenComparing(Box::getBoxID);

    private final InventoryState state;
    private final Map<String, Location> boxLocationOverride = new HashMap<>();

    public InventoryService(InventoryState state) {
        this.state = state;
    }

    public InventoryState getState() { return state; }

    // -------------------- LOCATION API --------------------
    public void setBoxLocation(String boxId, Location loc) {
        boxLocationOverride.put(boxId, loc);
    }

    private Location boxLocation(Box b) {
        return boxLocationOverride.get(b.getBoxID());
    }

    // -------------------- USEI01 ops --------------------

    /**
     * Insere a box no bay da sua localização (set por setBoxLocation) respeitando FEFO.
     * Valida capacidade e dupes. Atualiza índices (boxById, skuToBays).
     */
    public void insertBoxFEFO(Box box) {
        Location loc = boxLocation(box);
        if (loc == null) throw new IllegalStateException("ERR: location not set for " + box.getBoxID());

        BayMeta meta = state.bays.get(loc);
        if (meta == null) throw new IllegalArgumentException("ERR: Bay not found " + loc);

        NavigableSet<Box> set = state.bayBoxes.computeIfAbsent(loc, k -> new TreeSet<>(FEFO));
        if (set.size() >= meta.getCapacityBoxes())
            throw new IllegalStateException("ERR: Bay capacity exceeded at " + loc);

        if (state.boxById.containsKey(box.getBoxID()))
            throw new IllegalStateException("ERR: Duplicate boxId " + box.getBoxID());

        set.add(box);
        state.boxById.put(box.getBoxID(), box);

        // atualizar índice SKU -> bays (dedup por bay/location)
        SortedMap<Integer, Set<Location>> byBay =
                state.skuToBays.computeIfAbsent(box.getSKU(), k -> new TreeMap<>());
        Set<Location> locs = byBay.computeIfAbsent(loc.getBay(), k -> new LinkedHashSet<>());
        locs.add(loc);
    }

    /**
     * Despacha quantidade de uma SKU em FEFO, atravessando vários bays por ordem do nº do bay.
     * Remove caixas vazias mas não remove bays.
     * @return quantidade efetivamente despachada
     */
    public int dispatch(String sku, int qtyReq) {
        int remaining = qtyReq;
        SortedMap<Integer, Set<Location>> byBay = state.skuToBays.get(sku);
        if (byBay == null || byBay.isEmpty()) return 0;

        for (Integer bayNo : byBay.keySet()) {                 // ordem crescente do nº do bay
            for (Location loc : byBay.get(bayNo)) {
                NavigableSet<Box> set = state.bayBoxes.get(loc);
                if (set == null || set.isEmpty()) continue;

                while (remaining > 0 && !set.isEmpty()) {
                    Box first = set.first();                   // FEFO
                    int take = Math.min(first.getQuantity(), remaining);
                    first.setQuantity(first.getQuantity() - take);
                    remaining -= take;

                    if (first.getQuantity() == 0) {
                        set.pollFirst();                       // remove do bay
                        state.boxById.remove(first.getBoxID()); // remove índice global
                    } else {
                        // quantidade não afeta a ordenação (FEFO não usa quantity)
                        break;
                    }
                }
                if (remaining == 0) break;
            }
            if (remaining == 0) break;
        }
        return qtyReq - remaining;
    }

    /**
     * Reloca uma box para um novo bay.
     * Mantém a ordem FEFO, não reordena as restantes boxes do bay.
     */
    public void relocate(String boxId, Location newLoc) {
        Box b = state.boxById.get(boxId);
        if (b == null) throw new IllegalArgumentException("ERR: boxId not found " + boxId);

        Location oldLoc = boxLocationOverride.get(boxId);
        if (oldLoc == null) throw new IllegalStateException("ERR: old location unknown for " + boxId);
        if (newLoc.equals(oldLoc)) return; // nada a fazer

        // 1) Validar novo bay
        BayMeta newMeta = state.bays.get(newLoc);
        if (newMeta == null) throw new IllegalArgumentException("ERR: Bay not found " + newLoc);
        NavigableSet<Box> newSet = state.bayBoxes.computeIfAbsent(newLoc, k -> new TreeSet<>(FEFO));
        if (newSet.size() >= newMeta.getCapacityBoxes())
            throw new IllegalStateException("ERR: Bay capacity exceeded at " + newLoc);

        // 2) Remover do bay antigo
        NavigableSet<Box> oldSet = state.bayBoxes.get(oldLoc);
        if (oldSet != null) oldSet.remove(b);

        // 3) Atualizar índice sku->bays (remover oldLoc)
        SortedMap<Integer, Set<Location>> map = state.skuToBays.get(b.getSKU());
        if (map != null) {
            Set<Location> lst = map.get(oldLoc.getBay());
            if (lst != null) {
                lst.remove(oldLoc);
                if (lst.isEmpty()) map.remove(oldLoc.getBay());
            }
        }

        // 4) Atualizar localização e re-inserir no novo bay na posição FEFO
        setBoxLocation(boxId, newLoc);

        // evitar "duplicate boxId": retirar temporariamente e voltar a inserir via serviço
        state.boxById.remove(boxId);
        insertBoxFEFO(b);
    }

    /**
     * Procura um local com vaga para uma SKU.
     * 1) Tenta nos bays onde a SKU já existe (por nº de bay ascendente), saltando bays cheios.
     * 2) Se todos cheios, procura o próximo bay disponível no MESMO aisle.
     * 3) Caso a SKU ainda não exista, devolve o primeiro bay livre global (ordem natural).
     */
    public Location findAvailableLocationForSKU(String sku) {
        // 1) Se já existe SKU, tentar primeiro bays dela que ainda tenham espaço
        SortedMap<Integer, ? extends Collection<Location>> map = state.skuToBays.get(sku);


        if (map != null) {
            for (Collection<Location> locs : map.values()) {
                for (Location loc : locs) {
                    BayMeta meta = state.bays.get(loc);
                    NavigableSet<Box> boxes = state.bayBoxes.getOrDefault(loc, new TreeSet<>(FEFO));
                    if (meta != null && boxes.size() < meta.getCapacityBoxes()) {
                        return loc;
                    }
                }
            }

        }

        // 2) Caso contrário, procurar QUALQUER bay vazio
        for (Map.Entry<Location, BayMeta> entry : state.bays.entrySet()) {
            Location loc = entry.getKey();
            BayMeta meta = entry.getValue();
            NavigableSet<Box> boxes = state.bayBoxes.getOrDefault(loc, new TreeSet<>(FEFO));
            if (meta != null && boxes.size() < meta.getCapacityBoxes()) {
                return loc; // encontrou bay global livre
            }
        }

        // 3) Se nada encontrado -> exceção
        throw new IllegalStateException("No available bay with capacity for SKU: " + sku);
    }


}
