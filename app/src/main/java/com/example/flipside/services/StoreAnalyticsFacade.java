package com.example.flipside.services;

import com.example.flipside.models.Order;
import com.example.flipside.models.CartItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StoreAnalyticsFacade {

    private FirebaseFirestore db;

    public interface AnalyticsCallback {
        void onDataCalculated(double totalRevenue, int totalOrders, int totalItemsSold);
        void onError(String error);
    }

    public StoreAnalyticsFacade() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void fetchStoreStats(String sellerId, AnalyticsCallback callback) {

        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double revenue = 0;
                    int orderCount = 0;
                    int itemsSold = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null && order.getOrderItems() != null) {
                            boolean sellerInvolved = false;

                            for (CartItem item : order.getOrderItems()) {
                                if (item.getProduct().getSellerId().equals(sellerId)) {
                                    revenue += item.getItemTotalPrice();
                                    itemsSold += item.getQuantity();
                                    sellerInvolved = true;
                                }
                            }

                            if (sellerInvolved) {
                                orderCount++;
                            }
                        }
                    }
                    callback.onDataCalculated(revenue, orderCount, itemsSold);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}