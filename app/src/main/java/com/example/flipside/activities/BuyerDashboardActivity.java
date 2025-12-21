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
import com.example.flipside.fragments.ChatListFragment; // Import this
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
    private final Fragment chatListFragment = new ChatListFragment(); // 1. Add Chat Fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_drawer);
        bottomNav = findViewById(R.id.bottom_navigation); // Ensure this ID matches XML

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
                // 2. CHANGED: Load the Chat Fragment instead of Toast
                selectedFragment = chatListFragment;
            }
            else if (id == R.id.nav_sell) {
                // DIRECT ACTION: Open AddProductActivity
                Intent intent = new Intent(BuyerDashboardActivity.this, AddProductActivity.class);
                startActivity(intent);
                return false; // Don't highlight the tab
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

            // Logout logic is here (Side Drawer)
            if (id == R.id.menu_logout) {
                logout();
            } else if (id == R.id.menu_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
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