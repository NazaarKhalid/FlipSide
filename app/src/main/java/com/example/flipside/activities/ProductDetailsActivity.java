package com.example.flipside.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.CartItem;
import com.example.flipside.models.Product;
import com.example.flipside.models.User;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailsActivity extends AppCompatActivity {

    // UI Components (Updated to match your new Beige XML)
    private ImageView btnBack, ivProductImage;
    private TextView tvProductName, tvProductPrice, tvProductDesc;
    private Chip chipSeller;
    private Button btnAddToCart;

    // Backend Variables
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String productId;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 1. Get the Product ID passed from the Dashboard
        productId = getIntent().getStringExtra("product_id");

        // 2. Initialize Views
        btnBack = findViewById(R.id.btnBack);
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductDesc = findViewById(R.id.tvProductDesc);
        chipSeller = findViewById(R.id.chipSeller);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // 3. Setup Buttons
        btnBack.setOnClickListener(v -> finish()); // Go back to dashboard

        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                addToCart();
            }
        });

        // 4. Load Data
        if (productId != null) {
            loadProductDetails(productId);
        } else {
            Toast.makeText(this, "Error: Product not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProductDetails(String pid) {
        db.collection("products").document(pid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentProduct = documentSnapshot.toObject(Product.class);
                        updateUI(currentProduct);
                    } else {
                        Toast.makeText(this, "Product no longer available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUI(Product product) {
        tvProductName.setText(product.getName());
        tvProductPrice.setText("PKR " + product.getPrice());
        tvProductDesc.setText(product.getDescription());

        // Setup Seller Chip (Mock name if seller name isn't fetched yet)
        chipSeller.setText("Seller ID: " + product.getSellerId().substring(0, 6) + "...");

        // Decode Image (Base64 string to Bitmap)
        if (product.getImageBase64() != null && !product.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(product.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivProductImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                ivProductImage.setImageResource(R.drawable.ic_box); // Fallback
            }
        }
    }

    private void addToCart() {
        String uid = mAuth.getCurrentUser().getUid();

        // Fetch User -> Get Buyer Profile -> Add to Cart
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getBuyerProfile() != null) {

                        // Create Cart Item
                        CartItem newItem = new CartItem(currentProduct, 1);

                        // Add to local list
                        user.getBuyerProfile().getCart().addItem(newItem);

                        // Save back to Firestore
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show());
                    }
                });
    }
}