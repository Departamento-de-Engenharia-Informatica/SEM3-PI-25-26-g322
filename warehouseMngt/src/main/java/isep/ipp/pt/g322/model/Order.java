package isep.ipp.pt.g322.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderID;
    private LocalDateTime dueDate;
    private int priority;
    private List<OrderLine> orderLines;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }

    public Order(String orderID, LocalDateTime dueDate, int priority) {
        this.orderID = orderID;
        this.dueDate = dueDate;
        this.priority = priority;
        this.orderLines = new ArrayList<>();
    }

    public void addOrderLine(OrderLine line) {
        this.orderLines.add(line);
    }

}
