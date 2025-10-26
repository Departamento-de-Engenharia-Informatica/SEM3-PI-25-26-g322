package isep.ipp.pt.g322.model;

import java.time.LocalDateTime;

public class Allocation {
    private String orderID;
    private int lineNumber;
    private String sku;
    private int qtAlloc;
    private String boxID;
    private int aisle;
    private int bay;
    private double unitWeight;
    private double totalWeight;
    private int orderPriority;
    private LocalDateTime orderDueDate;

    public Allocation(String orderID, int lineNumber, String sku, int qtAlloc, String boxID, int aisle, int bay, int orderPriority, LocalDateTime orderDueDate) {
        this.orderID = orderID;
        this.lineNumber = lineNumber;
        this.sku = sku;
        this.qtAlloc = qtAlloc;
        this.boxID = boxID;
        this.aisle = aisle;
        this.bay = bay;
        this.orderPriority = orderPriority;
        this.orderDueDate = orderDueDate;
    }

    public String getOrderID() {
        return orderID;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSku() {
        return sku;
    }

    public int getQtAlloc() {
        return qtAlloc;
    }

    public String getBoxID() {
        return boxID;
    }

    public int getAisle() {
        return aisle;
    }

    public int getBay() {
        return bay;
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

    public void setQtAlloc(int qtAlloc) {
        this.qtAlloc = qtAlloc;
    }

    public void setBoxID(String boxID) {
        this.boxID = boxID;
    }

    public void setAisle(int aisle) {
        this.aisle = aisle;
    }

    public void setBay(int bay) {
        this.bay = bay;
    }

 public double getUnitWeight() {
        return unitWeight;
 }

 public void setUnitWeight(double unitWeight) {
        this.unitWeight = unitWeight;
 }

 public double getTotalWeight() {
        return unitWeight * qtAlloc;
 }
 public void setTotalWeight(double totalWeight) {
         this.totalWeight = totalWeight;
 }

 public int getOrderPriority() {
        return orderPriority;
 }

 public LocalDateTime getOrderDueDate() {
        return orderDueDate;
 }
}
