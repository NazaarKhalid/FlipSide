package com.example.flipside.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout; // Changed from Chip
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.CartItem;
import com.example.flipside.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView btnBack, ivProductImage;
    private TextView tvProductName, tvProductPrice, tvProductDesc, tvSellerName;
    private LinearLayout layoutSellerProfile; // Changed from Chip
    private Button btnAddToCart, btnContactSeller;

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

        initViews();

        // 1. Get the ID from the Intent
        if (getIntent().hasExtra("productId")) {
            productId = getIntent().getStringExtra("productId");

            tvProductName.setText(getIntent().getStringExtra("name"));

        } else {
            productId = getIntent().getStringExtra("product_id");
        }

        // 2. CRITICAL FIX: ALWAYS Load fresh details from Firestore
        // This ensures we get the 'stockQuantity' and accurate 'sellerId'
        if (productId != null) {
            loadProductDetails(productId);
        } else {
            Toast.makeText(this, "Error: Product ID missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductDesc = findViewById(R.id.tvProductDesc);
        tvSellerName = findViewById(R.id.tvSellerName);
        layoutSellerProfile = findViewById(R.id.layoutSellerProfile);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnContactSeller = findViewById(R.id.btnContactSeller);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddToCart.setOnClickListener(v -> addToCart());

        // 1. OPEN PUBLIC PROFILE
        layoutSellerProfile.setOnClickListener(v -> {
            if (currentProduct != null && currentProduct.getSellerId() != null) {
                Intent intent = new Intent(this, PublicProfileActivity.class);
                intent.putExtra("userId", currentProduct.getSellerId());
                startActivity(intent);
            }
        });

        // 2. MESSAGE SELLER
        btnContactSeller.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Login to chat", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentProduct != null && currentProduct.getSellerId() != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("receiverId", currentProduct.getSellerId());
                startActivity(intent);
            }
        });
    }

    private void loadProductDetails(String pid) {
        db.collection("products").document(pid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentProduct = documentSnapshot.toObject(Product.class);
                        if (currentProduct != null) updateUI(currentProduct);
                    }
                });
    }

    private void updateUI(Product product) {
        tvProductName.setText(product.getName());
        tvProductPrice.setText("PKR " + product.getPrice());
        tvProductDesc.setText(product.getDescription());

        // --- FIX 1: CHECK STOCK & OWNERSHIP ---
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Check 1: Is this MY product?
        if (product.getSellerId() != null && product.getSellerId().equals(currentUserId)) {
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Your Item");
            btnAddToCart.setAlpha(0.5f); // Make it look disabled

            btnContactSeller.setEnabled(false);
            btnContactSeller.setAlpha(0.5f);

            // Optional: Hide Follow button logic too if you want
        }
        // Check 2: Is it Out of Stock?
        else if (product.getStockQuantity() <= 0) {
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Out of Stock");
            btnAddToCart.setBackgroundColor(android.graphics.Color.GRAY); // Gray out button
        }
        // Otherwise: Normal State
        else {
            btnAddToCart.setEnabled(true);
            btnAddToCart.setText("Add to Cart");
            // Reset color if needed (depending on your theme)
        }

        // Decode Image
        if (product.getImageBase64() != null && !product.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(product.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivProductImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Fetch Seller Name
        if (product.getSellerId() != null) {
            db.collection("users").document(product.getSellerId()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            tvSellerName.setText(name != null ? name : "Unknown Seller");
                        }
                    });
        }
    }

    private void addToCart() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentProduct == null) {
            Toast.makeText(this, "Product data is loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- INVENTORY CHECK ---
        if (currentProduct.getStockQuantity() <= 0) {
            Toast.makeText(this, "Sorry! This item is Out of Stock.", Toast.LENGTH_LONG).show();
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Out of Stock");
            return;
        }
        // -----------------------

        String userId = mAuth.getCurrentUser().getUid();

        // Check if item already exists in cart to increment quantity (Optional logic)
        CartItem cartItem = new CartItem(currentProduct, 1);

        db.collection("carts").document(userId)
                .collection("items").document(currentProduct.getProductId())
                .set(cartItem)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ProductDetailsActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(ProductDetailsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}