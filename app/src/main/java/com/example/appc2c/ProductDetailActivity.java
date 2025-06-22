package com.example.appc2c;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ImageView img = findViewById(R.id.imgDetail);
        TextView txtName = findViewById(R.id.txtDetailName);
        TextView txtPrice = findViewById(R.id.txtDetailPrice);
        TextView txtDesc = findViewById(R.id.txtDetailDescription);

        // Lấy dữ liệu truyền sang
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            txtName.setText(extras.getString("name"));
            txtPrice.setText(extras.getString("price"));
            txtDesc.setText(extras.getString("desc"));
            Glide.with(this).load(extras.getString("image")).into(img);
        }
    }
}
