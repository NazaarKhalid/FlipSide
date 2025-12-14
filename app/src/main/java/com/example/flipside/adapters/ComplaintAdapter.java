package com.example.flipside.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.models.Complaint;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    private Context context;
    private List<Complaint> complaintList;

    public ComplaintAdapter(Context context, List<Complaint> complaintList) {
        this.context = context;
        this.complaintList = complaintList;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);

        holder.tvId.setText("Ticket: " + complaint.getId());
        holder.tvMsg.setText(complaint.getMessage());

        holder.btnResolve.setOnClickListener(v -> {
            String reply = holder.etReply.getText().toString();
            if (reply.isEmpty()) {
                holder.etReply.setError("Required");
                return;
            }

            complaint.setAdminReply("Admin: " + reply);
            complaint.setStatus("RESOLVED_BY_ADMIN");

            FirebaseFirestore.getInstance().collection("complaints")
                    .document(complaint.getId())
                    .set(complaint)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Resolved!", Toast.LENGTH_SHORT).show();
                        complaintList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, complaintList.size());
                    });
        });
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvMsg;
        EditText etReply;
        Button btnResolve;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvComplaintId);
            tvMsg = itemView.findViewById(R.id.tvComplaintMsg);
            etReply = itemView.findViewById(R.id.etAdminReply);
            btnResolve = itemView.findViewById(R.id.btnResolve);
        }
    }
}