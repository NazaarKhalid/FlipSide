package com.example.flipside.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.flipside.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private TextView tvFollowersCount, tvFollowingCount;
    private TextView tvRevenue, tvActiveProducts;

    private CardView cardBecomeSeller;
    private GridLayout gridSellerAnalytics;
    private Button btnBecomeSeller, btnEditProfile;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        initViews(view);
        loadUserProfile();

        // Listeners
        btnBecomeSeller.setOnClickListener(v -> handleBecomeSeller());
        btnEditProfile.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Profile coming soon", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvFollowersCount = view.findViewById(R.id.tvFollowersCount);
        tvFollowingCount = view.findViewById(R.id.tvFollowingCount);

        // Seller Dashboard Views
        tvRevenue = view.findViewById(R.id.tvRevenue);
        tvActiveProducts = view.findViewById(R.id.tvActiveProducts);
        cardBecomeSeller = view.findViewById(R.id.cardBecomeSeller);
        gridSellerAnalytics = view.findViewById(R.id.gridSellerAnalytics);
        btnBecomeSeller = view.findViewById(R.id.btnBecomeSeller);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
    }

    private void loadUserProfile() {
        if (currentUserId == null) return;

        // 1. Get Basic User Details (Name, Role, etc.)
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String role = document.getString("role"); // Assuming "Buyer" or "Seller"

                        tvUserName.setText(name != null ? name : "FlipSide User");
                        tvUserEmail.setText(email != null ? email : mAuth.getCurrentUser().getEmail());

                        // Check Role to Toggle UI
                        if ("Seller".equalsIgnoreCase(role)) {
                            showSellerDashboard();
                        } else {
                            showBuyerDashboard();
                        }

                        // If you have specific fields for counts in the user document:
                        Long followers = document.getLong("followersCount");
                        Long following = document.getLong("followingCount");

                        if (followers != null) tvFollowersCount.setText(String.valueOf(followers));
                        if (following != null) tvFollowingCount.setText(String.valueOf(following));
                    }
                })
                .addOnFailureListener(e -> {
                    if(getContext() != null)
                        Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void showBuyerDashboard() {
        cardBecomeSeller.setVisibility(View.VISIBLE);
        gridSellerAnalytics.setVisibility(View.GONE);
    }

    private void showSellerDashboard() {
        cardBecomeSeller.setVisibility(View.GONE);
        gridSellerAnalytics.setVisibility(View.VISIBLE);

        // Fetch fake analytics for now (You can replace this with real query later)
        tvRevenue.setText("PKR 0.00");

        // Count products for this seller
        db.collection("products")
                .whereEqualTo("sellerId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    tvActiveProducts.setText(String.valueOf(snapshots.size()));
                });
    }

    private void handleBecomeSeller() {
        // For now, simple toggle to Seller role in Firestore
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .update("role", "Seller")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "You are now a Seller!", Toast.LENGTH_SHORT).show();
                    showSellerDashboard();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update role", Toast.LENGTH_SHORT).show());
    }
}