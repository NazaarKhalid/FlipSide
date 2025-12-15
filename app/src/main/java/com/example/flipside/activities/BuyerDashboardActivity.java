package com.example.flipside.activities;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import com.example.flipside.utils.filters.*;

public class BuyerDashboardActivity extends AppCompatActivity {

    private RecyclerView rvMarketplace;
    private ProductAdapter productAdapter;
    private List<Product> allProductsList;
    private List<Product> filteredList;

    private SearchView searchView;
    private ProgressBar progressBar;
    private Button btnCatAll, btnCatClothing, btnCatShoes, btnCatElec;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        db = FirebaseFirestore.getInstance();

        rvMarketplace = findViewById(R.id.rvMarketplace);
        searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressBar);

        btnCatAll = findViewById(R.id.btnCatAll);
        btnCatClothing = findViewById(R.id.btnCatClothing);
        btnCatShoes = findViewById(R.id.btnCatShoes);
        btnCatElec = findViewById(R.id.btnCatElec);


        rvMarketplace.setLayoutManager(new GridLayoutManager(this, 2));

        allProductsList = new ArrayList<>();
        filteredList = new ArrayList<>();

        productAdapter = new ProductAdapter(this, filteredList);
        rvMarketplace.setAdapter(productAdapter);

        loadAllProducts();


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

        Button btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnOrderHistory.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        });


        btnCatAll.setOnClickListener(v -> filterByCategory("All"));
        btnCatClothing.setOnClickListener(v -> filterByCategory("Clothing"));
        btnCatShoes.setOnClickListener(v -> filterByCategory("Shoes"));
        btnCatElec.setOnClickListener(v -> filterByCategory("Electronics"));
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

                    // Initially show all
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
        // Use the Leaf Filter
        ProductFilter nameFilter = new NameFilter(text);

        // Apply Filter
        filteredList.clear();
        filteredList.addAll(nameFilter.meetCriteria(allProductsList));
        productAdapter.notifyDataSetChanged();
    }

    private void filterByCategory(String categoryName) {
        if (categoryName.equals("All")) {
            filteredList.clear();
            filteredList.addAll(allProductsList);
        } else {
            // DEMONSTRATING COMPOSITE (AND) LOGIC
            // Example: If user typed "Nike" AND clicked "Shoes", we want both.

            String currentSearch = searchView.getQuery().toString();
            ProductFilter catFilter = new CategoryFilter(categoryName);

            if (!currentSearch.isEmpty()) {
                // Use Composite Pattern: Filter by Name AND Category
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