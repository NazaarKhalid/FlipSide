package com.example.flipside.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.User;
import com.example.flipside.models.CartItem;
import com.example.flipside.models.Product;
import com.example.flipside.utils.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView ivDetailImage;
    private TextView tvDetailName, tvDetailPrice, tvDetailDesc, tvDetailStock;
    private Button btnAddToCart;

    private Product product;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Init UI
        ivDetailImage = findViewById(R.id.ivDetailImage);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailStock = findViewById(R.id.tvDetailStock);
        btnAddToCart = findViewById(R.id.btnAddToCart);


        String name = getIntent().getStringExtra("name");
        String desc = getIntent().getStringExtra("desc");
        double price = getIntent().getDoubleExtra("price", 0.0);
        int stock = getIntent().getIntExtra("stock", 0);
        String imageBase64 = getIntent().getStringExtra("image");
        String productId = getIntent().getStringExtra("productId");
        String storeId = getIntent().getStringExtra("storeId"); // Needed for ordering later


        tvDetailName.setText(name);
        tvDetailDesc.setText(desc);
        tvDetailPrice.setText("PKR " + price);
        tvDetailStock.setText("In Stock: " + stock);

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            Bitmap bitmap = ImageUtils.stringToBitmap(imageBase64);
            if (bitmap != null) {
                ivDetailImage.setImageBitmap(bitmap);
            }
        }

        btnAddToCart.setOnClickListener(v -> addToCart(productId, name, price));

        Button btnMessageSeller = findViewById(R.id.btnMessageSeller);
        String sellerId = getIntent().getStringExtra("sellerId");

        btnMessageSeller.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, ChatActivity.class);
            intent.putExtra("sellerId", sellerId);
            startActivity(intent);
        });
    }

    private void addToCart(String productId, String name, double price) {
        String userId = mAuth.getCurrentUser().getUid();

        Product partialProduct = new Product();
        partialProduct.setProductId(productId);
        partialProduct.setName(name);
        partialProduct.setPrice(price);
        // Add the image passed via intent so it shows in cart
        String imageBase64 = getIntent().getStringExtra("image");
        partialProduct.addImage(imageBase64);

        CartItem newItem = new CartItem(partialProduct, 1); // Default quantity 1


        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {

                        user.getBuyerProfile().getCart().addItem(newItem);

                        // Save back to Firestore
                        db.collection("users").document(userId).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show();

                                    // Navigate to Cart Activity
                                    android.content.Intent intent = new android.content.Intent(this, CartActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                    }
                });
    }
}