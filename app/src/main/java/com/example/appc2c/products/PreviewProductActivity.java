package com.example.appc2c.products;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appc2c.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class PreviewProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_product);

        // Ánh xạ view
        ImageView imgPreview = findViewById(R.id.imgPreview);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtDescription = findViewById(R.id.txtDescription);
        TextView txtPrice = findViewById(R.id.txtPrice);
        TextView txtFeatures = findViewById(R.id.txtFeatures);
        TextView txtTags = findViewById(R.id.txtTags);
        TextView txtCategory = findViewById(R.id.txtCategory);
        TextView txtCondition = findViewById(R.id.txtCondition);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        txtName.setText(intent.getStringExtra("name"));
        txtDescription.setText(intent.getStringExtra("description"));
        txtPrice.setText(intent.getStringExtra("price"));
        txtFeatures.setText(intent.getStringExtra("features"));
        txtTags.setText(intent.getStringExtra("tags"));
        txtCategory.setText(intent.getStringExtra("category"));
        txtCondition.setText(intent.getStringExtra("condition"));

        // Hiển thị ảnh đầu tiên (nếu có)
        ArrayList<String> imageUrls = intent.getStringArrayListExtra("imageUris");
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String firstImageUrl = imageUrls.get(0);
            Log.d("PREVIEW_IMAGE_URL", "URL: " + firstImageUrl);

            Glide.with(this)
                    .load(firstImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_delete)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            e.printStackTrace();
                            Toast.makeText(PreviewProductActivity.this, "Lỗi load ảnh", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, @NonNull Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(imgPreview);
        } else {
            Log.e("PREVIEW_IMAGE_URL", "Danh sách imageUris null hoặc rỗng");
            Toast.makeText(this, "Không có ảnh để hiển thị", Toast.LENGTH_SHORT).show();
        }

        // Xử lý toolbar back
        MaterialToolbar toolbar = findViewById(R.id.topAppBarPreview);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
