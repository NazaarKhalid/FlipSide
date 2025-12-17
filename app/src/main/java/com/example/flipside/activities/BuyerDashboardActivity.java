package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ProductAdapter;
import com.example.flipside.models.Product;
import com.example.flipside.utils.filters.*; // Assumes AndFilter, NameFilter, etc. are here
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BuyerDashboardActivity extends AppCompatActivity {

    private RecyclerView rvMarketplace;
    private ProductAdapter productAdapter;
    private List<Product> allProductsList;
    private List<Product> filteredList;

    private SearchView searchView;
    private ProgressBar progressBar;
    private Button btnCatAll, btnCatClothing, btnCatShoes, btnCatElec;
    private Button btnLogout, btnOrderHistory;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        db = FirebaseFirestore.getInstance();

        // Initialize Views
        rvMarketplace = findViewById(R.id.rvMarketplace);
        searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressBar);

        // Category Buttons
        btnCatAll = findViewById(R.id.btnCatAll);
        btnCatClothing = findViewById(R.id.btnCatClothing);
        btnCatShoes = findViewById(R.id.btnCatShoes);
        btnCatElec = findViewById(R.id.btnCatElec);

        // Other Buttons
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnLogout = findViewById(R.id.btnLogout);

        // Setup RecyclerView
        rvMarketplace.setLayoutManager(new GridLayoutManager(this, 2));

        allProductsList = new ArrayList<>();
        filteredList = new ArrayList<>();

        productAdapter = new ProductAdapter(this, filteredList);
        rvMarketplace.setAdapter(productAdapter);

        // Load Data
        loadAllProducts();

        // Setup Listeners
        setupListeners();
    }

    private void setupListeners() {
        // Search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return false;
            }
        });

        // Category Listeners
        btnCatAll.setOnClickListener(v -> filterByCategory("All"));
        btnCatClothing.setOnClickListener(v -> filterByCategory("Clothing"));
        btnCatShoes.setOnClickListener(v -> filterByCategory("Shoes"));
        btnCatElec.setOnClickListener(v -> filterByCategory("Electronics"));

        // Order History
        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        // Logout Logic
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("products")
                .whereEqualTo("available", true) // Only show available items
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    allProductsList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        allProductsList.add(product);
                    }

                    filteredList.clear();
                    filteredList.addAll(allProductsList);
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading market: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterProducts(String text) {
        ProductFilter nameFilter = new NameFilter(text);

        filteredList.clear();
        filteredList.addAll(nameFilter.meetCriteria(allProductsList));
        productAdapter.notifyDataSetChanged();
    }

    private void filterByCategory(String categoryName) {
        if (categoryName.equals("All")) {
            filteredList.clear();
            filteredList.addAll(allProductsList);
        } else {
            String currentSearch = searchView.getQuery().toString();
            ProductFilter catFilter = new CategoryFilter(categoryName);

            if (!currentSearch.isEmpty()) {
                // Composite Pattern: Filter by Name AND Category
                ProductFilter nameFilter = new NameFilter(currentSearch);
                ProductFilter compositeFilter = new AndFilter(nameFilter, catFilter);

                filteredList.clear();
                filteredList.addAll(compositeFilter.meetCriteria(allProductsList));
            } else {
                // Just Category
                filteredList.clear();
                filteredList.addAll(catFilter.meetCriteria(allProductsList));
            }
        }
        productAdapter.notifyDataSetChanged();
    }
}