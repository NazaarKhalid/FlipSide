package com.example.flipside.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.MessageAdapter;
import com.example.flipside.models.Chat;
import com.example.flipside.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private Button btnSend;
    private MessageAdapter adapter;
    private List<Message> messageList;

    private FirebaseFirestore db;
    private String currentUserId;
    private String receiverId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("sellerId");

        rvChatMessages = findViewById(R.id.rvChatMessages);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(adapter);

        setupChat();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupChat() {
        if (currentUserId.compareTo(receiverId) < 0) {
            chatId = currentUserId + "_" + receiverId;
        } else {
            chatId = receiverId + "_" + currentUserId;
        }

        db.collection("chats").document(chatId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) return;

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Chat chat = documentSnapshot.toObject(Chat.class);
                        if (chat != null && chat.getMessages() != null) {
                            messageList.clear();
                            messageList.addAll(chat.getMessages());
                            adapter.notifyDataSetChanged();
                            rvChatMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String content = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        etMessageInput.setText("");

        String messageId = String.valueOf(System.currentTimeMillis());
        Message message = new Message(messageId, currentUserId, content);

        db.collection("chats").document(chatId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        List<String> participants = Arrays.asList(currentUserId, receiverId);
                        Chat newChat = new Chat(chatId, participants);
                        newChat.addMessage(message);
                        db.collection("chats").document(chatId).set(newChat);
                    } else {
                        db.collection("chats").document(chatId)
                                .update("messages", FieldValue.arrayUnion(message));
                    }
                });
    }
}