package com.example.flipside.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    private EditText etName, etPrice, etStock, etDescription; // Added Description
    private Button btnSave;
    private FirebaseFirestore db;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        db = FirebaseFirestore.getInstance();

        // Init Views
        etName = findViewById(R.id.etEditName);
        etPrice = findViewById(R.id.etEditPrice);
        etStock = findViewById(R.id.etEditStock);
        etDescription = findViewById(R.id.etEditDescription); // New Field
        btnSave = findViewById(R.id.btnSaveChanges);

        // Get ID
        productId = getIntent().getStringExtra("productId");

        if (productId == null) {
            Toast.makeText(this, "Error: Unknown Product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // LOAD DATA FROM FIRESTORE (Better than Intent Extras)
        loadCurrentData();

        btnSave.setOnClickListener(v -> updateProduct());
    }

    private void loadCurrentData() {
        btnSave.setEnabled(false); // Disable button while loading
        db.collection("products").document(productId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        etName.setText(document.getString("name"));
                        etDescription.setText(document.getString("description"));

                        // Safely handle numbers
                        Double price = document.getDouble("price");
                        Long stock = document.getLong("stockQuantity");

                        etPrice.setText(price != null ? String.valueOf(price) : "0");
                        etStock.setText(stock != null ? String.valueOf(stock) : "0");
                    }
                    btnSave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateProduct() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("price", price);
        updates.put("stockQuantity", stock);
        updates.put("description", desc);

        btnSave.setText("Updating...");
        btnSave.setEnabled(false);

        db.collection("products").document(productId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product Updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Closes screen and goes back to list
                })
                .addOnFailureListener(e -> {
                    btnSave.setText("Save Changes");
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}