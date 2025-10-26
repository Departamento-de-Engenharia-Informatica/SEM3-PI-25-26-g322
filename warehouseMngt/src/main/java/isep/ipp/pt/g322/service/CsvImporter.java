package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Responsible for importing and validating data from CSV files:
 *  - items.csv  -> Item objects
 *  - bays.csv   -> Warehouse + Bays
 *  - wagons.csv -> Wagon + Box objects
 */
public class CsvImporter {
    private final InventoryService.InventoryState state;

    public CsvImporter(InventoryService.InventoryState state) {
        this.state = state;
    }

    /** Loads all items from items.csv into a map keyed by SKU. */
    public void loadItems(String filename) throws ValidationException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine(); // skip header
            if (header == null)
                throw new ValidationException("items.csv is empty");

            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                String[] f = line.split(",");
                if (f.length != 6)
                    throw new ValidationException("items.csv line " + lineNo + ": invalid column count");

                String sku = f[0].trim();
                if (sku.isEmpty())
                    throw new ValidationException("items.csv line " + lineNo + ": empty SKU");
                if (state.items.containsKey(sku))
                    throw new ValidationException("items.csv line " + lineNo + ": duplicate SKU " + sku);

                String name = f[1].trim();
                String category = f[2].trim();
                String unit = f[3].trim();
                if (name.isEmpty() || category.isEmpty() || unit.isEmpty())
                    throw new ValidationException("items.csv line " + lineNo + ": missing mandatory fields");

                double volume = Double.parseDouble(f[4]);
                double unitWeight = Double.parseDouble(f[5]);
                if (volume <= 0 || unitWeight <= 0)
                    throw new ValidationException("items.csv line " + lineNo + ": non-positive volume/weight");

