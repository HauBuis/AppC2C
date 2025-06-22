package com.example.appc2c;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class PostProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgProduct;
    private EditText edtProductName, edtDescription, edtPrice;
    private Spinner spinnerCategory;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_product);

        imgProduct = findViewById(R.id.imgProduct);
        edtProductName = findViewById(R.id.edtProductName);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        Button btnChooseImage = findViewById(R.id.btnChooseImage);
        Button btnPost = findViewById(R.id.btnPost);

        btnChooseImage.setOnClickListener(v -> openGallery());

        btnPost.setOnClickListener(v -> {
            String name = edtProductName.getText().toString();
            String description = edtDescription.getText().toString();
            String price = edtPrice.getText().toString();
            String category = spinnerCategory.getSelectedItem() != null
                    ? spinnerCategory.getSelectedItem().toString() : "Chưa chọn";

            if (name.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và giá sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạm thời chỉ in ra thông tin
            Toast.makeText(this, "Đã đăng: " + name, Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation xử lý chuyển trang
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_post);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else return id == R.id.nav_post;
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgProduct); // load ảnh trực tiếp
        }
    }
}
