package com.example.flipside.models;

import java.util.Date;

public class Message {
    public enum DeliveryStatus {
        SENT,
        DELIVERED,
        READ
    }


    private String messageId;
    private String senderId;
    private String content;
    private long timestamp;
    private DeliveryStatus status;


    public Message() {
    }


    public Message(String messageId, String senderId, String content) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = new Date().getTime();
        this.status = DeliveryStatus.SENT;
    }


    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }


    public void markDelivered() { this.status = DeliveryStatus.DELIVERED; }
    public void markRead() { this.status = DeliveryStatus.READ; }
}