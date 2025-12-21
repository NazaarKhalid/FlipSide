package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.flipside.R;
import com.example.flipside.fragments.ChatListFragment;
import com.example.flipside.fragments.HomeFragment;
import com.example.flipside.fragments.OrderHistoryFragment;
import com.example.flipside.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class BuyerDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;

    // Initialize Fragments
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment orderHistoryFragment = new OrderHistoryFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private final Fragment chatListFragment = new ChatListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_drawer);
        bottomNav = findViewById(R.id.bottom_navigation);

        // Header Hamburger Logic
        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        setupBottomNav();
        setupDrawer();

        // Load Home Fragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        }
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = homeFragment;
            }
            else if (id == R.id.nav_messages) {
                selectedFragment = chatListFragment;
            }
            else if (id == R.id.nav_sell) {
                // DIRECT ACTION: Open AddProductActivity
                Intent intent = new Intent(BuyerDashboardActivity.this, AddProductActivity.class);
                startActivity(intent);
                return false;
            }
            else if (id == R.id.nav_orders) {
                selectedFragment = orderHistoryFragment;
            }
            else if (id == R.id.nav_profile) {
                selectedFragment = profileFragment;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_logout) {
                logout();
            }
            else if (id == R.id.menu_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            }
            // --- NEW: COMPLAINT MODULE HANDLER ---
            else if (id == R.id.nav_complaint) {
                Intent intent = new Intent(BuyerDashboardActivity.this, ComplaintActivity.class);
                startActivity(intent);
            }
            // --- OPTIONAL: Handle Categories if needed ---
            else if (id == R.id.cat_clothing) {
                Toast.makeText(this, "Clothing Category", Toast.LENGTH_SHORT).show();
            }
            else if (id == R.id.cat_shoes) {
                Toast.makeText(this, "Shoes Category", Toast.LENGTH_SHORT).show();
            }
            else if (id == R.id.cat_electronics) {
                Toast.makeText(this, "Electronics Category", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}