package isep.ipp.pt.g322.model;

public class Item {
    private String sku;
    private String name;
    private String category;
    private String unit;
    private Double volume;
    private Double unitWeight;

    public Item(String sku, String name, String category, String unit, Double volume, Double unitWeight) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.volume = volume;
        this.unitWeight = unitWeight;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public Double getVolume() {
        return volume;
    }

    public Double getUnitWeight() {
        return unitWeight;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public void setUnitWeight(Double unitWeight) {
        this.unitWeight = unitWeight;
    }
}
