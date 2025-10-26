package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.Order;
import isep.ipp.pt.g322.model.OrderLine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockOrderService {

    private List<Order> Orders;

    public MockOrderService() {

        Orders = new ArrayList<>();

        Order order1 = new Order("ORD00001", LocalDateTime.parse("2025-09-29T09:00:00"),2);
        Order order2 = new Order("ORD00002", LocalDateTime.parse("2025-10-03T14:00:00"),3);
        Order order3 = new Order("ORD00003", LocalDateTime.parse("2025-10-04T22:00:00"),3);



        Orders.add(order1);
        Orders.add(order2);
        Orders.add(order3);
    }

    public List<Order> getOrders() {
        return Orders;
    }

}