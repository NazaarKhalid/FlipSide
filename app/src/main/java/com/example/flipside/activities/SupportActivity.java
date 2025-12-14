package com.example.flipside.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.models.Complaint;
import com.example.flipside.services.chain.AdminHandler;
import com.example.flipside.services.chain.BotHandler;
import com.example.flipside.services.chain.ComplaintHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SupportActivity extends AppCompatActivity {

    private EditText etComplaint;
    private Button btnSubmit;
    private TextView tvResult;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        etComplaint = findViewById(R.id.etComplaint);
        btnSubmit = findViewById(R.id.btnSubmitComplaint);
        tvResult = findViewById(R.id.tvStatusResult);
        db = FirebaseFirestore.getInstance();

        btnSubmit.setOnClickListener(v -> processComplaint());
    }

    private void processComplaint() {
        String text = etComplaint.getText().toString();
        if (text.isEmpty()) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String id = "comp_" + System.currentTimeMillis();
        Complaint complaint = new Complaint(id, userId, text);

        // COR (DP) setup
        ComplaintHandler bot = new BotHandler();
        ComplaintHandler admin = new AdminHandler();

        // bot next is admin
        bot.setNextHandler(admin);

        // start the chain
        bot.handleComplaint(complaint);


        tvResult.setText("Processing...");

        // delay slightly to allow Firestore to write
        new android.os.Handler().postDelayed(() -> {
            db.collection("complaints").document(id).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Complaint c = doc.toObject(Complaint.class);
                            tvResult.setText("Status: " + c.getStatus() + "\nReply: " + c.getAdminReply());
                        }
                    });
        }, 1500);
    }
}