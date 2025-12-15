package com.example.flipside.services.strategies;

public class StandardShipping implements IShippingStrategy {
    @Override
    public double calculateShippingCost(double cartTotal) {
        return 200.0; // Flat fee
    }

    @Override
    public String getName() {
        return "Standard Shipping (3-5 Days)";
    }
}