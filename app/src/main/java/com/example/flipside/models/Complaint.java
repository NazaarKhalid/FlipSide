package com.example.flipside.models;

public class Complaint {
    private String id;
    private String userId;
    private String message;
    private String status; // "RESOLVED_BY_BOT" or "ESCALATED_TO_ADMIN"
    private String adminReply;

    public Complaint() {}

    public Complaint(String id, String userId, String message) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.status = "PENDING";
    }

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
}