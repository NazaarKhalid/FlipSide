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

    private EditText etName, etPrice, etStock;
    private Button btnSave;
    private FirebaseFirestore db;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etEditName);
        etPrice = findViewById(R.id.etEditPrice);
        etStock = findViewById(R.id.etEditStock);
        btnSave = findViewById(R.id.btnSaveChanges);

        productId = getIntent().getStringExtra("productId");
        String oldName = getIntent().getStringExtra("name");
        double oldPrice = getIntent().getDoubleExtra("price", 0);
        int oldStock = getIntent().getIntExtra("stock", 0);

        etName.setText(oldName);
        etPrice.setText(String.valueOf(oldPrice));
        etStock.setText(String.valueOf(oldStock));

        btnSave.setOnClickListener(v -> updateProduct());
    }

    private void updateProduct() {
        String name = etName.getText().toString();
        String priceStr = etPrice.getText().toString();
        String stockStr = etStock.getText().toString();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) return;

        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("price", price);
        updates.put("stockQuantity", stock);

        db.collection("products").document(productId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
    }
}