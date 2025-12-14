package com.example.flipside.services.chain;

import com.example.flipside.models.Complaint;
import com.google.firebase.firestore.FirebaseFirestore;

public class BotHandler extends ComplaintHandler {
    @Override
    public void handleComplaint(Complaint complaint) {
        String msg = complaint.getMessage().toLowerCase();

        // bot logic: checks for keywords
        if (msg.contains("password") || msg.contains("login") || msg.contains("reset")) {
            complaint.setStatus("RESOLVED_BY_BOT");
            complaint.setAdminReply("Bot: To reset your password, go to Settings > Security.");
            saveToFirestore(complaint);
        } else if (msg.contains("hours") || msg.contains("time")) {
            complaint.setStatus("RESOLVED_BY_BOT");
            complaint.setAdminReply("Bot: We are open 24/7 online!");
            saveToFirestore(complaint);
        } else {
            // bot forwards request to admin
            if (nextHandler != null) {
                nextHandler.handleComplaint(complaint);
            }
        }
    }

    private void saveToFirestore(Complaint complaint) {
        FirebaseFirestore.getInstance().collection("complaints")
                .document(complaint.getId())
                .set(complaint);
    }
}