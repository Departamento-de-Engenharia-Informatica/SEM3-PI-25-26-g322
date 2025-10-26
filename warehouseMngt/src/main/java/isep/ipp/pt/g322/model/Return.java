package isep.ipp.pt.g322.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Return {
    private String returnId;
    private String sku;
    private int qty;
    private String reason;
    private LocalDateTime timestamp;

    public String getReturnId() {
        return returnId;
    }

    @Override
    public String toString() {
        return "Return{" +
                "returnId='" + returnId + '\'' +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                ", expiryDate=" + expiryDate +
                '}';
    }

    public void setReturnId(String returnId) {
        this.returnId = returnId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Return aReturn = (Return) o;
        return getQty() == aReturn.getQty() && Objects.equals(getReturnId(), aReturn.getReturnId()) && Objects.equals(getSku(), aReturn.getSku()) && Objects.equals(getReason(), aReturn.getReason()) && Objects.equals(getTimestamp(), aReturn.getTimestamp()) && Objects.equals(getExpiryDate(), aReturn.getExpiryDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReturnId(), getSku(), getQty(), getReason(), getTimestamp(), getExpiryDate());
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    private LocalDate expiryDate;

    public Return(String returnId, String sku, int qty, String reason,
                  LocalDateTime timestamp, LocalDate expiryDate) {
        this.returnId = returnId;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

}
