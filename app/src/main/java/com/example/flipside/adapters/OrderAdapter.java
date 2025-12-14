package com.example.flipside.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

        String orderIdDisplay = (order.getOrderId() != null && order.getOrderId().length() > 8)
                ? order.getOrderId().substring(0, 8)
                : order.getOrderId();

        holder.tvOrderId.setText("Order #" + orderIdDisplay);

        if (order.getStatus() != null) {
            holder.tvStatus.setText(order.getStatus().toString());
        }

        holder.tvTotal.setText("PKR " + order.getTotalAmount());

        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(order.getOrderDate()));
        }

        holder.btnRate.setOnClickListener(v -> {
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
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
        android.app.Dialog dialog = new android.app.Dialog(context);
        dialog.setContentView(R.layout.dialog_rate_store);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        android.widget.EditText etComment = dialog.findViewById(R.id.etComment);
        android.widget.Button btnSubmit = dialog.findViewById(R.id.btnSubmitReview);

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
            String reviewerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String reviewId = "rev_" + System.currentTimeMillis();
            Review review = new Review(reviewId, reviewerId, storeId, rating, comment);

            FirebaseFirestore.getInstance()
                    .collection("reviews").document(reviewId).set(review)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Review Submitted!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
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