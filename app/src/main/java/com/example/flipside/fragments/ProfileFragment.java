package com.example.flipside.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.flipside.R;
import com.example.flipside.activities.EditProfileActivity;
import com.example.flipside.activities.UserListActivity;
import com.example.flipside.services.StoreAnalyticsFacade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private TextView tvFollowersCount, tvFollowingCount;
    private ImageView ivProfilePic;

    // Clickable containers for lists
    private LinearLayout layoutFollowers, layoutFollowing;

    // Seller Dashboard Views
    private TextView tvRevenue, tvActiveProducts;
    private CardView cardBecomeSeller;
    private GridLayout gridSellerAnalytics;
    private Button btnBecomeSeller, btnEditProfile;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private StoreAnalyticsFacade analyticsFacade;
    private Button btnManageProducts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        analyticsFacade = new StoreAnalyticsFacade();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        initViews(view);
        setupListeners();

        // Initial Load
        loadUserProfile();

        return view;
    }

    // Refresh data when returning from Edit Profile
    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvFollowersCount = view.findViewById(R.id.tvFollowersCount);
        tvFollowingCount = view.findViewById(R.id.tvFollowingCount);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);

        layoutFollowers = view.findViewById(R.id.layoutFollowers);
        layoutFollowing = view.findViewById(R.id.layoutFollowing);

        tvRevenue = view.findViewById(R.id.tvRevenue);
        tvActiveProducts = view.findViewById(R.id.tvActiveProducts);
        cardBecomeSeller = view.findViewById(R.id.cardBecomeSeller);
        gridSellerAnalytics = view.findViewById(R.id.gridSellerAnalytics);
        btnBecomeSeller = view.findViewById(R.id.btnBecomeSeller);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnManageProducts = view.findViewById(R.id.btnManageProducts);
    }

    private void setupListeners() {
        // 1. Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnManageProducts.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), com.example.flipside.activities.ManageProductsActivity.class));
        });

        // 2. Become Seller
        btnBecomeSeller.setOnClickListener(v -> handleBecomeSeller());

        // 3. Followers List
        // Note: Ensure you add IDs to the LinearLayouts in XML
        if (layoutFollowers != null) {
            layoutFollowers.setOnClickListener(v -> openUserList("Followers", "followers"));
        }

        // 4. Following List
        if (layoutFollowing != null) {
            layoutFollowing.setOnClickListener(v -> openUserList("Following", "following"));
        }
    }

    private void openUserList(String title, String collection) {
        Intent intent = new Intent(getContext(), UserListActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("userId", currentUserId);
        intent.putExtra("collection", collection);
        startActivity(intent);
    }

    private void loadUserProfile() {
        if (currentUserId == null) return;

        // 1. Load Basic Info (Name, Email, Role, Image)
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String role = document.getString("role");

                        // --- PROFILE IMAGE LOGIC ---
                        String base64Image = document.getString("profileImageBase64");

                        if (base64Image != null && !base64Image.isEmpty()) {
                            // CASE 1: USER HAS A PHOTO
                            try {
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                ivProfilePic.setImageBitmap(decodedByte);
                                ivProfilePic.setBackground(null); // Remove gray background
                                ivProfilePic.setPadding(0, 0, 0, 0);
                                ivProfilePic.setImageTintList(null); // No brown tint

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            // CASE 2: NO PHOTO (Show Default Brown Icon)
                            ivProfilePic.setImageResource(android.R.drawable.sym_def_app_icon);
                            ivProfilePic.setPadding(40, 40, 40, 40); // Add padding for icon
                            ivProfilePic.setColorFilter(Color.parseColor("#5D4037")); // Add brown tint back
                        }

                        tvUserName.setText(name != null ? name : "FlipSide User");
                        tvUserEmail.setText(email != null ? email : mAuth.getCurrentUser().getEmail());

                        if ("Seller".equalsIgnoreCase(role)) {
                            showSellerDashboard();
                        } else {
                            showBuyerDashboard();
                        }
                    }
                });

        // 2. Count Followers (REAL-TIME COUNT from Collection)
        db.collection("users").document(currentUserId).collection("followers")
                .get()
                .addOnSuccessListener(snapshots -> {
                    tvFollowersCount.setText(String.valueOf(snapshots.size()));
                });

        // 3. Count Following (REAL-TIME COUNT from Collection)
        db.collection("users").document(currentUserId).collection("following")
                .get()
                .addOnSuccessListener(snapshots -> {
                    tvFollowingCount.setText(String.valueOf(snapshots.size()));
                });
    }

    // ... (Keep existing showBuyerDashboard, showSellerDashboard, handleBecomeSeller methods) ...
    private void showBuyerDashboard() {
        cardBecomeSeller.setVisibility(View.VISIBLE);
        gridSellerAnalytics.setVisibility(View.GONE);
    }

    private void showSellerDashboard() {
        cardBecomeSeller.setVisibility(View.GONE);
        gridSellerAnalytics.setVisibility(View.VISIBLE);
        btnManageProducts.setVisibility(View.VISIBLE);

        analyticsFacade.fetchStoreStats(currentUserId, new StoreAnalyticsFacade.AnalyticsCallback() {
            @Override
            public void onDataCalculated(double totalRevenue, int totalOrders, int totalItemsSold) {
                if (getContext() == null) return;
                tvRevenue.setText("PKR " + totalRevenue);
                tvActiveProducts.setText(String.valueOf(totalItemsSold));
            }

            @Override
            public void onError(String error) { /* Handle error */ }
        });
    }

    private void handleBecomeSeller() {
        if (currentUserId == null) return;
        db.collection("users").document(currentUserId)
                .update("role", "Seller")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "You are now a Seller!", Toast.LENGTH_SHORT).show();
                    showSellerDashboard();
                });
    }
}