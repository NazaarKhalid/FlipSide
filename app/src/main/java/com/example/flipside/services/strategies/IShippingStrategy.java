package com.example.flipside.services.strategies;

public interface IShippingStrategy {
    double calculateShippingCost(double cartTotal);
    String getName(); // Helping method to save the name later
}