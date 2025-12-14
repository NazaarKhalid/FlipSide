package com.example.flipside.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ComplaintAdapter;
import com.example.flipside.models.Complaint;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvComplaints;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        rvComplaints = findViewById(R.id.rvComplaints);
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));

        complaintList = new ArrayList<>();
        adapter = new ComplaintAdapter(this, complaintList);
        rvComplaints.setAdapter(adapter);

        loadEscalatedComplaints();
    }

    private void loadEscalatedComplaints() {
        FirebaseFirestore.getInstance().collection("complaints")
                .whereEqualTo("status", "ESCALATED_TO_ADMIN")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    complaintList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        complaintList.add(doc.toObject(Complaint.class));
                    }
                    adapter.notifyDataSetChanged();

                    if(complaintList.isEmpty()) {
                        Toast.makeText(this, "No pending complaints", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}