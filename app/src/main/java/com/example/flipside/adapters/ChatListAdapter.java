package com.example.flipside.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.activities.ChatActivity;
import com.example.flipside.models.Chat;
import com.example.flipside.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private Context context;
    private List<Chat> chatList;
    private String currentUserId;
    private FirebaseFirestore db;

    public ChatListAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // 1. Identify the "Other" User
        String otherUserId = "";
        for (String id : chat.getParticipantIds()) {
            if (!id.equals(currentUserId)) {
                otherUserId = id;
                break;
            }
        }

        // 2. Fetch Other User's Name
        // (For production, cache this or store name in Chat object to reduce reads)
        final String finalOtherUserId = otherUserId;
        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        holder.tvName.setText(name != null ? name : "Unknown User");
                    }
                });

        // 3. Show Last Message
        if (chat.getMessages() != null && !chat.getMessages().isEmpty()) {
            Message lastMsg = chat.getMessages().get(chat.getMessages().size() - 1);
            holder.tvLastMessage.setText(lastMsg.getContent());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(lastMsg.getTimestamp())));
        } else {
            holder.tvLastMessage.setText("Start a conversation");
            holder.tvTime.setText("");
        }

        // 4. Click to Open Chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", finalOtherUserId);
            // We pass receiverId so ChatActivity can generate the correct chatId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime;
        ImageView ivProfile;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvChatLastMsg);
            tvTime = itemView.findViewById(R.id.tvChatTime);
            ivProfile = itemView.findViewById(R.id.ivChatProfile);
        }
    }
}