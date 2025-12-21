package com.example.flipside.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.ChatListAdapter;
import com.example.flipside.models.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private RecyclerView rvChatList;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private ChatListAdapter adapter;
    private List<Chat> chatList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvChatList = view.findViewById(R.id.rvChatList);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmptyChats);

        setupRecyclerView();
        loadChats();

        return view;
    }

    private void setupRecyclerView() {
        chatList = new ArrayList<>();
        adapter = new ChatListAdapter(getContext(), chatList);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatList.setAdapter(adapter);
    }

    private void loadChats() {
        progressBar.setVisibility(View.VISIBLE);

        // Fetch chats where "participantIds" array contains currentUserId
        db.collection("chats")
                .whereArrayContains("participantIds", currentUserId)
                .orderBy("lastActivityAt", Query.Direction.DESCENDING) // Show newest first
                .addSnapshotListener((snapshots, e) -> {
                    if (getContext() == null) return;
                    progressBar.setVisibility(View.GONE);

                    if (e != null) return;

                    if (snapshots != null) {
                        chatList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                            Chat chat = doc.toObject(Chat.class);
                            chatList.add(chat);
                        }

                        if (chatList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}