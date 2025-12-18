package com.example.flipside.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.activities.CartActivity;
import com.example.flipside.adapters.ProductAdapter;
import com.example.flipside.models.Product;
import com.example.flipside.utils.filters.NameFilter;
import com.example.flipside.utils.filters.ProductFilter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvClothing, rvShoes, rvElectronics;
    private ProductAdapter clothingAdapter, shoesAdapter, electronicsAdapter;

    private List<Product> listClothing, listShoes, listElectronics;
    private List<Product> masterClothing, masterShoes, masterElectronics;

    private SearchView searchView;
    private ProgressBar progressBar;
    private ImageView ivMenu, btnCart;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        loadAllProducts();

        return view;
    }

    private void initViews(View view) {
        ivMenu = view.findViewById(R.id.ivMenu);
        btnCart = view.findViewById(R.id.btnCart);
        searchView = view.findViewById(R.id.searchView);
        progressBar = view.findViewById(R.id.progressBar);

        rvClothing = view.findViewById(R.id.rvClothing);
        rvShoes = view.findViewById(R.id.rvShoes);
        rvElectronics = view.findViewById(R.id.rvElectronics);
    }

    private void setupRecyclerViews() {
        listClothing = new ArrayList<>();
        listShoes = new ArrayList<>();
        listElectronics = new ArrayList<>();

        masterClothing = new ArrayList<>();
        masterShoes = new ArrayList<>();
        masterElectronics = new ArrayList<>();

        // NOTE: 'false' here implies isSellerMode=false (Buyer View)
        clothingAdapter = new ProductAdapter(getContext(), listClothing, false);
        shoesAdapter = new ProductAdapter(getContext(), listShoes, false);
        electronicsAdapter = new ProductAdapter(getContext(), listElectronics, false);

        rvClothing.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvShoes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvElectronics.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvClothing.setAdapter(clothingAdapter);
        rvShoes.setAdapter(shoesAdapter);
        rvElectronics.setAdapter(electronicsAdapter);
    }

    private void setupListeners() {
        ivMenu.setOnClickListener(v -> Toast.makeText(getContext(), "Menu", Toast.LENGTH_SHORT).show());

        btnCart.setOnClickListener(v -> startActivity(new Intent(getContext(), CartActivity.class)));

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
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("products")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null) return;

                    progressBar.setVisibility(View.GONE);

                    listClothing.clear(); masterClothing.clear();
                    listShoes.clear(); masterShoes.clear();
                    listElectronics.clear(); masterElectronics.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Product product = doc.toObject(Product.class);
                            // Ensure ID is set
                            if (product.getProductId() == null) product.setProductId(doc.getId());

                            String cat = product.getCategory();

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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    clothingAdapter.notifyDataSetChanged();
                    shoesAdapter.notifyDataSetChanged();
                    electronicsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterAllShelves(String query) {
        ProductFilter filter = new NameFilter(query);

        listClothing.clear();
        listClothing.addAll(filter.meetCriteria(masterClothing));
        clothingAdapter.notifyDataSetChanged();

        listShoes.clear();
        listShoes.addAll(filter.meetCriteria(masterShoes));
        shoesAdapter.notifyDataSetChanged();

        listElectronics.clear();
        listElectronics.addAll(filter.meetCriteria(masterElectronics));
        electronicsAdapter.notifyDataSetChanged();
    }
}