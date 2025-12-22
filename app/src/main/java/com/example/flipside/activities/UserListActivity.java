package com.example.flipside.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipside.R;
import com.example.flipside.adapters.UserListAdapter; // See Step 4
import com.example.flipside.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView rvUserList;
    private TextView tvTitle;
    private UserListAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list); // Create a simple layout with RecyclerView + Title

        db = FirebaseFirestore.getInstance();

        String title = getIntent().getStringExtra("title");
        String targetUserId = getIntent().getStringExtra("userId");
        String collectionPath = getIntent().getStringExtra("collection"); // "followers" or "following"

        rvUserList = findViewById(R.id.rvUserList);
        tvTitle = findViewById(R.id.tvListTitle);
        tvTitle.setText(title);

        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserListAdapter(this, userList);
        rvUserList.setAdapter(adapter);

        loadUsers(targetUserId, collectionPath);
    }


    private void loadUsers(String userId, String collection) {
        db.collection("users").document(userId).collection(collection)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) return;

                    for (DocumentSnapshot doc : snapshots) {
                        String relatedUserId = doc.getId();

                        db.collection("users").document(relatedUserId).get()
                                .addOnSuccessListener(userDoc -> {
                                    User user = userDoc.toObject(User.class);
                                    if (user != null) {
                                        // CRITICAL FIX: Ensure ID is set!
                                        user.setUserId(userDoc.getId());

                                        userList.add(user);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }
}