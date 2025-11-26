package com.example.flipside.models;

public class Payment {
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }

    private String paymentId;
    private String orderId;
    private double amount;
    private PaymentStatus status;
    private String transactionId;

    // default constructor
    public Payment() {
    }

    // parameterized constructor
    public Payment(String paymentId, String orderId, double amount, String transactionId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.status = PaymentStatus.PENDING;
    }

    // getter setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    // Methods
    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }
}