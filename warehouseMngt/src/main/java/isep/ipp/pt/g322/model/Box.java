package isep.ipp.pt.g322.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Box implements Comparable<Box> {
    private String boxID;
    private String SKU;
    private int quantity;
    private LocalDate expiryDate;
    private LocalDateTime receivedAt;
    private int aisle;
    private int bay;

    public String getBoxID() {
        return boxID;
    }

    public void setBoxID(String boxID) {
        this.boxID = boxID;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Box(String boxID, String SKU, int quantity, LocalDate expiryDate, LocalDateTime receivedAt) {
        setBoxID(boxID);
        setSKU(SKU);
        setQuantity(quantity);
        setExpiryDate(expiryDate);
        setReceivedAt(receivedAt);

    }

     public int compareTo(Box other) {
        // exemplo: FEFO = First Expired, First Out
        if (this.getExpiryDate() == null && other.getExpiryDate() != null) return 1;
        if (this.getExpiryDate() != null && other.getExpiryDate() == null) return -1;
        if (this.getExpiryDate() != null && other.getExpiryDate() != null) {
            int cmp = this.getExpiryDate().compareTo(other.getExpiryDate());
            if (cmp != 0) return cmp;
        }

        // fallback FIFO (ordem de recebimento)
        return this.getReceivedAt().compareTo(other.getReceivedAt());
    }

    public int getAisle() {
        return aisle;
    }

    public int getBay() {
        return bay;
    }
}
