package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.BayMeta;
import isep.ipp.pt.g322.model.Box;
import isep.ipp.pt.g322.model.Item;
import isep.ipp.pt.g322.model.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class CsvImporter {
    private final InventoryService.InventoryState state;
    private final InventoryService inventory;

    public CsvImporter(InventoryService.InventoryState state) {
        this.state = state;
        this.inventory = new InventoryService(state);
    }

    public void loadItems(Path itemsCsv) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(itemsCsv)) {
            br.readLine(); // skip header
            String line; int ln = 1;
            while ((line = br.readLine()) != null) {
                ln++;
                String[] t = line.split(",", -1);
                if (t.length < 6) { err(itemsCsv, ln, "Not enough columns"); continue; }
                try {
                    Item it = new Item(t[0].trim(), t[1].trim(), t[2].trim(),
                            t[3].trim(),
                            Double.parseDouble(t[4].trim()),
                            Double.parseDouble(t[5].trim()));
                    state.items.put(it.getSku(), it);
                } catch (Exception e) {
                    err(itemsCsv, ln, "Invalid item data: " + e.getMessage());
                }
            }
        }
    }

    public void loadBays(Path baysCsv) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(baysCsv)) {
            br.readLine();
            String line; int ln = 1;
            while ((line = br.readLine()) != null) {
                ln++;
                String[] t = line.split(",", -1);
                if (t.length < 4) { err(baysCsv, ln, "Not enough columns"); continue; }
                try {
                    BayMeta meta = new BayMeta(t[0].trim(),
                            Integer.parseInt(t[1].trim()),
                            Integer.parseInt(t[2].trim()),
                            Integer.parseInt(t[3].trim()));
                    state.bays.put(meta.toLocation(), meta);
                } catch (Exception e) {
                    err(baysCsv, ln, "Invalid bay data: " + e.getMessage());
                }
            }
        }
    }

    public void loadWagons(Path wagonsCsv, Map<String, Location> skuToLocation) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(wagonsCsv)) {
            br.readLine();
            String line; int ln = 1;
            while ((line = br.readLine()) != null) {
                ln++;
                String[] t = line.split(",", -1);
                if (t.length < 6) { err(wagonsCsv, ln, "Not enough columns"); continue; }
                String boxId = t[1].trim();
                String sku = t[2].trim();
                if (!state.items.containsKey(sku)) { err(wagonsCsv, ln, "Unknown SKU " + sku); continue; }
                int qty;
                try { qty = Integer.parseInt(t[3].trim()); }
                catch (Exception e) { err(wagonsCsv, ln, "Invalid qty"); continue; }
                LocalDate expiry = t[4].trim().isEmpty() ? null : LocalDate.parse(t[4].trim());
                LocalDateTime received;
                try { received = LocalDateTime.parse(t[5].trim()); }
                catch (Exception e) { err(wagonsCsv, ln, "Invalid receivedAt"); continue; }
                Box box = new Box(boxId, sku, qty, expiry, received);
                Location loc = skuToLocation.get(sku);
                if (loc == null) { err(wagonsCsv, ln, "No default location for SKU " + sku); continue; }
                inventory.setBoxLocation(boxId, loc);
                try {
                    inventory.insertBoxFEFO(box);
                } catch (Exception e) {
                    err(wagonsCsv, ln, "Insert failed: " + e.getMessage());
                }
            }
        }
    }

    private void err(Path file, int lineNo, String msg) {
        System.err.println("ERR[" + file.getFileName() + ":L" + lineNo + "]: " + msg);
    }
}