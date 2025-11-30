package com.example.flipside.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

// Firebase & Models
import com.example.flipside.models.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.flipside.models.Product;
import com.example.flipside.models.User;
// NOTE: We need the ImageUtils class you created in Step 1 of the Firebase setup!
// If it's in a different package (like 'utils'), import it here.
// Assuming it is in 'com.example.flipside' for now based on your older code.
import com.example.flipside.utils.ImageUtils;
import java.io.IOException;

public class AddProductActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private EditText etProdName, etProdPrice, etProdStock, etProdDesc;
    private Spinner spCategory;
    private Button btnUploadProduct;
    private ProgressBar progressBar;

    private Uri imageUri;
    private Bitmap selectedBitmap;
    private String currentUserId;
    private String myStoreId;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        // Convert URI to Bitmap
                        selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        // Show in ImageView
                        ivProductImage.setImageBitmap(selectedBitmap);
                        ivProductImage.setAlpha(1.0f);
                        ivProductImage.setPadding(0,0,0,0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.flipside.R.layout.activity_add_product);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        ivProductImage = findViewById(com.example.flipside.R.id.ivProductImage);
        etProdName = findViewById(com.example.flipside.R.id.etProdName);
        etProdPrice = findViewById(com.example.flipside.R.id.etProdPrice);
        etProdStock = findViewById(com.example.flipside.R.id.etProdStock);
        etProdDesc = findViewById(com.example.flipside.R.id.etProdDesc);
        spCategory = findViewById(com.example.flipside.R.id.spCategory);
        btnUploadProduct = findViewById(com.example.flipside.R.id.btnUploadProduct);
        progressBar = findViewById(com.example.flipside.R.id.progressBar);

        String[] categories = {"Clothing", "Shoes", "Accessories", "Electronics", "Books"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(adapter);

        fetchStoreId();

        ivProductImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnUploadProduct.setOnClickListener(v -> uploadProduct());
    }

    private void fetchStoreId() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.isSeller()) {
                            myStoreId = user.getSellerProfile().getStore().getStoreId();
                        } else {
                            Toast.makeText(this, "Error: You are not a seller!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private void uploadProduct() {
        String name = etProdName.getText().toString().trim();
        String priceStr = etProdPrice.getText().toString().trim();
        String stockStr = etProdStock.getText().toString().trim();
        String desc = etProdDesc.getText().toString().trim();
        String categoryName = spCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(stockStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedBitmap == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUploadProduct.setEnabled(false);

        String imageBase64 = ImageUtils.bitmapToString(selectedBitmap);

        Category category = new Category("cat_" + categoryName, categoryName, "Description for " + categoryName);

        String productId = db.collection("products").document().getId();
        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);

        Product newProduct = new Product(productId, currentUserId, myStoreId, name, desc, stock, price, 0.0, category);

        newProduct.addImage(imageBase64);

        db.collection("products").document(productId).set(newProduct)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Product Uploaded!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUploadProduct.setEnabled(true);
                    Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}