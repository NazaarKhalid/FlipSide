package com.example.flipside.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ChatAdapter;
import com.example.flipside.models.Chat;
import com.example.flipside.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;

    private ChatAdapter chatAdapter;
    private ArrayList<Message> messageList;
    private FirebaseFirestore db;
    private String currentUserId;
    private String receiverId; // The person we are talking to
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Passed from the previous screen (e.g., Product Details or Chat List)
        receiverId = getIntent().getStringExtra("receiverId");

        initViews();
        setupRecyclerView();

        // Find or Create the Chat ID based on participants
        generateChatId();

        // Listen for live messages
        listenForMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);
    }

    private void generateChatId() {
        // Unique ID combination: SmallerID_LargerID (so it's always the same for two people)
        if (currentUserId.compareTo(receiverId) < 0) {
            chatId = currentUserId + "_" + receiverId;
        } else {
            chatId = receiverId + "_" + currentUserId;
        }
    }

    private void listenForMessages() {
        // Listen to the specific Chat Document
        db.collection("chats").document(chatId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;

                    if (snapshot != null && snapshot.exists()) {
                        Chat chat = snapshot.toObject(Chat.class);
                        if (chat != null && chat.getMessages() != null) {
                            messageList.clear();
                            messageList.addAll(chat.getMessages());
                            chatAdapter.notifyDataSetChanged();
                            rvChat.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        etMessage.setText(""); // Clear input

        String messageId = UUID.randomUUID().toString();
        Message newMessage = new Message(messageId, currentUserId, content);

        db.collection("chats").document(chatId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Chat chat;
                    if (documentSnapshot.exists()) {
                        // Chat exists, add message
                        chat = documentSnapshot.toObject(Chat.class);
                        chat.addMessage(newMessage);
                    } else {
                        // Create new chat
                        chat = new Chat(chatId, Arrays.asList(currentUserId, receiverId));
                        chat.addMessage(newMessage);
                    }

                    // Save back to Firestore
                    db.collection("chats").document(chatId).set(chat);
                });
    }
}