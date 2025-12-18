package com.example.flipside.adapters;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.example.flipside.utils.ImageUtils;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onRemoveClick(int position);
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, OnItemClickListener listener) {
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
            holder.tvPrice.setText("PKR " + item.getProduct().getPrice());
            holder.tvQuantity.setText("x" + item.getQuantity());

            String base64 = item.getProduct().getImageBase64();
            if (base64 != null && !base64.isEmpty()) {
                Bitmap bitmap = ImageUtils.stringToBitmap(base64);
                if (bitmap != null) {
                    holder.ivImage.setImageBitmap(bitmap);
                } else {
                    holder.ivImage.setImageResource(R.drawable.ic_box);
                }
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_box);
            }
        }

        holder.btnRemove.setOnClickListener(v -> listener.onRemoveClick(position));
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageView ivImage;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartName);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            ivImage = itemView.findViewById(R.id.ivCartImage);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }
}