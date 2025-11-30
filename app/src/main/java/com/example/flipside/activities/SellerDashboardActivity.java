package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.flipside.models.User;
import com.example.flipside.models.Store;
import com.example.flipside.models.SellerProfile;

public class SellerDashboardActivity extends AppCompatActivity {

    private LinearLayout layoutCreateStore, layoutDashboard;
    private EditText etStoreName, etStoreDesc;
    private Button btnCreateStore, btnAddProduct;
    private TextView tvStoreTitle;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        layoutCreateStore = findViewById(R.id.layoutCreateStore);
        layoutDashboard = findViewById(R.id.layoutDashboard);
        etStoreName = findViewById(R.id.etStoreName);
        etStoreDesc = findViewById(R.id.etStoreDesc);
        btnCreateStore = findViewById(R.id.btnCreateStore);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        tvStoreTitle = findViewById(R.id.tvStoreTitle);
        progressBar = findViewById(R.id.progressBar);

        checkSellerStatus();

        btnCreateStore.setOnClickListener(v -> createStore());

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(SellerDashboardActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void checkSellerStatus() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);

                        if (user != null && user.isSeller()) {
                            layoutDashboard.setVisibility(View.VISIBLE);
                            layoutCreateStore.setVisibility(View.GONE);

                            if (user.getSellerProfile() != null && user.getSellerProfile().getStore() != null) {
                                tvStoreTitle.setText(user.getSellerProfile().getStore().getStoreName());
                            }
                        } else {
                            layoutCreateStore.setVisibility(View.VISIBLE);
                            layoutDashboard.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createStore() {
        String name = etStoreName.getText().toString().trim();
        String desc = etStoreDesc.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etStoreName.setError("Store Name Required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String storeId = "store_" + currentUserId;
        Store newStore = new Store(storeId, currentUserId, name, desc, "");

        SellerProfile newSellerProfile = new SellerProfile(currentUserId, currentUserId, newStore);

        db.collection("users").document(currentUserId)
                .update("sellerProfile", newSellerProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Store Created Successfully!", Toast.LENGTH_SHORT).show();
                    recreate();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to create store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}