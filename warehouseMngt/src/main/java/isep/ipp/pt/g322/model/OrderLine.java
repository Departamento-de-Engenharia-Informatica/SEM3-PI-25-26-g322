package isep.ipp.pt.g322.model;

public class OrderLine {
    private String sku;
    private int quantityRequested;

    public String getSKU() {
        return sku;
    }

    public int getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(int quantityRequested) {
        this.quantityRequested = quantityRequested;

    }

    public void setSKU(String sku) {
        this.sku = sku;
    }

    public OrderLine(String sku, int quantityRequested) {
        this.sku = sku;
        this.quantityRequested = quantityRequested;
    }
}