                Item item = new Item(sku, name, category, unit, volume, unitWeight);
                state.items.put(sku, item);
            }
        } catch (IOException e) {
            throw new ValidationException("Error reading items.csv: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid numeric value in items.csv: " + e.getMessage(), e);
        }
    }

    /** Loads bays.csv and constructs a Warehouse with its Bays. */
    public void loadWarehouse(String filename) throws ValidationException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null)
                throw new ValidationException("bays.csv is empty");

            String line;
            int lineNo = 1;
            Set<String> bayKeys = new HashSet<>();

            while ((line = br.readLine()) != null) {
                lineNo++;
                String[] f = line.split("[,;\\t]+");
                if (f.length != 4)
                    throw new ValidationException("bays.csv line " + lineNo + ": invalid column count");

                String warehouseId = f[0].trim();
                String aisleId = f[1].trim();
                int bayNumber = Integer.parseInt(f[2].trim());
                int capacity = Integer.parseInt(f[3].trim());
                if (capacity <= 0)
                    throw new ValidationException("bays.csv line " + lineNo + ": capacityBoxes must be positive");

                String key = warehouseId + "_" + aisleId + "_" + bayNumber;
                if (!bayKeys.add(key))
                    throw new ValidationException("bays.csv line " + lineNo + ": duplicate bay definition");

                // Add to state.bays
                int aisleNum = Integer.parseInt(aisleId);
                Location loc = new Location(warehouseId, aisleNum, bayNumber);
                BayMeta meta = new BayMeta(warehouseId, aisleNum, bayNumber, capacity);
                state.bays.put(loc, meta);
            }
        } catch (IOException e) {
            throw new ValidationException("Error reading bays.csv: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid numeric value in bays.csv: " + e.getMessage(), e);
        }
    }

    /** Loads wagons.csv and groups boxes under Wagon objects. */
    public void loadWagons(String filename) throws ValidationException {
        Set<String> boxIds = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null)
                throw new ValidationException("wagons.csv is empty");

            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                String[] f = line.split(",");
                if (f.length != 6)
                    throw new ValidationException("wagons.csv line " + lineNo + ": invalid column count");

                String wagonId = f[0].trim();
                String boxId = f[1].trim();
                String sku = f[2].trim();
                int qty = Integer.parseInt(f[3].trim());
                String expiryStr = f[4].trim();
                String receivedStr = f[5].trim();

                // --- Validation ---
                if (wagonId.isEmpty() || boxId.isEmpty() || sku.isEmpty())
                    throw new ValidationException("wagons.csv line " + lineNo + ": missing required field");
                if (!state.items.containsKey(sku))
                    throw new ValidationException("wagons.csv line " + lineNo + ": unknown SKU " + sku);
                if (qty <= 0)
                    throw new ValidationException("wagons.csv line " + lineNo + ": invalid quantity " + qty);
                if (!boxIds.add(boxId))
                    throw new ValidationException("wagons.csv line " + lineNo + ": duplicate boxId " + boxId);

                LocalDate expiryDate = null;
                if (!expiryStr.isEmpty()) {
                    try {
                        expiryDate = LocalDate.parse(expiryStr);
                    } catch (DateTimeParseException e) {
                        throw new ValidationException("wagons.csv line " + lineNo + ": invalid expiryDate format");
                    }
                }

                LocalDateTime receivedAt;
                try {
                    receivedAt = LocalDateTime.parse(receivedStr);
                } catch (DateTimeParseException e) {
                    throw new ValidationException("wagons.csv line " + lineNo + ": invalid receivedAt timestamp");
                }

                // --- Object creation ---
                Box box = new Box(boxId, sku, qty, expiryDate, receivedAt);
                // Find a bay with capacity for this SKU
                Location loc = null;
                for (Map.Entry<Location, BayMeta> entry : state.bays.entrySet()) {
                    BayMeta meta = entry.getValue();
                    Set<Box> boxes = state.bayBoxes.computeIfAbsent(entry.getKey(), k -> new TreeSet<>(InventoryService.FEFO));
                    if (boxes.size() < meta.getCapacityBoxes()) {
                        loc = entry.getKey();
                        break;
                    }
                }
                if (loc == null) {
                    throw new ValidationException("No available bay with capacity for boxId: " + boxId);
                }
                state.bayBoxes.get(loc).add(box);
                state.boxById.put(boxId, box);
                state.skuToBays.computeIfAbsent(sku, k -> new TreeMap<>())
                        .computeIfAbsent(loc.getBay(), k -> new ArrayList<>())
                        .add(loc);
            }

        } catch (IOException e) {
            throw new ValidationException("Error reading wagons.csv: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid numeric value in wagons.csv: " + e.getMessage(), e);
        }
    }

    /** Loads orders.csv and creates Order objects. */
    public List<Order> loadOrders(String filename) throws ValidationException {
        List<Order> orders = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null)
                throw new ValidationException("orders.csv is empty");

            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                String[] f = line.split(",");
                if (f.length != 3)
                    throw new ValidationException("orders.csv line " + lineNo + ": invalid column count");

                String orderId = f[0].trim();
                String dueDateStr = f[1].trim();
                String priorityStr = f[2].trim();

                if (orderId.isEmpty() || dueDateStr.isEmpty() || priorityStr.isEmpty())
                    throw new ValidationException("orders.csv line " + lineNo + ": missing mandatory fields");

                LocalDateTime dueDate;
                try {
                    dueDate = LocalDateTime.parse(dueDateStr);
                } catch (DateTimeParseException e) {
                    throw new ValidationException("orders.csv line " + lineNo + ": invalid dueDate format (expected yyyy-MM-ddTHH:mm:ss)");
                }

                int priority;
                try {
                    priority = Integer.parseInt(priorityStr);
                } catch (NumberFormatException e) {
                    throw new ValidationException("orders.csv line " + lineNo + ": invalid priority (must be integer)");
                }

                // verificar duplicidade
                boolean duplicate = orders.stream().anyMatch(o -> o.getOrderID().equals(orderId));
                if (duplicate)
                    throw new ValidationException("orders.csv line " + lineNo + ": duplicate order ID " + orderId);

                orders.add(new Order(orderId, dueDate, priority));
            }

        } catch (IOException e) {
            throw new ValidationException("Error reading orders.csv: " + e.getMessage(), e);
        }

        return orders;
    }

    /** Loads orderlines.csv and attaches lines to existing Order objects. */
    public void loadOrderLines(String filename, List<Order> orders) throws ValidationException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null)
                throw new ValidationException("orderlines.csv is empty");

            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                String[] f = line.split(",");
                if (f.length != 4)
                    throw new ValidationException("orderlines.csv line " + lineNo + ": invalid column count");

                String orderId = f[0].trim();
                String lineNumberStr = f[1].trim();
                String sku = f[2].trim();
                String qtyStr = f[3].trim();

                if (orderId.isEmpty() || sku.isEmpty() || qtyStr.isEmpty() || lineNumberStr.isEmpty())
                    throw new ValidationException("orderlines.csv line " + lineNo + ": missing mandatory fields");

                int qty;
                try {
                    qty = Integer.parseInt(qtyStr);
                } catch (NumberFormatException e) {
                    throw new ValidationException("orderlines.csv line " + lineNo + ": invalid quantity (must be integer)");
                }

                if (qty <= 0)
                    throw new ValidationException("orderlines.csv line " + lineNo + ": quantity must be positive");

                int lineNumber;
                try {
                    lineNumber = Integer.parseInt(lineNumberStr);
                } catch (NumberFormatException e) {
                    throw new ValidationException("orderlines.csv line " + lineNo + ": invalid line number (must be integer)");
                }

                if (lineNumber <= 0)
                    throw new ValidationException("orderlines.csv line " + lineNo + ": quantity must be positive");

                // encontrar o pedido correspondente na lista
                Order order = orders.stream()
                        .filter(o -> o.getOrderID().equals(orderId))
                        .findFirst()
                        .orElse(null);

                if (order == null)
                    throw new ValidationException("orderlines.csv line " + lineNo + ": unknown order ID " + orderId);

                // adicionar a orderLine
                order.addOrderLine(new OrderLine(sku, lineNumber, qty));
            }

        } catch (IOException e) {
            throw new ValidationException("Error reading orderlines.csv: " + e.getMessage(), e);
        }
    }





}
