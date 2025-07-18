package com.example.appc2c.products;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.models.User;
import com.example.appc2c.profile.PublicProfileActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SellerListActivity extends AppCompatActivity {

    private RecyclerView recyclerSeller;
    private SellerAdapter sellerAdapter;
    private final List<User> sellerList = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_list);

        recyclerSeller = findViewById(R.id.recyclerSeller);
        recyclerSeller.setLayoutManager(new LinearLayoutManager(this));

        sellerAdapter = new SellerAdapter(this, sellerList);
        recyclerSeller.setAdapter(sellerAdapter);

        firestore = FirebaseFirestore.getInstance();
        loadSellers();

        sellerAdapter.setOnItemClickListener(user -> {
            Intent intent = new Intent(SellerListActivity.this, PublicProfileActivity.class);
            intent.putExtra("userId", user.getId());
            intent.putExtra("userName", user.getName());
            intent.putExtra("userEmail", user.getEmail());
            intent.putExtra("userAvatar", user.getAvatar());
            startActivity(intent);
        });
    }

    private void loadSellers() {
        firestore.collection("users")
                .whereIn("role", Arrays.asList("user", "seller"))
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sellerList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            sellerList.add(user);
                        }
                    }
                    sellerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SellerListActivity.this, "Lỗi tải danh sách", Toast.LENGTH_SHORT).show());
    }
}
