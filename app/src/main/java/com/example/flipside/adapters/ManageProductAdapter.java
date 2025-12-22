package com.example.flipside.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.activities.EditProductActivity;
import com.example.flipside.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ManageProductAdapter extends RecyclerView.Adapter<ManageProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore db;

    public ManageProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("PKR " + product.getPrice());
        holder.tvStock.setText("Stock: " + product.getStockQuantity());

        // EDIT BUTTON
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditProductActivity.class);
            intent.putExtra("productId", product.getProductId());
            context.startActivity(intent);
        });

        // DELETE BUTTON
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to remove this item?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteProduct(product.getProductId(), position))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void deleteProduct(String productId, int position) {
        db.collection("products").document(productId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show();
                    productList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, productList.size());
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error deleting", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvManageName);
            tvPrice = itemView.findViewById(R.id.tvManagePrice);
            tvStock = itemView.findViewById(R.id.tvManageStock);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}