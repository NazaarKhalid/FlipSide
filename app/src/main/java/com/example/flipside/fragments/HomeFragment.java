package com.example.flipside.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.activities.CartActivity;
import com.example.flipside.adapters.ProductAdapter;
import com.example.flipside.models.Product;
import com.example.flipside.utils.filters.AndFilter;       // <--- NEW IMPORT
import com.example.flipside.utils.filters.CategoryFilter;
import com.example.flipside.utils.filters.NameFilter;
import com.example.flipside.utils.filters.ProductFilter;    // <--- NEW IMPORT
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // Views
    private ScrollView layoutDefaultView;
    private RecyclerView rvClothing, rvShoes, rvElectronics;
    private RecyclerView rvFilteredView;
    private TextView tvPageTitle;
    private ImageView btnCart;
    private ProgressBar progressBar;
    private EditText etSearch;

    // Data Lists
    private List<Product> listClothing, listShoes, listElectronics;
    private List<Product> listFiltered;
    private List<Product> allProductsCache;

    private FirebaseFirestore db;

    // STATE VARIABLES
    private String currentCategory = null; // Tracks which category we are in
    private String pendingCategory = null; // For crash prevention

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();
        allProductsCache = new ArrayList<>();

        initViews(view);
        setupListeners();

        // Setup Default Lists
        listClothing = new ArrayList<>();
        listShoes = new ArrayList<>();
        listElectronics = new ArrayList<>();
        setupRecyclerView(rvClothing, listClothing, true);
        setupRecyclerView(rvShoes, listShoes, true);
        setupRecyclerView(rvElectronics, listElectronics, true);

        // Setup Filtered List (Vertical)
        listFiltered = new ArrayList<>();
        setupRecyclerView(rvFilteredView, listFiltered, false);

        loadAllProducts();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (pendingCategory != null) {
            filterByCategory(pendingCategory);
            pendingCategory = null;
        }
    }

    private void initViews(View view) {
        layoutDefaultView = view.findViewById(R.id.layoutDefaultView);
        rvClothing = view.findViewById(R.id.rvClothing);
        rvShoes = view.findViewById(R.id.rvShoes);
        rvElectronics = view.findViewById(R.id.rvElectronics);
        rvFilteredView = view.findViewById(R.id.rvFilteredView);
        tvPageTitle = view.findViewById(R.id.tvPageTitle);
        btnCart = view.findViewById(R.id.btnCart);
        progressBar = view.findViewById(R.id.progressBar);
        etSearch = view.findViewById(R.id.etSearch);
    }

    private void setupListeners() {
        btnCart.setOnClickListener(v -> startActivity(new Intent(getContext(), CartActivity.class)));

        // SEARCH LOGIC
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Whenever text changes, re-apply filters based on CURRENT state
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<Product> list, boolean isHorizontal) {
        if (isHorizontal) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        ProductAdapter adapter = new ProductAdapter(getContext(), list, false);
        recyclerView.setAdapter(adapter);
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("products").whereEqualTo("available", true).get()
                .addOnSuccessListener(snapshots -> {
                    allProductsCache.clear();
                    listClothing.clear();
                    listShoes.clear();
                    listElectronics.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Product p = doc.toObject(Product.class);
                        allProductsCache.add(p);

                        if ("Clothing".equalsIgnoreCase(p.getCategory())) listClothing.add(p);
                        else if ("Shoes".equalsIgnoreCase(p.getCategory())) listShoes.add(p);
                        else if ("Electronics".equalsIgnoreCase(p.getCategory())) listElectronics.add(p);
                    }

                    if (rvClothing.getAdapter() != null) rvClothing.getAdapter().notifyDataSetChanged();
                    if (rvShoes.getAdapter() != null) rvShoes.getAdapter().notifyDataSetChanged();
                    if (rvElectronics.getAdapter() != null) rvElectronics.getAdapter().notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
    }

    // --- THE COMPOSITE LOGIC ENGINE ---
    private void applyFilters() {
        if (layoutDefaultView == null) return;

        String searchQuery = etSearch.getText().toString().trim();

        // CASE 1: No Category & No Search -> Show Default Home
        if (currentCategory == null && searchQuery.isEmpty()) {
            layoutDefaultView.setVisibility(View.VISIBLE);
            rvFilteredView.setVisibility(View.GONE);
            tvPageTitle.setText("Discover");
            return;
        }

        // Otherwise, we are in "Filtered Mode"
        layoutDefaultView.setVisibility(View.GONE);
        rvFilteredView.setVisibility(View.VISIBLE);

        ProductFilter finalFilter;

        // CASE 2: Category AND Search -> USE COMPOSITE (AndFilter)
        if (currentCategory != null && !searchQuery.isEmpty()) {
            tvPageTitle.setText(currentCategory + ": " + searchQuery);

            ProductFilter catFilter = new CategoryFilter(currentCategory);
            ProductFilter nameFilter = new NameFilter(searchQuery);

            // This is the Composite Pattern in action!
            finalFilter = new AndFilter(catFilter, nameFilter);
        }
        // CASE 3: Only Category
        else if (currentCategory != null) {
            tvPageTitle.setText(currentCategory);
            finalFilter = new CategoryFilter(currentCategory);
        }
        // CASE 4: Only Search
        else {
            tvPageTitle.setText("Results for: " + searchQuery);
            finalFilter = new NameFilter(searchQuery);
        }

        // Execute Filter
        List<Product> results = finalFilter.meetCriteria(allProductsCache);

        // Update UI
        listFiltered.clear();
        listFiltered.addAll(results);
        if (rvFilteredView.getAdapter() != null) {
            rvFilteredView.getAdapter().notifyDataSetChanged();
        }
    }

    // --- PUBLIC METHODS CALLED BY ACTIVITY ---

    public void filterByCategory(String categoryName) {
        if (layoutDefaultView == null) {
            pendingCategory = categoryName;
            return;
        }

        // 1. Set the new state
        this.currentCategory = categoryName;

        // 2. Clear search ONLY if we want a fresh start for the category
        if (etSearch.getText().length() > 0) {
            etSearch.setText("");
        }

        // 3. Apply logic
        applyFilters();
    }

    public void showGeneralView() {
        if (layoutDefaultView == null) return;

        this.currentCategory = null;

        if (etSearch.getText().length() > 0) {
            etSearch.setText("");
        }

        applyFilters();
    }
}