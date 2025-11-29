package com.example.flipside.models;

import java.util.ArrayList;
import java.util.List;

public class Chat {

    private String chatId;
    private List<String> participantIds;
    private List<Message> messages;
    private long lastActivityAt;


    public Chat() {
    }


    public Chat(String chatId, List<String> participantIds) {
        this.chatId = chatId;
        this.participantIds = participantIds;
        this.messages = new ArrayList<>();
        this.lastActivityAt = System.currentTimeMillis();
    }

    // 3. Getters and Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public long getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(long lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public void addMessage(Message message) {
        if (this.messages == null) this.messages = new ArrayList<>();
        this.messages.add(message);
        this.lastActivityAt = message.getTimestamp(); // Update activity time
    }
}