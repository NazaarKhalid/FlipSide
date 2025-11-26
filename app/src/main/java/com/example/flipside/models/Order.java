package com.example.flipside.models;

import java.util.Date;
import java.util.List;

public class Order {
    public enum OrderStatus {
        PLACED,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    private String orderId;
    private String customerId;
    private List<CartItem> orderItems;
    private OrderStatus status;
    private double totalAmount;
    private Address shippingAddress;
    private Date orderDate;
    private Payment payment;

    // default constructor
    public Order() {
    }

    // paramterized constructor
    public Order(String orderId, String customerId, List<CartItem> orderItems, double totalAmount, Address shippingAddress) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderItems = orderItems;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PLACED;
        this.orderDate = new Date();
    }

    // getter setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

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

    // methods
    public void updateOrderStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void cancelOrder() {
        this.status = OrderStatus.CANCELLED;
    }
}