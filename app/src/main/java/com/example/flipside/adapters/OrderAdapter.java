package com.example.flipside.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.models.Order;
import com.example.flipside.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // 1. Order ID
        String orderIdDisplay = (order.getOrderId() != null && order.getOrderId().length() > 8)
                ? order.getOrderId().substring(0, 8).toUpperCase()
                : order.getOrderId();
        holder.tvOrderId.setText("Order #" + orderIdDisplay);

        // 2. Date
        if (order.getOrderDate() != null) {
            holder.tvDate.setText(dateFormat.format(order.getOrderDate()));
        }

        // 3. Price
        holder.tvTotal.setText("PKR " + order.getTotalAmount());

        // 4. Status Styling
        String status = (order.getStatus() != null) ? order.getStatus().toString() : "PLACED";
        holder.tvStatus.setText(status);

        if ("DELIVERED".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#388E3C")); // Green
            holder.btnRate.setVisibility(View.VISIBLE);
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Red
            holder.btnRate.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F57C00")); // Orange
            holder.btnRate.setVisibility(View.GONE);
        }

        // 5. Rate Logic
        holder.btnRate.setOnClickListener(v -> {
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                // Get store ID from the first item
                String storeId = order.getOrderItems().get(0).getProduct().getStoreId();
                if (storeId != null) {
                    showRatingDialog(context, storeId);
                } else {
                    Toast.makeText(context, "Cannot rate: Store ID missing", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void showRatingDialog(Context context, String storeId) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_rate_store);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        EditText etComment = dialog.findViewById(R.id.etComment);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmitReview);

        btnSubmit.setOnClickListener(v -> {
            float ratingFloat = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
            String reviewerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String reviewId = "rev_" + System.currentTimeMillis();

            // FIXED: Casting float to double to match your Model
            Review review = new Review(
                    reviewId,
                    reviewerId,
                    storeId,        // This maps to 'targetStoreId' in your constructor
                    (double) ratingFloat,
                    comment
            );

            FirebaseFirestore.getInstance()
                    .collection("reviews").document(reviewId).set(review)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Review Submitted!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvDate, tvTotal;
        Button btnRate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure these IDs match your item_order.xml
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnRate = itemView.findViewById(R.id.btnRateOrder);
        }
    }
}