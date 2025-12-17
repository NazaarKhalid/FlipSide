package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ProductAdapter;
import com.example.flipside.models.Product;
import com.example.flipside.utils.filters.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BuyerDashboardActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;

    private RecyclerView rvMarketplace;
    private ProductAdapter productAdapter;
    private SearchView searchView;
    private ProgressBar progressBar;

    private ImageView ivMenu, btnCart;
    private Button btnCatAll, btnCatClothing, btnCatShoes, btnCatElec;

    private List<Product> allProductsList;
    private List<Product> filteredList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        db = FirebaseFirestore.getInstance();

        initViews();


        rvMarketplace.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        allProductsList = new ArrayList<>();
        filteredList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, filteredList);
        rvMarketplace.setAdapter(productAdapter);

        setupListeners();

        loadAllProducts();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_drawer);
        bottomNav = findViewById(R.id.bottomNav);

        ivMenu = findViewById(R.id.ivMenu);
        btnCart = findViewById(R.id.btnCart);
        searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressBar);
        rvMarketplace = findViewById(R.id.rvMarketplace);

        btnCatAll = findViewById(R.id.btnCatAll);
        btnCatClothing = findViewById(R.id.btnCatClothing);
        btnCatShoes = findViewById(R.id.btnCatShoes);
        btnCatElec = findViewById(R.id.btnCatElec);
    }

    private void setupListeners() {

        ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_logout) {
                logout();
            } else if (id == R.id.menu_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.cat_clothing) {
                filterByCategory("Clothing");
            } else if (id == R.id.cat_electronics) {
                filterByCategory("Electronics");
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {

                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {

                Toast.makeText(this, "Sell Feature coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_messages) {
                Toast.makeText(this, "Messages coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });


        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { filterProducts(query); return false; }
            @Override
            public boolean onQueryTextChange(String newText) { filterProducts(newText); return false; }
        });

        btnCatAll.setOnClickListener(v -> filterByCategory("All"));
        btnCatClothing.setOnClickListener(v -> filterByCategory("Clothing"));
        btnCatShoes.setOnClickListener(v -> filterByCategory("Shoes"));
        btnCatElec.setOnClickListener(v -> filterByCategory("Electronics"));
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("products")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    allProductsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        allProductsList.add(doc.toObject(Product.class));
                    }

                    filteredList.clear();
                    filteredList.addAll(allProductsList);
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

                ProductFilter nameFilter = new NameFilter(currentSearch);
                ProductFilter compositeFilter = new AndFilter(nameFilter, catFilter);
                filteredList.clear();
                filteredList.addAll(compositeFilter.meetCriteria(allProductsList));
            } else {
                filteredList.clear();
                filteredList.addAll(catFilter.meetCriteria(allProductsList));
            }
        }
        productAdapter.notifyDataSetChanged();
    }
}