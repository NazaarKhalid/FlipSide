package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ProductAdapter;
import com.example.flipside.models.Product;
import com.example.flipside.utils.filters.NameFilter;
import com.example.flipside.utils.filters.ProductFilter;
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

    private RecyclerView rvClothing, rvShoes, rvElectronics;

    private ProductAdapter clothingAdapter, shoesAdapter, electronicsAdapter;

    private List<Product> listClothing, listShoes, listElectronics;
    private List<Product> masterClothing, masterShoes, masterElectronics;

    private SearchView searchView;
    private ProgressBar progressBar;
    private ImageView ivMenu, btnCart;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        db = FirebaseFirestore.getInstance();

        initViews();

        setupRecyclerViews();

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

        rvClothing = findViewById(R.id.rvClothing);
        rvShoes = findViewById(R.id.rvShoes);
        rvElectronics = findViewById(R.id.rvElectronics);
    }

    private void setupRecyclerViews() {
        // Initialize Data Lists
        listClothing = new ArrayList<>();
        listShoes = new ArrayList<>();
        listElectronics = new ArrayList<>();

        masterClothing = new ArrayList<>();
        masterShoes = new ArrayList<>();
        masterElectronics = new ArrayList<>();

        // Initialize Adapters
        // Note: passing 'false' or standard constructor assuming not Seller mode
        clothingAdapter = new ProductAdapter(this, listClothing);
        shoesAdapter = new ProductAdapter(this, listShoes);
        electronicsAdapter = new ProductAdapter(this, listElectronics);

        // Set Horizontal Layout Managers
        rvClothing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvShoes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvElectronics.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Attach Adapters
        rvClothing.setAdapter(clothingAdapter);
        rvShoes.setAdapter(shoesAdapter);
        rvElectronics.setAdapter(electronicsAdapter);
    }

    private void setupListeners() {
        // hamburger menu
        ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // cart button
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        // search bar
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterAllShelves(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAllShelves(newText);
                return false;
            }
        });

        // side drawer nav
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_logout) {
                logout();
            } else if (id == R.id.menu_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // bottom nav
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {
                Toast.makeText(this, "Seller Mode Required", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_messages) {
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("products")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    // Clear current lists
                    listClothing.clear(); masterClothing.clear();
                    listShoes.clear(); masterShoes.clear();
                    listElectronics.clear(); masterElectronics.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        String cat = String.valueOf(product.getCategory());

                        // Sort products into shelves based on Category
                        if (cat != null) {
                            if (cat.equalsIgnoreCase("Clothing")) {
                                listClothing.add(product);
                                masterClothing.add(product);
                            } else if (cat.equalsIgnoreCase("Shoes")) {
                                listShoes.add(product);
                                masterShoes.add(product);
                            } else if (cat.equalsIgnoreCase("Electronics")) {
                                listElectronics.add(product);
                                masterElectronics.add(product);
                            }
                        }
                    }

                    // Update UI
                    clothingAdapter.notifyDataSetChanged();
                    shoesAdapter.notifyDataSetChanged();
                    electronicsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Helper to filter all 3 horizontal lists simultaneously
    private void filterAllShelves(String query) {
        ProductFilter filter = new NameFilter(query);

        // Filter Clothing
        listClothing.clear();
        listClothing.addAll(filter.meetCriteria(masterClothing));
        clothingAdapter.notifyDataSetChanged();

        // Filter Shoes
        listShoes.clear();
        listShoes.addAll(filter.meetCriteria(masterShoes));
        shoesAdapter.notifyDataSetChanged();

        // Filter Electronics
        listElectronics.clear();
        listElectronics.addAll(filter.meetCriteria(masterElectronics));
        electronicsAdapter.notifyDataSetChanged();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}