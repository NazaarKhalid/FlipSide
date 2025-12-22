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

        // Get Intent Data
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
            if (productId != null) loadProductDetails(productId);
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

        // FETCH SELLER NAME (Replace ID with Name)
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
        if (currentProduct == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        CartItem cartItem = new CartItem(currentProduct, 1);

        db.collection("carts").document(userId)
                .collection("items").document(currentProduct.getProductId())
                .set(cartItem)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show());
    }
}