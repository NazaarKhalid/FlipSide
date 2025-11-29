package com.example.flipside.models;

public class Review {

    private String reviewId;
    private String reviewerId;
    private String targetStoreId;
    private double rating;
    private String comment;
    private long timestamp;


    public Review() {
    }


    public Review(String reviewId, String reviewerId, String targetStoreId, double rating, String comment) {
        this.reviewId = reviewId;
        this.reviewerId = reviewerId;
        this.targetStoreId = targetStoreId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = System.currentTimeMillis();
    }

    // 3. Getters and Setters
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getTargetStoreId() { return targetStoreId; }
    public void setTargetStoreId(String targetStoreId) { this.targetStoreId = targetStoreId; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}