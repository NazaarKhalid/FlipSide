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
import com.example.flipside.models.Cart;
import com.example.flipside.models.Order;
import com.example.flipside.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etAddress, etCity, etPhone;
    private TextView tvTotal;
    private Button btnPlaceOrder;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etPhone = findViewById(R.id.etPhone);
        tvTotal = findViewById(R.id.tvCheckoutTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        tvTotal.setText("Total: PKR " + totalPrice);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String street = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(street) || TextUtils.isEmpty(city) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String newAddressId = db.collection("addresses").document().getId();

        Address deliveryAddress = new Address(
                newAddressId,
                userId,
                street,
                city,
                "00000",
                true
        );

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getBuyerProfile() != null) {
                            Cart currentCart = user.getBuyerProfile().getCart();

                            if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
                                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            double cartTotal = currentCart.getTotalPrice();

                            String orderId = db.collection("orders").document().getId();

                            Order newOrder = new Order(
                                    orderId,
                                    userId,
                                    new ArrayList<>(currentCart.getItems()),
                                    cartTotal,
                                    deliveryAddress
                            );

                            db.collection("orders").document(orderId).set(newOrder)
                                    .addOnSuccessListener(aVoid -> {
                                        clearCart();
                                        Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(CheckoutActivity.this, BuyerDashboardActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Order Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }
                });
    }

    private void clearCart() {
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            User user = doc.toObject(User.class);
            if (user != null) {
                user.getBuyerProfile().setCart(new Cart());
                db.collection("users").document(userId).set(user);
            }
        });
    }
}