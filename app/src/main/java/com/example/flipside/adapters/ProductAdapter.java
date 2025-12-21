package com.example.flipside.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.activities.ProductDetailsActivity;
import com.example.flipside.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private boolean isSellerMode;

    // Constructor for Seller Mode (shows Edit/Delete buttons)
    public ProductAdapter(Context context, List<Product> productList, boolean isSellerMode) {
        this.context = context;
        this.productList = productList;
        this.isSellerMode = isSellerMode;
    }

    // Default Constructor (Buyer Mode - Hides buttons)
    public ProductAdapter(Context context, List<Product> productList) {
        this(context, productList, false);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure you have a layout named item_product.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // 1. Set Text Data
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("PKR " + product.getPrice());

        // 2. Image Decoding (Directly here, no ImageUtils needed)
        String base64Image = product.getImageBase64();
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 3. Seller Mode Logic (Edit/Delete)
        if (isSellerMode) {
            holder.layoutActions.setVisibility(View.VISIBLE);

            // Edit Button (Placeholder for now)
            holder.btnEdit.setOnClickListener(v ->
                    Toast.makeText(context, "Edit feature coming next!", Toast.LENGTH_SHORT).show()
            );

            // Delete Button
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Product")
                        .setMessage("Are you sure you want to delete this?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteProduct(product.getProductId(), position))
                        .setNegativeButton("No", null)
                        .show();
            });
        } else {
            holder.layoutActions.setVisibility(View.GONE);
        }

        // 4. Handle Click -> Open Details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            // Passing ALL data so the next screen doesn't need to load again
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("imageBase64", product.getImageBase64());
            intent.putExtra("stock", product.getStockQuantity());
            intent.putExtra("sellerId", product.getSellerId());
            context.startActivity(intent);
        });
    }

    private void deleteProduct(String productId, int position) {
        FirebaseFirestore.getInstance().collection("products").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    productList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivImage;
        LinearLayout layoutActions;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            ivImage = itemView.findViewById(R.id.ivItemImage);
            layoutActions = itemView.findViewById(R.id.layoutSellerActions);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}