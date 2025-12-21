package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.Address;
import com.example.flipside.models.CartItem;
import com.example.flipside.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etAddress, etCity, etPhone;
    private TextView tvTotal;
    private Button btnPlaceOrder;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private double totalPrice;

    private List<CartItem> itemsToOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        itemsToOrder = new ArrayList<>();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        initViews();

        // 1. DISABLE BUTTON INITIALLY (Prevent clicking before load)
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Loading Cart...");

        totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        tvTotal.setText("PKR " + totalPrice);

        loadCartItems();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void initViews() {
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etPhone = findViewById(R.id.etPhone);
        tvTotal = findViewById(R.id.tvCheckoutTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
    }

    private void loadCartItems() {
        // Fetch items from the correct collection path
        db.collection("carts").document(userId).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemsToOrder.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            itemsToOrder.add(doc.toObject(CartItem.class));
                        }
                        // 2. ENABLE BUTTON ONLY AFTER DATA ARRIVES
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText("Confirm Order");
                    } else {
                        Toast.makeText(this, "Error: Your cart appears empty.", Toast.LENGTH_LONG).show();
                        btnPlaceOrder.setText("Cart Empty");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPlaceOrder.setText("Error Loading");
                });
    }

    private void placeOrder() {
        String street = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(street) || TextUtils.isEmpty(city) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill all shipping fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Final safety check
        if (itemsToOrder.isEmpty()) {
            Toast.makeText(this, "Cart data is missing. Please go back and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Processing...");

        // Create Address
        String newAddressId = UUID.randomUUID().toString();
        Address deliveryAddress = new Address(
                newAddressId,
                userId,
                street,
                city,
                "00000",
                true
        );

        // Create Order
        String orderId = UUID.randomUUID().toString();
        Order newOrder = new Order(
                orderId,
                userId,
                itemsToOrder,
                totalPrice,
                deliveryAddress
        );

        // Save Order
        db.collection("orders").document(orderId).set(newOrder)
                .addOnSuccessListener(aVoid -> {
                    clearCartAndFinish();
                })
                .addOnFailureListener(e -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Confirm Order");
                    Toast.makeText(this, "Order Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearCartAndFinish() {
        // Delete items from Firestore cart
        db.collection("carts").document(userId).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CheckoutActivity.this, BuyerDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}