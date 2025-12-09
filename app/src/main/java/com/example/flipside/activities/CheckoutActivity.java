package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.Address;
import com.example.flipside.models.Cart;
import com.example.flipside.models.Order;
import com.example.flipside.models.Payment;
import com.example.flipside.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etStreet, etCity, etZip;
    private TextView tvFinalTotal;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private User currentUserObj;
    private Cart currentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        etStreet = findViewById(R.id.etStreet);
        etCity = findViewById(R.id.etCity);
        etZip = findViewById(R.id.etZip);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);


        loadCartData();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadCartData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        currentUserObj = documentSnapshot.toObject(User.class);
                        if (currentUserObj != null) {
                            currentCart = currentUserObj.getBuyerProfile().getCart();
                            if (currentCart != null) {
                                tvFinalTotal.setText("PKR " + currentCart.getTotalAmount());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show());
    }

    private void placeOrder() {
        String street = etStreet.getText().toString();
        String city = etCity.getText().toString();
        String zip = etZip.getText().toString();

        if (TextUtils.isEmpty(street) || TextUtils.isEmpty(city) || TextUtils.isEmpty(zip)) {
            Toast.makeText(this, "Please fill address details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCart == null || currentCart.getCartItems().isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);


        String addressId = "addr_" + System.currentTimeMillis();
        Address shippingAddress = new Address(addressId, currentUserId, street, city, zip, true);


        String orderId = "ord_" + System.currentTimeMillis();
        Order newOrder = new Order(orderId, currentUserId, currentCart.getCartItems(), currentCart.getTotalAmount(), shippingAddress);


        String paymentId = "pay_" + System.currentTimeMillis();
        Payment payment = new Payment(paymentId, orderId, currentCart.getTotalAmount(), "txn_simulated");
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        newOrder.setPayment(payment);


        db.collection("orders").document(orderId).set(newOrder)
                .addOnSuccessListener(aVoid -> {

                    currentUserObj.getBuyerProfile().getCart().setCartItems(new ArrayList<>());

                    db.collection("users").document(currentUserId).set(currentUserObj)
                            .addOnSuccessListener(aVoid1 -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();


                                Intent intent = new Intent(CheckoutActivity.this, BuyerDashboardActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(this, "Order Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}