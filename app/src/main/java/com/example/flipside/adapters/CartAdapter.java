package com.example.flipside.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;
    private OnCartActionListener listener;

    // Interface to handle +, -, and Remove actions
    public interface OnCartActionListener {
        void onQuantityChanged(int position, int newQuantity);
        void onRemoveItem(int position);
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, OnCartActionListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        if (item.getProduct() != null) {
            holder.tvName.setText(item.getProduct().getName());
            // Calculate price based on qty
            double totalItemPrice = item.getProduct().getPrice() * item.getQuantity();
            holder.tvPrice.setText("PKR " + totalItemPrice);
            holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Decode Image
            String base64 = item.getProduct().getImageBase64();
            if (base64 != null && !base64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.ivImage.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Action Listeners
        holder.btnPlus.setOnClickListener(v -> listener.onQuantityChanged(position, item.getQuantity() + 1));

        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChanged(position, item.getQuantity() - 1);
            } else {
                listener.onRemoveItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageView ivImage;
        ImageButton btnPlus, btnMinus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartName);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQty);
            ivImage = itemView.findViewById(R.id.ivCartImage);
            btnPlus = itemView.findViewById(R.id.btnCartPlus);
            btnMinus = itemView.findViewById(R.id.btnCartMinus);
        }
    }
}