package com.example.appc2c.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.appc2c.R;
import androidx.appcompat.app.AppCompatActivity;

public class PublicProfileActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        // Ánh xạ View
        TextView txtName = findViewById(R.id.txtName);
        TextView txtBio = findViewById(R.id.txtBio);
        TextView txtRating = findViewById(R.id.txtRating);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String bio = intent.getStringExtra("bio");
        String rating = intent.getStringExtra("rating");

        // Gán dữ liệu hiển thị
        txtName.setText(name != null ? name : "Người dùng");
        txtBio.setText("Mô tả: " + (bio != null ? bio : "Chưa có mô tả."));
        txtRating.setText("Đánh giá: " + (rating != null ? rating : "0") + " ★");

        // (Nếu sau này muốn truyền ảnh đại diện thì thêm ở đây bằng Glide hoặc URI)
    }
}
