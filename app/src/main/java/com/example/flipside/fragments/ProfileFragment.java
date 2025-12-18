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
import com.example.flipside.services.StoreAnalyticsFacade; // Importing your Facade
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private TextView tvFollowersCount, tvFollowingCount;

    // Seller Dashboard Views
    private TextView tvRevenue, tvActiveProducts;

    private CardView cardBecomeSeller;
    private GridLayout gridSellerAnalytics;
    private Button btnBecomeSeller, btnEditProfile;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    // Add Facade
    private StoreAnalyticsFacade analyticsFacade;

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
        loadUserProfile();

        btnBecomeSeller.setOnClickListener(v -> handleBecomeSeller());
        btnEditProfile.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Profile coming soon", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvFollowersCount = view.findViewById(R.id.tvFollowersCount);
        tvFollowingCount = view.findViewById(R.id.tvFollowingCount);

        tvRevenue = view.findViewById(R.id.tvRevenue);
        tvActiveProducts = view.findViewById(R.id.tvActiveProducts);

        cardBecomeSeller = view.findViewById(R.id.cardBecomeSeller);
        gridSellerAnalytics = view.findViewById(R.id.gridSellerAnalytics);
        btnBecomeSeller = view.findViewById(R.id.btnBecomeSeller);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
    }

    private void loadUserProfile() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String role = document.getString("role");

                        tvUserName.setText(name != null ? name : "FlipSide User");
                        tvUserEmail.setText(email != null ? email : mAuth.getCurrentUser().getEmail());

                        Long followers = document.getLong("followersCount");
                        Long following = document.getLong("followingCount");

                        if (followers != null) tvFollowersCount.setText(String.valueOf(followers));
                        if (following != null) tvFollowingCount.setText(String.valueOf(following));

                        if ("Seller".equalsIgnoreCase(role)) {
                            showSellerDashboard();
                        } else {
                            showBuyerDashboard();
                        }
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

        analyticsFacade.fetchStoreStats(currentUserId, new StoreAnalyticsFacade.AnalyticsCallback() {
            @Override
            public void onDataCalculated(double totalRevenue, int totalOrders, int totalItemsSold) {
                if (getContext() == null) return;

                tvRevenue.setText("PKR " + totalRevenue);
                tvActiveProducts.setText(String.valueOf(totalItemsSold));
            }

            @Override
            public void onError(String error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Analytics Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleBecomeSeller() {
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