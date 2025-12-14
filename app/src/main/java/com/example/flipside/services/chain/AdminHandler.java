package com.example.flipside.services.chain;

import com.example.flipside.models.Complaint;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHandler extends ComplaintHandler {
    @Override
    public void handleComplaint(Complaint complaint) {
        // admin handles complaint when bot fails
        complaint.setStatus("ESCALATED_TO_ADMIN");
        complaint.setAdminReply("Your request has been forwarded to a human agent.");

        FirebaseFirestore.getInstance().collection("complaints")
                .document(complaint.getId())
                .set(complaint);
    }
}