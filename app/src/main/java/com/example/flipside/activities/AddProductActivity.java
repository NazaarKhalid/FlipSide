package com.example.flipside.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.Chat;
import com.example.flipside.models.Message;
import com.example.flipside.models.Product;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText etName, etPrice, etStock, etDelivery, etDescription;
    private RadioGroup rgCategory;
    private ImageView ivProductImage;
    private Button btnSaveProduct;

    private String selectedImageBase64 = "";
    private FirebaseFirestore db;
    private String currentUserId;

    // Image Picker Launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        ivProductImage.setImageBitmap(bitmap);
                        ivProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        selectedImageBase64 = encodeImage(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews();

        ivProductImage.setOnClickListener(v -> pickImage());
        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        etDelivery = findViewById(R.id.etDelivery);
        etDescription = findViewById(R.id.etDescription);
        rgCategory = findViewById(R.id.rgCategory);
        ivProductImage = findViewById(R.id.ivProductImage);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private String encodeImage(Bitmap bitmap) {
        int maxWidth = 800;
        int height = (int) (bitmap.getHeight() * ((float) maxWidth / bitmap.getWidth()));
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, maxWidth, height, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String deliveryStr = etDelivery.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        int selectedId = rgCategory.getCheckedRadioButtonId();
        String category = "";
        if (selectedId == R.id.rbClothing) category = "Clothing";
        else if (selectedId == R.id.rbShoes) category = "Shoes";
        else if (selectedId == R.id.rbElectronics) category = "Electronics";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(stockStr) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageBase64.isEmpty()) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);
        double delivery = TextUtils.isEmpty(deliveryStr) ? 0.0 : Double.parseDouble(deliveryStr);

        String productId = UUID.randomUUID().toString();

        Product newProduct = new Product(
                productId,
                currentUserId,
                currentUserId,
                name,
                desc,
                stock,
                price,
                delivery,
                category
        );
        newProduct.setImageBase64(selectedImageBase64);

        btnSaveProduct.setEnabled(false);
        btnSaveProduct.setText("Saving...");

        //
        // Logic: Save Product -> Success -> Trigger Notify -> Finish
        db.collection("products").document(productId).set(newProduct)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product Added!", Toast.LENGTH_SHORT).show();

                    // --- THE FIX IS HERE ---
                    // We must call this BEFORE finishing the activity
                    notifyFollowers(name);

                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveProduct.setEnabled(true);
                    btnSaveProduct.setText("Save Product");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- HELPER METHODS (These were fine, just unused) ---

    private void notifyFollowers(String productName) {
        String messageContent = "Hey! I just posted a new product: " + productName + ". Check it out!";
        String sellerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Find users who have this seller in their 'following' list
        //
        db.collection("users").whereArrayContains("following", sellerId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String followerId = doc.getString("userId");
                            // Send the automated message
                            sendAutomatedMessage(sellerId, followerId, messageContent);
                        }
                    }
                });
    }

    private void sendAutomatedMessage(String senderId, String receiverId, String content) {
        String chatId;
        if (senderId.compareTo(receiverId) < 0) chatId = senderId + "_" + receiverId;
        else chatId = receiverId + "_" + senderId;

        Message autoMessage = new Message(UUID.randomUUID().toString(), senderId, content);

        db.collection("chats").document(chatId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Chat chat;
                    if (documentSnapshot.exists()) {
                        chat = documentSnapshot.toObject(Chat.class);
                        if (chat != null) {
                            chat.addMessage(autoMessage);
                        }
                    } else {
                        chat = new Chat(chatId, Arrays.asList(senderId, receiverId));
                        chat.addMessage(autoMessage);
                    }
                    if (chat != null) {
                        db.collection("chats").document(chatId).set(chat);
                    }
                });
    }
}