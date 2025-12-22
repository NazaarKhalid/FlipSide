package com.example.flipside.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ManageProductAdapter;
import com.example.flipside.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ImageView btnBack;

    private ManageProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyProducts(); // Refresh list when coming back from Edit page
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvManageProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        adapter = new ManageProductAdapter(this, productList);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
    }

    private void loadMyProducts() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("products")
                .whereEqualTo("sellerId", currentUserId)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshots -> {
                    progressBar.setVisibility(View.GONE);
                    productList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Product p = doc.toObject(Product.class);
                        // Double check ID injection (Good practice)
                        p.setProductId(doc.getId());
                        productList.add(p);
                    }

                    if (productList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to refresh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}