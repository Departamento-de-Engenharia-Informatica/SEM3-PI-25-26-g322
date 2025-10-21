package isep.ipp.pt.g322.model;

public class OrderLineStatus {
    private String orderID;
    private int lineNumber;
    private String sku;
    private int reqQty;
    private int qtAlloc;
    private String status;

    public OrderLineStatus(String orderID, int lineNumber, String sku, int reqQty, int qtAlloc, String status) {
        this.orderID = orderID;
        this.lineNumber = lineNumber;
        this.sku = sku;
        this.reqQty = reqQty;
        this.qtAlloc = qtAlloc;
        this.status = status;
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

    public int getReqQty() {
        return reqQty;
    }

    public int getQtAlloc() {
        return qtAlloc;
    }

    public String getStatus() {
        return status;
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

    public void setReqQty(int reqQty) {
        this.reqQty = reqQty;
    }

    public void setQtAlloc(int qtAlloc) {
        this.qtAlloc = qtAlloc;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
