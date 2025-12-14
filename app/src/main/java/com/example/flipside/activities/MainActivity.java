package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnSellerDashboard = findViewById(R.id.btnSellerDashboard);

        tvWelcome.setText("Welcome, " + currentUser.getEmail());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });


        btnSellerDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SellerDashboardActivity.class);
            startActivity(intent);
        });

        Button btnMarketplace = findViewById(R.id.btnMarketplace);

        btnMarketplace.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BuyerDashboardActivity.class);
            startActivity(intent);
        });

        Button btnSupport = findViewById(R.id.btnSupport);

        btnSupport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SupportActivity.class);
            startActivity(intent);
        });

        Button btnAdminPanel = findViewById(R.id.btnAdminPanel);

        // check if user is admin
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        com.example.flipside.models.User user = doc.toObject(com.example.flipside.models.User.class);
                        if (user != null && user.isAdmin()) {
                            btnAdminPanel.setVisibility(View.VISIBLE);
                        }
                    }
                });

        btnAdminPanel.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
        });
    }
}