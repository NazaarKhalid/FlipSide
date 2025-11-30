package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Firebase Imports
import com.example.flipside.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvWelcome;
    private Button btnLogout, btnSellerDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 2. CHECK LOGIN STATUS
        if (currentUser == null) {
            // Not logged in? Send to Login Screen immediately
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close Main so they can't go back
            return; // Stop running the rest of the code
        }

        // 3. If we are here, User IS logged in. Setup UI.
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnSellerDashboard = findViewById(R.id.btnSellerDashboard);

        // Show their email for now (Proof it works)
        tvWelcome.setText("Welcome, " + currentUser.getEmail());

        // LOGOUT BUTTON Logic
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            // Send back to Login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // SELLER DASHBOARD Logic (We will build this screen next)
        btnSellerDashboard.setOnClickListener(v -> {
            // Placeholder: We will create SellerActivity next!
            Toast.makeText(MainActivity.this, "Opening Seller Dashboard...", Toast.LENGTH_SHORT).show();
        });
    }
}