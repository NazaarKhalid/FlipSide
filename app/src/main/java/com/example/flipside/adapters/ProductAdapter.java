package com.example.flipside.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flipside.R;
import com.example.flipside.models.Product;
import com.example.flipside.utils.ImageUtils;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
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
            String base64Image = product.getImagesBase64().get(0);
            Bitmap bitmap = ImageUtils.stringToBitmap(base64Image);
            if (bitmap != null) {
                holder.ivImage.setImageBitmap(bitmap);
            }
        }


        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.example.flipside.activities.ProductDetailsActivity.class);


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


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        ImageView ivImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvStock = itemView.findViewById(R.id.tvItemStock);
            ivImage = itemView.findViewById(R.id.ivItemImage);
        }
    }
}