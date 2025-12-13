package com.example.flipside.services;

import android.util.Log;

public class EasyPaisaAdapter implements IPaymentGateway {
    @Override
    public boolean processPayment(double amount, String orderId) {
        Log.d("PAYMENT", "Connecting to EasyPaisa for Order: " + orderId);
        return true;
    }
}