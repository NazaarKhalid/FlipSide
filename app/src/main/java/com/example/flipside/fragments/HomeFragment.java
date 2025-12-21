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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.activities.CartActivity;
import com.example.flipside.adapters.ProductAdapter;
import com.example.flipside.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvClothing, rvShoes, rvElectronics;
    private ProductAdapter adapterClothing, adapterShoes, adapterElectronics;
    private List<Product> listClothing, listShoes, listElectronics;

    private ImageView btnCart; // The button causing the crash
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupListeners();

        // Initialize Lists & Adapters
        listClothing = new ArrayList<>();
        listShoes = new ArrayList<>();
        listElectronics = new ArrayList<>();

        setupRecyclerView(rvClothing, listClothing);
        setupRecyclerView(rvShoes, listShoes);
        setupRecyclerView(rvElectronics, listElectronics);

        loadProducts();

        return view;
    }

    private void initViews(View view) {
        rvClothing = view.findViewById(R.id.rvClothing);
        rvShoes = view.findViewById(R.id.rvShoes);
        rvElectronics = view.findViewById(R.id.rvElectronics);
        btnCart = view.findViewById(R.id.btnCart);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        // FIX FOR CRASH: Open CartActivity when clicked
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CartActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<Product> list) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // Pass 'false' for isSellerMode because this is the Buyer view
        ProductAdapter adapter = new ProductAdapter(getContext(), list, false);
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("products")
                .whereEqualTo("available", true) // Only show available items
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listClothing.clear();
                    listShoes.clear();
                    listElectronics.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);

                        // Filter by category
                        if ("Clothing".equalsIgnoreCase(product.getCategory())) {
                            listClothing.add(product);
                        } else if ("Shoes".equalsIgnoreCase(product.getCategory())) {
                            listShoes.add(product);
                        } else if ("Electronics".equalsIgnoreCase(product.getCategory())) {
                            listElectronics.add(product);
                        }
                    }

                    // Notify adapters (We need to re-set them or notify them if we kept references)
                    // Since I didn't keep adapter references in class vars for simplicity in setupRecyclerView,
                    // let's just re-set them here to be safe and quick.
                    rvClothing.getAdapter().notifyDataSetChanged();
                    rvShoes.getAdapter().notifyDataSetChanged();
                    rvElectronics.getAdapter().notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                });
    }
}