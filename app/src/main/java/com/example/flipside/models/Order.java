package com.example.flipside.models;

import java.util.Date;
import java.util.List;

public class Order {
    public enum OrderStatus {
        PLACED, SHIPPED, DELIVERED, CANCELLED
    }

    private String orderId;
    private String customerId;
    private String sellerId; // <--- NEW: Crucial for "My Sales" tab
    private List<CartItem> orderItems;
    private OrderStatus status;
    private double totalAmount;
    private Address shippingAddress;
    private Date orderDate;
    private Payment payment;

    public Order() {}

    public Order(String orderId, String customerId, String sellerId, List<CartItem> orderItems, double totalAmount, Address shippingAddress) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.sellerId = sellerId; // <--- Initialize this
        this.orderItems = orderItems;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PLACED;
        this.orderDate = new Date();
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getSellerId() { return sellerId; } // <--- New Getter
    public void setSellerId(String sellerId) { this.sellerId = sellerId; } // <--- New Setter

    public List<CartItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<CartItem> orderItems) { this.orderItems = orderItems; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
}