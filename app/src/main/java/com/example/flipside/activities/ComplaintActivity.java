package com.example.flipside.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.Complaint;
import com.example.flipside.services.chain.AdminHandler;
import com.example.flipside.services.chain.BotHandler;
import com.example.flipside.services.chain.ComplaintHandler;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public class ComplaintActivity extends AppCompatActivity {

    private EditText etComplaintMsg;
    private Button btnSubmit;
    private ImageView btnBack;
    private TextView tvStatusResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        etComplaintMsg = findViewById(R.id.etComplaintMsg);
        btnSubmit = findViewById(R.id.btnSubmitComplaint);
        btnBack = findViewById(R.id.btnBack);
        tvStatusResult = findViewById(R.id.tvStatusResult);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> handleComplaintSubmission());
    }

    private void handleComplaintSubmission() {
        String message = etComplaintMsg.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            etComplaintMsg.setError("Please describe your issue");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String complaintId = UUID.randomUUID().toString();

        // 1. Create Complaint Object
        Complaint complaint = new Complaint(complaintId, userId, message);

        // 2. Setup Chain of Responsibility
        ComplaintHandler botHandler = new BotHandler();
        ComplaintHandler adminHandler = new AdminHandler();

        // Chain: Bot -> Admin
        botHandler.setNextHandler(adminHandler);

        // 3. Process Complaint
        botHandler.handleComplaint(complaint);

        // 4. Update UI based on Result
        // Since your handlers modify the 'complaint' object in memory, we can read the result immediately
        if ("RESOLVED_BY_BOT".equals(complaint.getStatus())) {
            tvStatusResult.setText("🤖 Bot Reply:\n" + complaint.getAdminReply());
            tvStatusResult.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            Toast.makeText(this, "Resolved by Automated Support", Toast.LENGTH_SHORT).show();
        } else {
            tvStatusResult.setText("👤 Admin Update:\n" + complaint.getAdminReply());
            tvStatusResult.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            Toast.makeText(this, "Escalated to Support Team", Toast.LENGTH_SHORT).show();
        }

        // Clear input
        etComplaintMsg.setText("");
    }
}