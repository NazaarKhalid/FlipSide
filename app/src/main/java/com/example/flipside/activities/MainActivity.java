package com.example.flipside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User is NOT logged in -> Send to Login
            sendToLogin();
        } else {
            // User IS logged in -> Check if Buyer or Seller -> Send to Dashboard
            checkUserTypeAndRedirect(currentUser.getUid());
        }
    }

    private void checkUserTypeAndRedirect(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("userType");

                        if ("Seller".equalsIgnoreCase(userType)) {
                            Intent intent = new Intent(MainActivity.this, SellerDashboardActivity.class);
                            startActivity(intent);
                        } else {
                            // Default to Buyer Dashboard
                            Intent intent = new Intent(MainActivity.this, BuyerDashboardActivity.class);
                            startActivity(intent);
                        }
                        finish(); // Close MainActivity so they can't go back
                    } else {
                        // Profile missing (rare error)
                        sendToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    sendToLogin();
                });
    }

    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}