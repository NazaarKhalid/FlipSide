package com.example.flipside.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.OrderAdapter;
import com.example.flipside.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrderHistory;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyText;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    private TextView btnPurchases, btnSales;
    private boolean isBuyerMode = true;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        initViews(view);
        setupToggleListeners();

        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(getContext(), orderList);
        rvOrderHistory.setAdapter(orderAdapter);

        fetchOrders();

        return view;
    }

    private void initViews(View view) {
        rvOrderHistory = view.findViewById(R.id.rvOrderHistory);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvEmptyText = view.findViewById(R.id.tvEmptyText);
        btnPurchases = view.findViewById(R.id.btnPurchases);
        btnSales = view.findViewById(R.id.btnSales);
    }

    private void setupToggleListeners() {
        btnPurchases.setOnClickListener(v -> {
            if (!isBuyerMode) {
                isBuyerMode = true;
                updateToggleUI();
                fetchOrders();
            }
        });

        btnSales.setOnClickListener(v -> {
            if (isBuyerMode) {
                isBuyerMode = false;
                updateToggleUI();
                fetchOrders();
            }
        });
    }

    // --- FIX IS HERE ---
    private void updateToggleUI() {
        if (isBuyerMode) {
            // My Purchases: Active
            // CHANGED: setBackgroundResource -> setBackgroundColor
            btnPurchases.setBackgroundColor(Color.parseColor("#5D4037"));
            btnPurchases.setTextColor(Color.WHITE);

            btnSales.setBackgroundColor(Color.TRANSPARENT);
            btnSales.setTextColor(Color.parseColor("#757575")); // Grey

            tvEmptyText.setText("No purchases yet");
        } else {
            // My Sales: Active
            // CHANGED: setBackgroundResource -> setBackgroundColor
            btnSales.setBackgroundColor(Color.parseColor("#5D4037"));
            btnSales.setTextColor(Color.WHITE);

            btnPurchases.setBackgroundColor(Color.TRANSPARENT);
            btnPurchases.setTextColor(Color.parseColor("#757575"));

            tvEmptyText.setText("No sales yet");
        }
    }

    private void fetchOrders() {
        if (currentUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        rvOrderHistory.setVisibility(View.GONE);

        String fieldToQuery = isBuyerMode ? "customerId" : "sellerId";

        db.collection("orders")
                .whereEqualTo(fieldToQuery, currentUserId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null) return;

                    progressBar.setVisibility(View.GONE);
                    orderList.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            try {
                                Order order = document.toObject(Order.class);
                                if (order != null) {
                                    if (order.getOrderId() == null) {
                                        order.setOrderId(document.getId());
                                    }
                                    orderList.add(order);
                                }
                            } catch (Exception e) {
                                // Safe catch: Skip corrupted orders
                                System.out.println("Skipping bad order: " + document.getId());
                            }
                        }
                    }

                    if (orderList.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvOrderHistory.setVisibility(View.VISIBLE);
                        orderAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        progressBar.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}