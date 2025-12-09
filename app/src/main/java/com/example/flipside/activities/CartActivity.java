package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.CartAdapter;
import com.example.flipside.models.Cart;
import com.example.flipside.models.CartItem;
import com.example.flipside.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotalAmount, tvEmptyCart;
    private Button btnCheckout;
    private ProgressBar progressBar;

    private CartAdapter adapter;
    private List<CartItem> cartItemList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private User currentUserObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        rvCart = findViewById(R.id.rvCart);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        progressBar = findViewById(R.id.progressBar);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartItemList = new ArrayList<>();


        adapter = new CartAdapter(this, cartItemList, position -> removeItem(position));
        rvCart.setAdapter(adapter);

        loadCart();

        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            startActivity(intent);

        });
    }

    private void loadCart() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        currentUserObj = documentSnapshot.toObject(User.class);

                        if (currentUserObj != null &&
                                currentUserObj.getBuyerProfile() != null &&
                                currentUserObj.getBuyerProfile().getCart() != null) {

                            Cart cart = currentUserObj.getBuyerProfile().getCart();
                            cartItemList.clear();

                            if (cart.getCartItems() != null) {
                                cartItemList.addAll(cart.getCartItems());
                            }

                            updateUI();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();


        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.getItemTotalPrice();
        }
        tvTotalAmount.setText("PKR " + total);


        if (cartItemList.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            btnCheckout.setEnabled(false);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(true);
        }
    }

    private void removeItem(int position) {

        cartItemList.remove(position);


        currentUserObj.getBuyerProfile().getCart().setCartItems(cartItemList);

        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(currentUserId)
                .set(currentUserObj)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    updateUI();
                    Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update cart", Toast.LENGTH_SHORT).show();
                });
    }
}