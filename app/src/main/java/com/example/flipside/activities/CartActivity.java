package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.CartAdapter;
import com.example.flipside.models.CartItem;
import com.example.flipside.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;

    private TextView tvTotalPrice, tvEmptyCart;
    private Button btnCheckout;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            finish();
        }

        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartItemList = new ArrayList<>();

        cartAdapter = new CartAdapter(this, cartItemList, position -> removeItem(position));
        rvCartItems.setAdapter(cartAdapter);

        loadCart();

        btnCheckout.setOnClickListener(v -> {
            if (!cartItemList.isEmpty()) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("totalPrice", calculateTotal());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getBuyerProfile() != null && user.getBuyerProfile().getCart() != null) {

                            List<CartItem> items = user.getBuyerProfile().getCart().getItems();

                            cartItemList.clear();
                            if (items != null) {
                                cartItemList.addAll(items);
                            }

                            if (cartItemList.isEmpty()) {
                                tvEmptyCart.setVisibility(View.VISIBLE);
                                rvCartItems.setVisibility(View.GONE);
                            } else {
                                tvEmptyCart.setVisibility(View.GONE);
                                rvCartItems.setVisibility(View.VISIBLE);
                            }

                            cartAdapter.notifyDataSetChanged();
                            updateTotalPrice();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show());
    }

    private void removeItem(int position) {
        cartItemList.remove(position);
        cartAdapter.notifyItemRemoved(position);
        updateTotalPrice();
        updateCartInFirebase();
    }

    private void updateCartInFirebase() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        user.getBuyerProfile().getCart().setItems(cartItemList);
                        db.collection("users").document(userId).set(user);
                    }
                });
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