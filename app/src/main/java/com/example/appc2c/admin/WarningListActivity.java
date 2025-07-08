package com.example.appc2c.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.models.Warning;
import com.example.appc2c.models.WarningAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WarningListActivity extends AppCompatActivity {

    private RecyclerView recyclerWarnings;
    private WarningAdapter warningAdapter;
    private final List<Warning> warningList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning_list);

        recyclerWarnings = findViewById(R.id.recyclerWarnings);
        recyclerWarnings.setLayoutManager(new LinearLayoutManager(this));

        warningAdapter = new WarningAdapter(this, warningList);
        recyclerWarnings.setAdapter(warningAdapter);

        loadWarningsFromFirestore();
    }

    private void loadWarningsFromFirestore() {
        FirebaseFirestore.getInstance()
                .collection("warnings")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    warningList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Warning warning = doc.toObject(Warning.class);
                        warningList.add(warning);
                    }
                    warningAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi tải danh sách cảnh báo", Toast.LENGTH_SHORT).show());
    }
}
