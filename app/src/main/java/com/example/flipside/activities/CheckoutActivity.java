package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.Address;
import com.example.flipside.models.CartItem;
import com.example.flipside.models.Order;
import com.example.flipside.services.EasyPaisaAdapter;
import com.example.flipside.services.IPaymentGateway;
import com.example.flipside.services.SadaPayAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
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
    private RadioGroup radioGroupPayment; // New UI

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

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Loading Cart...");

        totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        tvTotal.setText("PKR " + totalPrice);

        loadCartItems();

        btnPlaceOrder.setOnClickListener(v -> handleCheckoutProcess());
    }

    private void initViews() {
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etPhone = findViewById(R.id.etPhone);
        tvTotal = findViewById(R.id.tvCheckoutTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        radioGroupPayment = findViewById(R.id.radioGroupPayment); // New
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
                        btnPlaceOrder.setText("Confirm & Pay");
                    } else {
                        Toast.makeText(this, "Cart Empty", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void handleCheckoutProcess() {
        String street = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(street) || TextUtils.isEmpty(city) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill all shipping fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. SELECT PAYMENT GATEWAY (Adapter Pattern)
        IPaymentGateway paymentGateway;
        int selectedId = radioGroupPayment.getCheckedRadioButtonId();

        if (selectedId == R.id.rbSadaPay) {
            paymentGateway = new SadaPayAdapter();
        } else {
            paymentGateway = new EasyPaisaAdapter();
        }

        // 2. PROCESS PAYMENT (Simulated)
        // In a real app, you would pass callbacks here.
        // Since your interface is synchronous (boolean), we call it directly.
        String tempOrderId = UUID.randomUUID().toString(); // ID for payment tracking
        boolean paymentSuccess = paymentGateway.processPayment(totalPrice, tempOrderId);

        if (paymentSuccess) {
            // If payment authorized, Proceed to update Database
            placeOrderInFirestore(street, city, phone);
        } else {
            Toast.makeText(this, "Payment Failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void placeOrderInFirestore(String street, String city, String phone) {
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Finalizing...");

        String newAddressId = UUID.randomUUID().toString();
        Address deliveryAddress = new Address(newAddressId, userId, street, city, "00000", true);

        // Group by Seller
        Map<String, List<CartItem>> sellerGroups = new HashMap<>();
        for (CartItem item : itemsToOrder) {
            if (item.getProduct() != null && item.getProduct().getSellerId() != null) {
                String sellerId = item.getProduct().getSellerId();
                if (!sellerGroups.containsKey(sellerId)) {
                    sellerGroups.put(sellerId, new ArrayList<>());
                }
                sellerGroups.get(sellerId).add(item);
            }
        }

        WriteBatch batch = db.batch();

        // 3. CREATE ORDERS & DECREMENT STOCK
        for (Map.Entry<String, List<CartItem>> entry : sellerGroups.entrySet()) {
            String sellerId = entry.getKey();
            List<CartItem> sellerItems = entry.getValue();

            double sellerTotal = 0;
            for (CartItem item : sellerItems) {

                // --- SAFETY CHECK ---
                if (item.getProduct() == null || item.getProduct().getProductId() == null) {
                    continue; // Skip invalid items to prevent crash/errors
                }

                sellerTotal += (item.getProduct().getPrice() * item.getQuantity());

                String productId = item.getProduct().getProductId();
                int qtyPurchased = item.getQuantity();

                // Decrement Stock
                batch.update(db.collection("products").document(productId),
                        "stockQuantity", FieldValue.increment(-qtyPurchased));
            }

            String orderId = UUID.randomUUID().toString();
            Order subOrder = new Order(orderId, userId, sellerId, sellerItems, sellerTotal, deliveryAddress);

            // Save Order
            batch.set(db.collection("orders").document(orderId), subOrder);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> clearCartAndFinish())
                .addOnFailureListener(e -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Confirm & Pay");
                    Toast.makeText(this, "Order Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearCartAndFinish() {
        // Clear Cart
        db.collection("carts").document(userId).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    Toast.makeText(this, "Order Placed & Stock Updated!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckoutActivity.this, BuyerDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}