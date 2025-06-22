package com.example.appc2c;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo RecyclerView sản phẩm
        recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setNestedScrollingEnabled(false);

        // Danh sách sản phẩm mẫu
        List<Product> productList = new ArrayList<>();
        productList.add(new Product(
                "Áo hoodie local brand",
                "₫199.000",
                "https://via.placeholder.com/300.png",
                "Áo hoodie mềm mại, thích hợp thời tiết se lạnh"
        ));

        productList.add(new Product(
                "Giày sneaker năng động",
                "₫399.000",
                "https://via.placeholder.com/300.png",
                "Thiết kế thể thao, năng động, phù hợp đi học đi chơi"
        ));

        productList.add(new Product(
                "Balo thời trang",
                "₫299.000",
                "https://via.placeholder.com/300.png",
                "Chất vải chống nước, nhiều ngăn tiện dụng"
        ));


        ProductAdapter productAdapter = new ProductAdapter(this, productList);
        recyclerProducts.setAdapter(productAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {

                return true;
            } else if (itemId == R.id.nav_post) {
                startActivity(new Intent(MainActivity.this, PostProductActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // ✅ Đảm bảo tab Home được chọn khi khởi động
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
