package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ProductAdapter;
import com.example.flipside.models.Product;
import com.example.flipside.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerDashboardActivity extends AppCompatActivity {

    private TextView tvStoreName;
    private Button btnAddProduct, btnViewAnalytics, btnLogout;
    private RecyclerView rvSellerProducts;

    private ProductAdapter productAdapter;
    private List<Product> productList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        tvStoreName = findViewById(R.id.tvStoreName);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnViewAnalytics = findViewById(R.id.btnViewAnalytics);
        btnLogout = findViewById(R.id.btnLogout);
        rvSellerProducts = findViewById(R.id.rvSellerProducts);

        rvSellerProducts.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();

        productAdapter = new ProductAdapter(this, productList, true);
        rvSellerProducts.setAdapter(productAdapter);

        loadStoreInfo();
        loadSellerProducts();

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(SellerDashboardActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        btnViewAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(SellerDashboardActivity.this, SellerAnalyticsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(SellerDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSellerProducts();
    }

    private void loadStoreInfo() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getSellerProfile() != null && user.getSellerProfile().getStore() != null) {
                            tvStoreName.setText(user.getSellerProfile().getStore().getStoreName());
                        }
                    }
                });
    }

    private void loadSellerProducts() {
        db.collection("products")
                .whereEqualTo("sellerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading products", Toast.LENGTH_SHORT).show());
    }
}