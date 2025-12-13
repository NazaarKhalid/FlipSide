package com.example.flipside.services;

import android.util.Log;

public class SadaPayAdapter implements IPaymentGateway {
    @Override
    public boolean processPayment(double amount, String orderId) {
        Log.d("PAYMENT", "Connecting to SadaPay for Order: " + orderId);
        return true;
    }
}