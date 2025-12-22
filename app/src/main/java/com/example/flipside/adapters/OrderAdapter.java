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
    private String currentUserId;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        // Cache the ID once to improve performance
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
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

        // 1. Display Order ID (Shortened)
        String orderIdDisplay = (order.getOrderId() != null && order.getOrderId().length() > 8)
                ? order.getOrderId().substring(0, 8).toUpperCase()
                : order.getOrderId();
        holder.tvOrderId.setText("Order #" + orderIdDisplay);

        // 2. Display Date
        if (order.getOrderDate() != null) {
            holder.tvDate.setText(dateFormat.format(order.getOrderDate()));
        }

        // 3. Display Price
        holder.tvTotal.setText("PKR " + order.getTotalAmount());

        // 4. Status Display & Styling
        String status = (order.getStatus() != null) ? order.getStatus().toString() : "PLACED";
        holder.tvStatus.setText(status);

        // Define Colors
        int colorGreen = Color.parseColor("#388E3C");
        int colorRed = Color.parseColor("#D32F2F");
        int colorOrange = Color.parseColor("#F57C00");

        // 5. LOGIC: Button Visibility & Color
        // Check if I am the seller
        boolean isSeller = (order.getSellerId() != null && order.getSellerId().equals(currentUserId));

        if ("DELIVERED".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(colorGreen);

            // Only show Rate button if I am the BUYER (not the seller)
            if (!isSeller) {
                holder.btnRate.setVisibility(View.VISIBLE);
            } else {
                holder.btnRate.setVisibility(View.GONE);
            }

        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(colorRed);
            holder.btnRate.setVisibility(View.GONE);
        } else {
            // Placed, Shipped, etc.
            holder.tvStatus.setTextColor(colorOrange);
            holder.btnRate.setVisibility(View.GONE);
        }

        // 6. Rate Button Click Listener
        holder.btnRate.setOnClickListener(v -> {
            // SAFETY CHECK: Ensure list is not null and not empty
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {

                // 1. Try to get Seller ID from the Order object itself
                String targetId = order.getSellerId();

                // 2. Fallback: If null, try to get Store ID from the first product
                if (targetId == null) {
                    if (order.getOrderItems().get(0).getProduct() != null) {
                        targetId = order.getOrderItems().get(0).getProduct().getStoreId();
                    }
                }

                if (targetId != null) {
                    showRatingDialog(context, targetId);
                } else {
                    Toast.makeText(context, "Cannot rate: Seller info missing", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Error: Order has no items", Toast.LENGTH_SHORT).show();
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
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        EditText etComment = dialog.findViewById(R.id.etComment);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmitReview);

        btnSubmit.setOnClickListener(v -> {
            float ratingFloat = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (currentUserId == null) return;

            String reviewId = "rev_" + System.currentTimeMillis();

            // Create Review Object
            Review review = new Review(
                    reviewId,
                    currentUserId,
                    storeId,
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
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnRate = itemView.findViewById(R.id.btnRateOrder);
        }
    }
}