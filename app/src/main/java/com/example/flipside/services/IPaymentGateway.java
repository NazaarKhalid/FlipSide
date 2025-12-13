package com.example.flipside.services;

public interface IPaymentGateway {
    boolean processPayment(double amount, String orderId);
}