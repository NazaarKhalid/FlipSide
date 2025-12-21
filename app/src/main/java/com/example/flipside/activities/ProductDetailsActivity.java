package com.example.flipside.activities;

import android.content.Intent;
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
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue; // Make sure this is imported
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView btnBack, ivProductImage;
    private TextView tvProductName, tvProductPrice, tvProductDesc;
    private Chip chipSeller;
    private Button btnAddToCart, btnContactSeller, btnFollow; // Added btnFollow

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

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductDesc = findViewById(R.id.tvProductDesc);
        chipSeller = findViewById(R.id.chipSeller);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnContactSeller = findViewById(R.id.btnContactSeller);
        btnFollow = findViewById(R.id.btnFollow); // Initialize new button

        // 1. Try to get data from Intent
        if (getIntent().hasExtra("productId")) {
            productId = getIntent().getStringExtra("productId");

            currentProduct = new Product();
            currentProduct.setProductId(productId);
            currentProduct.setName(getIntent().getStringExtra("name"));
            currentProduct.setPrice(getIntent().getDoubleExtra("price", 0.0));
            currentProduct.setDescription(getIntent().getStringExtra("description"));
            currentProduct.setImageBase64(getIntent().getStringExtra("imageBase64"));
            currentProduct.setSellerId(getIntent().getStringExtra("sellerId"));

            updateUI(currentProduct);
        } else {
            productId = getIntent().getStringExtra("product_id");
        }

        if (productId == null) {
            Toast.makeText(this, "Error: Product ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Refresh data from Firestore
        loadProductDetails(productId);

        // --- BUTTON LISTENERS ---

        btnBack.setOnClickListener(v -> finish());

        btnAddToCart.setOnClickListener(v -> addToCart());

        // Message Seller Logic
        btnContactSeller.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Please login to chat", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentProduct == null || currentProduct.getSellerId() == null) {
                Toast.makeText(this, "Seller info loading...", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = mAuth.getCurrentUser().getUid();
            String sellerId = currentProduct.getSellerId();

            if (sellerId.equals(currentUserId)) {
                Toast.makeText(this, "You cannot message yourself!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ProductDetailsActivity.this, ChatActivity.class);
            intent.putExtra("receiverId", sellerId);
            startActivity(intent);
        });

        // --- FOLLOW SELLER LOGIC ---
        btnFollow.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Login to follow sellers", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentProduct == null || currentProduct.getSellerId() == null) {
                return;
            }

            String currentUserId = mAuth.getCurrentUser().getUid();
            String sellerId = currentProduct.getSellerId();

            if (sellerId.equals(currentUserId)) {
                Toast.makeText(this, "You cannot follow yourself", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update User's "following" list in Firestore
            db.collection("users").document(currentUserId)
                    .update("following", FieldValue.arrayUnion(sellerId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "You are now following this seller!", Toast.LENGTH_SHORT).show();
                        btnFollow.setText("Following");
                        btnFollow.setEnabled(false);
                        btnFollow.setAlpha(0.5f);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadProductDetails(String pid) {
        db.collection("products").document(pid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product fetchedProduct = documentSnapshot.toObject(Product.class);
                        if (fetchedProduct != null) {
                            currentProduct = fetchedProduct;
                            updateUI(currentProduct);
                        }
                    }
                });
    }

    private void updateUI(Product product) {
        tvProductName.setText(product.getName());
        tvProductPrice.setText("PKR " + product.getPrice());
        tvProductDesc.setText(product.getDescription());

        if (product.getSellerId() != null) {
            String shortId = product.getSellerId().length() > 6
                    ? product.getSellerId().substring(0, 6)
                    : product.getSellerId();
            chipSeller.setText("Seller ID: " + shortId);
        }

        if (product.getImageBase64() != null && !product.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(product.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivProductImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
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

        String userId = mAuth.getCurrentUser().getUid();

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