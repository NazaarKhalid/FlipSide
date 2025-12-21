package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.CartAdapter;
import com.example.flipside.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;

    private TextView tvTotalPrice, tvEmptyCart;
    private Button btnCheckout;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 1. Check Auth
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadCart();

        // 2. Checkout Button Logic
        btnCheckout.setOnClickListener(v -> {
            if (!cartItemList.isEmpty()) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                // Pass total price to Checkout Screen
                intent.putExtra("totalPrice", calculateTotal());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartItemList = new ArrayList<>();

        // Initialize Adapter with the Action Listeners (+, -, Remove)
        cartAdapter = new CartAdapter(this, cartItemList, new CartAdapter.OnCartActionListener() {
            @Override
            public void onQuantityChanged(int position, int newQuantity) {
                // 1. Update Local Data
                CartItem item = cartItemList.get(position);
                item.setQuantity(newQuantity);

                // 2. Update UI
                cartAdapter.notifyItemChanged(position);
                updateTotalPrice();

                // 3. Update Firestore (Using Product ID stored inside CartItem)
                if (item.getProduct() != null) {
                    db.collection("carts").document(userId)
                            .collection("items").document(item.getProduct().getProductId())
                            .update("quantity", newQuantity);
                }
            }

            @Override
            public void onRemoveItem(int position) {
                CartItem itemToRemove = cartItemList.get(position);

                // 1. Remove from Local List
                cartItemList.remove(position);
                cartAdapter.notifyItemRemoved(position);
                updateTotalPrice();
                checkEmptyState();

                // 2. Remove from Firestore
                if (itemToRemove.getProduct() != null) {
                    db.collection("carts").document(userId)
                            .collection("items").document(itemToRemove.getProduct().getProductId())
                            .delete();
                }
            }
        });
        rvCartItems.setAdapter(cartAdapter);
    }

    private void loadCart() {
        // Load from the "items" sub-collection
        db.collection("carts").document(userId).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItemList.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            CartItem item = doc.toObject(CartItem.class);
                            cartItemList.add(item);
                        }
                    }

                    checkEmptyState();
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkEmptyState();
                });
    }

    private void checkEmptyState() {
        if (cartItemList.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
        }
    }

    private void updateTotalPrice() {
        double total = calculateTotal();
        tvTotalPrice.setText("PKR " + total);
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItemList) {
            if (item.getProduct() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        return total;
    }
}