package com.example.flipside.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.example.flipside.activities.EditProductActivity;
import com.example.flipside.activities.ProductDetailsActivity;
import com.example.flipside.models.Product;
import com.example.flipside.utils.ImageUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private boolean isSellerMode;

    // Constructor updated to accept "isSellerMode"
    public ProductAdapter(Context context, List<Product> productList, boolean isSellerMode) {
        this.context = context;
        this.productList = productList;
        this.isSellerMode = isSellerMode;
    }

    // Constructor overload for backward compatibility (defaults to false)
    public ProductAdapter(Context context, List<Product> productList) {
        this(context, productList, false);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("PKR " + product.getPrice());
        holder.tvStock.setText("Stock: " + product.getStockQuantity());

        if (product.getImagesBase64() != null && !product.getImagesBase64().isEmpty()) {
            Bitmap bitmap = ImageUtils.stringToBitmap(product.getImagesBase64().get(0));
            if (bitmap != null) holder.ivImage.setImageBitmap(bitmap);
        }


        if (isSellerMode) {
            holder.layoutActions.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", product.getProductId());
                intent.putExtra("name", product.getName());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("stock", product.getStockQuantity());
                context.startActivity(intent);
            });


            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Product")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteProduct(product.getProductId(), position))
                        .setNegativeButton("No", null)
                        .show();
            });


            holder.itemView.setOnClickListener(null);

        } else {
            holder.layoutActions.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra("name", product.getName());
                intent.putExtra("desc", product.getDescription());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("stock", product.getStockQuantity());
                intent.putExtra("productId", product.getProductId());
                intent.putExtra("storeId", product.getStoreId());
                intent.putExtra("sellerId", product.getSellerId());
                if (product.getImagesBase64() != null && !product.getImagesBase64().isEmpty()) {
                    intent.putExtra("image", product.getImagesBase64().get(0));
                }
                context.startActivity(intent);
            });
        }
    }

    private void deleteProduct(String productId, int position) {
        FirebaseFirestore.getInstance().collection("products").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    productList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        ImageView ivImage;
        LinearLayout layoutActions;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvStock = itemView.findViewById(R.id.tvItemStock);
            ivImage = itemView.findViewById(R.id.ivItemImage);
            layoutActions = itemView.findViewById(R.id.layoutSellerActions);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}