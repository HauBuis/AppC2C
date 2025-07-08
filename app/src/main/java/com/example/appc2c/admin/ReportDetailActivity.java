package com.example.appc2c.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appc2c.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportDetailActivity extends AppCompatActivity {

    private TextView txtTitle, txtDescription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        txtTitle = findViewById(R.id.txtReportTitle);
        txtDescription = findViewById(R.id.txtReportDescription);

        String type = getIntent().getStringExtra("type");       // "product" | "user"
        String targetId = getIntent().getStringExtra("targetId");

        if (type == null || targetId == null) {
            Toast.makeText(this, "Thiếu dữ liệu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(type.equals("product") ? "products" : "users")
                .document(targetId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        showContent(type, snapshot);
                    } else {
                        Toast.makeText(this, "Không tìm thấy đối tượng bị báo cáo", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showContent(String type, DocumentSnapshot doc) {
        if (type.equals("product")) {
            String name = doc.getString("name");
            String desc = doc.getString("desc");
            txtTitle.setText("Tên sản phẩm: " + name);
            txtDescription.setText(desc);
        } else {
            String name = doc.getString("name");
            String email = doc.getString("email");
            txtTitle.setText("Người dùng: " + name);
            txtDescription.setText("Email: " + email);
        }
    }
}
