package com.example.appc2c;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtPhone, txtAddress;
    private ImageView imgAvatar;
    private static final int PICK_IMAGE_AVATAR = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ view
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);
        Button btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("name", txtName.getText().toString());
            intent.putExtra("email", txtEmail.getText().toString().replace("Email: ", ""));
            intent.putExtra("phone", txtPhone.getText().toString().replace("SĐT: ", ""));
            intent.putExtra("address", txtAddress.getText().toString().replace("Địa chỉ: ", ""));
            startActivityForResult(intent, 1001);
        });

        // Chọn ảnh đại diện
        imgAvatar = findViewById(R.id.imgAvatar);
        imgAvatar.setOnClickListener(v -> openGallery());

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Chuyển về màn hình Login
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xoá ngăn quay lại
            startActivity(intent);
            finish();
        });

        // BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_post) {
                startActivity(new Intent(this, PostProductActivity.class));
                return true;
            }
            return true;
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_AVATAR);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            txtName.setText(data.getStringExtra("name"));
            txtEmail.setText("Email: " + data.getStringExtra("email"));
            txtPhone.setText("SĐT: " + data.getStringExtra("phone"));
            txtAddress.setText("Địa chỉ: " + data.getStringExtra("address"));
        }

        if (requestCode == PICK_IMAGE_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri avatarUri = data.getData();
            Glide.with(this).load(avatarUri).into(imgAvatar);
        }
    }
}
