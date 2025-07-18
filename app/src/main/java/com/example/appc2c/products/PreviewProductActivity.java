package com.example.appc2c.products;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class PreviewProductActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_product);

        ImageView imgPreview = findViewById(R.id.imgPreview);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtDescription = findViewById(R.id.txtDescription);
        TextView txtPrice = findViewById(R.id.txtPrice);
        TextView txtCategory = findViewById(R.id.txtCategory);
        TextView txtCondition = findViewById(R.id.txtCondition);
        TextView txtLocation = findViewById(R.id.txtLocation);

        Intent intent = getIntent();
        txtName.setText("Tên sản phẩm: " + intent.getStringExtra("name"));
        txtDescription.setText("Mô tả: " + intent.getStringExtra("description"));
        txtPrice.setText("Giá: " + intent.getStringExtra("price"));
        txtCategory.setText("Danh mục: " + intent.getStringExtra("category"));
        txtCondition.setText("Tình trạng: " + intent.getStringExtra("condition"));
        txtLocation.setText("Vị trí: " + intent.getStringExtra("location"));

        // Lấy cả imageUrl và imageUris để fallback nếu thiếu
        ArrayList<String> imageList = intent.getStringArrayListExtra("imageUris");
        String imageUrl = intent.getStringExtra("imageUrl");

        if (imageList != null && !imageList.isEmpty()) {
            String firstImage = imageList.get(0);
            Log.d("PREVIEW_IMAGE", "Đường dẫn ảnh từ imageUris: " + firstImage);
            loadImage(firstImage, imgPreview);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("PREVIEW_IMAGE", "Đường dẫn ảnh từ imageUrl: " + imageUrl);
            loadImage(imageUrl, imgPreview);
        } else {
            Log.e("PREVIEW_IMAGE", "Không có ảnh để hiển thị");
            Toast.makeText(this, "Không có ảnh để hiển thị", Toast.LENGTH_SHORT).show();
        }

        MaterialToolbar toolbar = findViewById(R.id.topAppBarPreview);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadImage(String url, ImageView imgPreview) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_launcher_foreground)
                .into(imgPreview);
    }
}
