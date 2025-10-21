package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.Box;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class MockInventoryService {

    public List<Box> getBoxes() {
        List<Box> inventory = new ArrayList<Box>();

        Box box1 = new Box("BOX00001","SKU0022", 7,null, LocalDateTime.parse("2025-07-05T15:42:00"));
        Box box2 = new Box("BOX00002","SKU0005", 26, LocalDate.parse("2026-01-05"), LocalDateTime.parse("2025-07-21T22:02:00"));
        Box box3 = new Box("BOX00003","SKU0006", 52,LocalDate.parse("2025-09-20"), LocalDateTime.parse("2025-09-17T21:57:00"));
        Box box4 = new Box("BOX00017","SKU0025", 35,LocalDate.parse("2026-05-13"), LocalDateTime.parse("2025-08-02T21:48:00"));
        Box box5 = new Box("BOX00018","SKU0007", 41,LocalDate.parse("2026-03-26"), LocalDateTime.parse("2025-07-04T00:22:00"));
        Box box6 = new Box("BOX00018","SKU0007", 41,LocalDate.parse("2026-03-26"), LocalDateTime.parse("2025-07-04T00:22:00"));


        inventory.add(box1);
        inventory.add(box2);
        inventory.add(box3);
        inventory.add(box4);
        inventory.add(box5);

        return inventory;
    }

    public Map<String, List<Box>> getBoxBySKU() {
        Map<String, List<Box>> skuMap = new HashMap<String, List<Box>>();

        for (Box box : getBoxes()) {

            String sku = box.getSKU();
            if (!skuMap.containsKey(sku)) {
                skuMap.put(sku, new ArrayList<>());
            }
            skuMap.get(sku).add(box);
        }


        //Orders the list on FEFO and FIFO based on the SKU
        for(List<Box> boxes : skuMap.values()) {
            boolean hasExpiry = boxes.stream().anyMatch(box -> box.getExpiryDate() != null);

            if(hasExpiry) {
                boxes.sort(Comparator.comparing(Box::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())));
            } else {
                boxes.sort(Comparator.comparing(Box::getReceivedAt));
            }
        }

        return skuMap;
    }
}
