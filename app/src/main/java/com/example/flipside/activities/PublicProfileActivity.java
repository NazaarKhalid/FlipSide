package com.example.flipside.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ProductAdapter; // Reusing your existing adapter
import com.example.flipside.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePic, btnBack;
    private TextView tvName, tvFollowers, tvFollowing;
    private Button btnFollow, btnMessage;

    // NEW: Product List Views
    private RecyclerView rvSellerProducts;
    private ProgressBar progressBar;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String targetUserId;
    private String currentUserId;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        targetUserId = getIntent().getStringExtra("userId");

        if (targetUserId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupProductRecyclerView(); // NEW

        loadTargetUserProfile();
        loadSellerProducts(); // NEW
        checkFollowStatus();

        setupListeners();
    }

    private void initViews() {
        ivProfilePic = findViewById(R.id.ivPublicProfilePic);
        tvName = findViewById(R.id.tvPublicName);
        tvFollowers = findViewById(R.id.tvPublicFollowers);
        tvFollowing = findViewById(R.id.tvPublicFollowing);
        btnFollow = findViewById(R.id.btnPublicFollow);
        btnMessage = findViewById(R.id.btnPublicMessage);
        btnBack = findViewById(R.id.btnBack);

        rvSellerProducts = findViewById(R.id.rvSellerProducts);
        progressBar = findViewById(R.id.progressBar);

        // Hide buttons if viewing own profile
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            btnFollow.setVisibility(View.GONE);
            btnMessage.setVisibility(View.GONE);
        }
    }

    private void setupProductRecyclerView() {
        // Use a Grid Layout (2 columns) for products
        rvSellerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();
        // Reusing ProductAdapter. ensure the 3rd param isSellerMode is false
        // (we just want to view products, not edit them)
        productAdapter = new ProductAdapter(this, productList, false);
        rvSellerProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        btnFollow.setOnClickListener(v -> toggleFollow());

        btnMessage.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "Login to message", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiverId", targetUserId);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadSellerProducts() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("products")
                .whereEqualTo("sellerId", targetUserId)
                .whereEqualTo("available", true) // Optional: only show available items
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressBar.setVisibility(View.GONE);
                    productList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }

                    productAdapter.notifyDataSetChanged();

                    if (productList.isEmpty()) {
                        // Optional: Show a "No products" text if list is empty
                        // Toast.makeText(this, "No products found for this seller", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTargetUserProfile() {
        // 1. Basic Info
        db.collection("users").document(targetUserId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        tvName.setText(name != null ? name : "User");

                        String base64Image = document.getString("profileImageBase64");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivProfilePic.setImageBitmap(decodedByte);
                                ivProfilePic.setBackground(null);
                                ivProfilePic.setPadding(0, 0, 0, 0);
                                ivProfilePic.setImageTintList(null);
                            } catch (Exception e) { e.printStackTrace(); }
                        } else {
                            ivProfilePic.setImageResource(android.R.drawable.sym_def_app_icon);
                            ivProfilePic.setPadding(40, 40, 40, 40);
                            ivProfilePic.setColorFilter(Color.parseColor("#5D4037"));
                        }
                    }
                });

        // 2. Real-time Counts
        db.collection("users").document(targetUserId).collection("followers").addSnapshotListener((snapshots, e) -> {
            if (snapshots != null) tvFollowers.setText(String.valueOf(snapshots.size()));
        });

        db.collection("users").document(targetUserId).collection("following").addSnapshotListener((snapshots, e) -> {
            if (snapshots != null) tvFollowing.setText(String.valueOf(snapshots.size()));
        });
    }

    private void checkFollowStatus() {
        if (currentUserId == null) return;

        db.collection("users").document(targetUserId)
                .collection("followers").document(currentUserId)
                .get()
                .addOnSuccessListener(document -> {
                    isFollowing = document.exists();
                    updateFollowButtonUI();
                });
    }

    private void toggleFollow() {
        if (currentUserId == null) return;
        btnFollow.setEnabled(false);

        if (isFollowing) {
            // Unfollow
            db.collection("users").document(targetUserId).collection("followers").document(currentUserId).delete();
            db.collection("users").document(currentUserId).collection("following").document(targetUserId).delete()
                    .addOnSuccessListener(aVoid -> {
                        isFollowing = false;
                        updateFollowButtonUI();
                        btnFollow.setEnabled(true);
                        Toast.makeText(this, "Unfollowed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Follow
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", System.currentTimeMillis());

            db.collection("users").document(targetUserId).collection("followers").document(currentUserId).set(data);
            db.collection("users").document(currentUserId).collection("following").document(targetUserId).set(data)
                    .addOnSuccessListener(aVoid -> {
                        isFollowing = true;
                        updateFollowButtonUI();
                        btnFollow.setEnabled(true);
                        Toast.makeText(this, "Following!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateFollowButtonUI() {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundColor(Color.parseColor("#E0E0E0"));
            btnFollow.setTextColor(Color.BLACK);
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundColor(Color.parseColor("#5D4037"));
            btnFollow.setTextColor(Color.WHITE);
        }
    }
}