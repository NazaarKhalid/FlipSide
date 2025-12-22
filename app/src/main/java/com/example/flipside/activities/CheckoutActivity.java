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
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etAddress, etCity, etPhone;
    private TextView tvTotal;
    private Button btnPlaceOrder;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private double totalPrice; // This is the total displayed, but we recalculate per seller

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

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Loading Cart...");

        // Display total from previous screen just for UI
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
        db.collection("carts").document(userId).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemsToOrder.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            itemsToOrder.add(doc.toObject(CartItem.class));
                        }
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

        if (itemsToOrder.isEmpty()) {
            Toast.makeText(this, "Cart data is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Processing...");

        // Create Shared Address Object
        String newAddressId = UUID.randomUUID().toString();
        Address deliveryAddress = new Address(newAddressId, userId, street, city, "00000", true);

        // --- STEP 1: GROUP ITEMS BY SELLER ID ---
        Map<String, List<CartItem>> sellerGroups = new HashMap<>();

        for (CartItem item : itemsToOrder) {
            if (item.getProduct() != null) {
                String sellerId = item.getProduct().getSellerId();
                if (sellerId != null) {
                    if (!sellerGroups.containsKey(sellerId)) {
                        sellerGroups.put(sellerId, new ArrayList<>());
                    }
                    sellerGroups.get(sellerId).add(item);
                }
            }
        }

        // --- STEP 2: BATCH WRITE TO FIRESTORE ---
        WriteBatch batch = db.batch();

        for (Map.Entry<String, List<CartItem>> entry : sellerGroups.entrySet()) {
            String sellerId = entry.getKey();
            List<CartItem> sellerItems = entry.getValue();

            // Calculate total for THIS specific seller
            double sellerOrderTotal = 0;
            for (CartItem item : sellerItems) {
                sellerOrderTotal += (item.getProduct().getPrice() * item.getQuantity());
            }

            String orderId = UUID.randomUUID().toString();

            // Create Order Object (Using the NEW Constructor with sellerId)
            Order subOrder = new Order(
                    orderId,
                    userId,
                    sellerId,       // <--- Passed correctly now
                    sellerItems,
                    sellerOrderTotal,
                    deliveryAddress
            );

            // Add to batch
            batch.set(db.collection("orders").document(orderId), subOrder);
        }

        // --- STEP 3: COMMIT BATCH ---
        batch.commit()
                .addOnSuccessListener(aVoid -> clearCartAndFinish())
                .addOnFailureListener(e -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Confirm Order");
                    Toast.makeText(this, "Order Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearCartAndFinish() {
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