package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.flipside.R;
import com.example.flipside.fragments.HomeFragment;
import com.example.flipside.fragments.OrderHistoryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class BuyerDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // Define Fragments
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment orderHistoryFragment = new OrderHistoryFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (id == R.id.nav_orders) {
                selectedFragment = orderHistoryFragment;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_logout) { // Assuming you might have this in menu
                logout();
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Load Home Fragment by default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}