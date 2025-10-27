package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Responsible for importing and validating data from CSV files:
 *  - items.csv  -> Item objects
 *  - bays.csv   -> Warehouse + Bays
 *  - wagons.csv -> Wagon + Box objects
 *  - orders.csv / order_lines.csv
 */
public class CsvImporter {
    private final InventoryService.InventoryState state;
    private final InventoryService inventoryService;

    public CsvImporter(InventoryService.InventoryState state, InventoryService inventoryService) {
        this.state = state;
        this.inventoryService = inventoryService;
    }

    /** Split que aceita vírgula OU ponto-e-vírgula, mantendo colunas vazias. */
    private static String[] splitFlexible(String line) {
        String[] parts = line.split("[,;]", -1);
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    // ---------------- items.csv ----------------
    public void loadItems(String filename) throws ValidationException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) throw new IOException("Resource not found: " + filename);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String header = br.readLine(); // 1ª linha = cabeçalho
                if (header == null) throw new ValidationException("items.csv is empty");

                String line; int lineNo = 1;
                while ((line = br.readLine()) != null) {
                    lineNo++;
                    String[] f = splitFlexible(line);
                    if (f.length != 6)
                        throw new ValidationException("items.csv line " + lineNo + ": invalid column count");

                    String sku = f[0];
                    String name = f[1];
                    String category = f[2];
                    String unit = f[3];
                    if (sku.isEmpty() || name.isEmpty() || category.isEmpty() || unit.isEmpty())
                        throw new ValidationException("items.csv line " + lineNo + ": missing mandatory fields");

                    if (state.items.containsKey(sku))
                        throw new ValidationException("items.csv line " + lineNo + ": duplicate SKU " + sku);

                    double volume = Double.parseDouble(f[4]);
                    double unitWeight = Double.parseDouble(f[5]);
                    if (volume <= 0 || unitWeight <= 0)
                        throw new ValidationException("items.csv line " + lineNo + ": non-positive volume/weight");

                    state.items.put(sku, new Item(sku, name, category, unit, volume, unitWeight));
                }
            }
        } catch (IOException e) {
            throw new ValidationException("Error reading items.csv: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid numeric value in items.csv: " + e.getMessage(), e);
        }
    }

    // ---------------- bays.csv ----------------
    public void loadWarehouse(String filename) throws ValidationException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) throw new IOException("Resource not found: " + filename);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String header = br.readLine(); // 1ª linha = cabeçalho
                if (header == null) throw new ValidationException("bays.csv is empty");

                String line; int lineNo = 1;
                Set<String> bayKeys = new HashSet<>();

                while ((line = br.readLine()) != null) {
                    lineNo++;
                    String[] f = splitFlexible(line);
                    if (f.length != 4)
                        throw new ValidationException("bays.csv line " + lineNo + ": invalid column count");

                    String warehouseId = f[0];
                    String aisleId = f[1];
                    int bayNumber = Integer.parseInt(f[2]);
                    int capacity = Integer.parseInt(f[3]);
                    if (capacity <= 0)
                        throw new ValidationException("bays.csv line " + lineNo + ": capacityBoxes must be positive");

                    String key = warehouseId + "_" + aisleId + "_" + bayNumber;
                    if (!bayKeys.add(key))
                        throw new ValidationException("bays.csv line " + lineNo + ": duplicate bay definition");

                    int aisleNum = Integer.parseInt(aisleId);
                    Location loc = new Location(warehouseId, aisleNum, bayNumber);
                    BayMeta meta = new BayMeta(warehouseId, aisleId, bayNumber, capacity);
                    state.bays.put(loc, meta);
                }
            }
        } catch (IOException e) {
            throw new ValidationException("Error reading bays.csv: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid numeric value in bays.csv: " + e.getMessage(), e);
        }
    }

    // ---------------- wagons.csv ----------------
    public void loadWagons(String filename) throws ValidationException {
        Set<String> boxIds = new HashSet<>();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) throw new IOException("Resource not found: " + filename);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String header = br.readLine(); // 1ª linha = cabeçalho
                if (header == null) throw new ValidationException("wagons.csv is empty");

                String line; int lineNo = 1;
                while ((line = br.readLine()) != null) {
                    lineNo++;
                    String[] f = splitFlexible(line);
                    if (f.length != 6)
                        throw new ValidationException("wagons.csv line " + lineNo + ": invalid column count");

                    String wagonId = f[0];
                    String boxId = f[1];
                    String sku = f[2];
                    int qty = Integer.parseInt(f[3]);
                    String expiryStr = f[4];
                    String receivedStr = f[5];

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

                    Box box = new Box(boxId, sku, qty, expiryDate, receivedAt);

                    // escolher local para este SKU e inserir pelo serviço
                    Location loc = inventoryService.findAvailableLocationForSKU(sku);
                    inventoryService.setBoxLocation(boxId, loc);
                    inventoryService.insertBoxFEFO(box);
                }
            }
        } catch (IOException e) {
            throw new ValidationException("Error reading wagons.csv: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid numeric value in wagons.csv: " + e.getMessage(), e);
        }
    }

    // ---------------- orders.csv ----------------
    public List<Order> loadOrders(String filename) throws ValidationException {
        List<Order> orders = new ArrayList<>();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) throw new IOException("Resource not found: " + filename);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String header = br.readLine(); // 1ª linha = cabeçalho
                if (header == null) throw new ValidationException("orders.csv is empty");

                String line; int lineNo = 1;
                while ((line = br.readLine()) != null) {
                    lineNo++;
                    String[] f = splitFlexible(line);
                    if (f.length != 3)
                        throw new ValidationException("orders.csv line " + lineNo + ": invalid column count");

                    String orderId = f[0];
                    String dueDateStr = f[1];
                    String priorityStr = f[2];

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

                    boolean dup = orders.stream().anyMatch(o -> o.getOrderID().equals(orderId));
                    if (dup)
                        throw new ValidationException("orders.csv line " + lineNo + ": duplicate order ID " + orderId);

                    orders.add(new Order(orderId, dueDate, priority));
                }
            }
        } catch (IOException e) {
            throw new ValidationException("Error reading orders.csv: " + e.getMessage(), e);
        }
        return orders;
    }

    // ---------------- order_lines.csv ----------------
    public void loadOrderLines(String filename, List<Order> orders) throws ValidationException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) throw new IOException("Resource not found: " + filename);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String header = br.readLine(); // 1ª linha = cabeçalho
                if (header == null) throw new ValidationException("orderlines.csv is empty");

                String line; int lineNo = 1;
                while ((line = br.readLine()) != null) {
                    lineNo++;
                    String[] f = splitFlexible(line);
                    if (f.length != 4)
                        throw new ValidationException("orderlines.csv line " + lineNo + ": invalid column count");

                    String orderId = f[0];
                    String lineNumberStr = f[1];
                    String sku = f[2];
                    String qtyStr = f[3];

                    if (orderId.isEmpty() || sku.isEmpty() || qtyStr.isEmpty() || lineNumberStr.isEmpty())
                        throw new ValidationException("orderlines.csv line " + lineNo + ": missing mandatory fields");

                    int qty;
                    try { qty = Integer.parseInt(qtyStr); }
                    catch (NumberFormatException e) {
                        throw new ValidationException("orderlines.csv line " + lineNo + ": invalid quantity (must be integer)");
                    }
                    if (qty <= 0)
                        throw new ValidationException("orderlines.csv line " + lineNo + ": quantity must be positive");

                    int lineNumber;
                    try { lineNumber = Integer.parseInt(lineNumberStr); }
                    catch (NumberFormatException e) {
                        throw new ValidationException("orderlines.csv line " + lineNo + ": invalid line number (must be integer)");
                    }
                    if (lineNumber <= 0)
                        throw new ValidationException("orderlines.csv line " + lineNo + ": line number must be positive");

                    Order order = orders.stream().filter(o -> o.getOrderID().equals(orderId)).findFirst().orElse(null);
                    if (order == null)
                        throw new ValidationException("orderlines.csv line " + lineNo + ": unknown order ID " + orderId);

                    order.addOrderLine(new OrderLine(sku, lineNumber, qty));
                }
            }
        } catch (IOException e) {
            throw new ValidationException("Error reading orderlines.csv: " + e.getMessage(), e);
        }
    }
}