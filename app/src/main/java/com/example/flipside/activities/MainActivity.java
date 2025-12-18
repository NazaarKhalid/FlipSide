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
            // User IS logged in -> Check existence -> Send to Unified Dashboard
            checkUserAndRedirect(currentUser.getUid());
        }
    }

    private void checkUserAndRedirect(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        Intent intent = new Intent(MainActivity.this, BuyerDashboardActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        // User exists in Auth but not in Firestore
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
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