package isep.ipp.pt.g322.model;

public class AllocationFragment {
    private String orderID;
    private int lineNumber;
    private String sku;
    private String boxID;
    private Integer aisle;
    private Integer bay;
    private int qtyPlaced;
    private double weightPlaced;
    private Allocation originalAllocation;
    private int trolleyId;

    public String getOrderID() {
        return orderID;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSku() {
        return sku;
    }

    public String getBoxID() {
        return boxID;
    }

    public Integer getAisle() {
        return aisle;
    }

    public Integer getBay() {
        return bay;
    }

    public int getQtyPlaced() {
        return qtyPlaced;
    }

    public double getWeightPlaced() {
        return weightPlaced;
    }

    public int getTrolleyId() {
        return trolleyId;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setBoxID(String boxID) {
        this.boxID = boxID;
    }

    public void setAisle(Integer aisle) {
        this.aisle = aisle;
    }

    public void setBay(Integer bay) {
        this.bay = bay;
    }

    public void setQtyPlaced(int qtyPlaced) {
        this.qtyPlaced = qtyPlaced;
    }

    public void setWeightPlaced(double weightPlaced) {
        this.weightPlaced = weightPlaced;
    }

    public void setTrolleyId(int trolleyId) {
        this.trolleyId = trolleyId;
    }

    public Allocation getOriginalAllocation() {
        return originalAllocation;
    }

    public void setOriginalAllocation(Allocation originalAllocation) {
        this.originalAllocation = originalAllocation;
    }
}
