# Train Station Warehouse — Sample Dataset (Sprint 1)

This dataset supports USEI01–USEI05 and optional Reorder.

## Files & Schemas
- bays.csv: warehouseId, aisle, bay, capacityBoxes
- wagons.csv: wagonId, boxId, sku, qty, expiryDate (optional), receivedAt
- items.csv: sku, name, category, unit, volume, unitWeight
- orders.csv: orderId, dueDate, priority
- order_lines.csv: orderId, lineNo, sku, qty
- returns.csv: returnId, sku, qty, reason, timestamp, expiryDate (optional)

Notes:
- Times are ISO-8601; sample current date = 2025-09-28T10:00:00.
- Some expiry dates are in the past to exercise policies (discard/quarantine vs allow).
- Quantities/weights/volumes are small integers/reals for hand-checking.
