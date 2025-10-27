package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.Item;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemService {
    static String path = "isep/ipp/pt/g322/data/items.csv";

    public static List<Item> getItems() {
        List<Item> items = new ArrayList<>();
        InputStream is = ItemService.class.getClassLoader().getResourceAsStream(path);
        if (is == null) {
            System.err.println("Resource not found: " + path);
            return items;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String sku = data[0];
                String name = data[1];
                String category = data[2];
                String unit = data[3];
                double volume = Double.parseDouble(data[4]);
                double unitWeight = Double.parseDouble(data[5]);
                Item item = new Item(sku, name, category, unit, volume, unitWeight);
                items.add(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    public Map<String, Item> getItemBySKU() {
        Map<String, Item> skuMap = new HashMap<>();
        List<Item> items = getItems();
        for (Item item : items) {
            String sku = item.getSku();
            if (!skuMap.containsKey(sku)) {
                skuMap.put(sku, item);
            }
        }
        return skuMap;
    }
}