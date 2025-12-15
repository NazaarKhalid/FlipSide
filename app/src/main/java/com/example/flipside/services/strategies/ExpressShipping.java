package com.example.flipside.services.strategies;

public class ExpressShipping implements IShippingStrategy {
    @Override
    public double calculateShippingCost(double cartTotal) {
        return 500.0;
    }

    @Override
    public String getName() {
        return "Express Shipping (1 Day)";
    }
}