package com.example.appc2c.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ModerationActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private UserManageAdapter userManageAdapter;
    private final List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderation);

        recyclerUsers = findViewById(R.id.recyclerModeration);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        userManageAdapter = new UserManageAdapter(this, userList);
        recyclerUsers.setAdapter(userManageAdapter);

        loadUsers();
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());
                        userList.add(user);
                    }
                    userManageAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải danh sách user", Toast.LENGTH_SHORT).show()
                );
    }
}
